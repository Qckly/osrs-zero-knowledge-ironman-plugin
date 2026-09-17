package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws a directional arrow in the game viewport when the current destination
 * is off-screen or otherwise not obvious.
 *
 * Target priority:
 *  1) native OSRS hint-arrow target;
 *  2) ordered guide waypoint;
 *  3) matching loaded NPC.
 *
 * The arrow is camera-relative, so rotating the camera also rotates the arrow.
 */
public final class DirectionalGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color FILL = new Color(0, 220, 255, 70);
    private static final int ARROW_DISTANCE_FROM_PLAYER = 92;
    private static final int ARROW_LENGTH = 30;
    private static final int ARROW_HALF_WIDTH = 13;

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public DirectionalGuidanceOverlay(
        Client client,
        GuideState guideState,
        ZeroKnowledgeIronmanConfig config,
        TutorialStateTracker tutorialStateTracker)
    {
        this.client = client;
        this.guideState = guideState;
        this.config = config;
        this.tutorialStateTracker = tutorialStateTracker;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        boolean forceVisibleForTutorialQa =
            tutorialStateTracker != null && tutorialStateTracker.isOnTutorialIsland();

        if (!forceVisibleForTutorialQa && !config.showWorldGuidance())
        {
            return null;
        }

        GuideStep step = guideState.getCurrentStep();
        Player player = client.getLocalPlayer();
        if (step == null || player == null)
        {
            return null;
        }

        WorldPoint playerPoint = player.getWorldLocation();
        WorldPoint targetPoint = resolveTargetPoint(step, playerPoint);
        if (playerPoint == null || targetPoint == null || playerPoint.getPlane() != targetPoint.getPlane())
        {
            return null;
        }

        int worldDistance = playerPoint.distanceTo(targetPoint);
        if (worldDistance <= 2)
        {
            return null;
        }

        net.runelite.api.Point playerCanvas = Perspective.localToCanvas(
            client,
            player.getLocalLocation(),
            client.getPlane()
        );

        int originX = playerCanvas != null
            ? playerCanvas.getX()
            : client.getViewportXOffset() + client.getViewportWidth() / 2;
        int originY = playerCanvas != null
            ? playerCanvas.getY() - 25
            : client.getViewportYOffset() + client.getViewportHeight() / 2;

        double dx = targetPoint.getX() - playerPoint.getX();
        double dy = targetPoint.getY() - playerPoint.getY();

        double yaw = client.getCameraYaw() * Perspective.UNIT;
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        // Same yaw transform RuneLite uses for world -> canvas projection.
        double screenX = dx * cos + dy * sin;
        double forward = dy * cos - dx * sin;
        double screenY = -forward;

        double magnitude = Math.hypot(screenX, screenY);
        if (magnitude < 0.001)
        {
            return null;
        }

        double ux = screenX / magnitude;
        double uy = screenY / magnitude;

        int tipX = (int) Math.round(originX + ux * ARROW_DISTANCE_FROM_PLAYER);
        int tipY = (int) Math.round(originY + uy * ARROW_DISTANCE_FROM_PLAYER);

        // Keep the arrow inside the viewport.
        int left = client.getViewportXOffset() + 28;
        int top = client.getViewportYOffset() + 28;
        int right = client.getViewportXOffset() + client.getViewportWidth() - 28;
        int bottom = client.getViewportYOffset() + client.getViewportHeight() - 28;
        tipX = Math.max(left, Math.min(right, tipX));
        tipY = Math.max(top, Math.min(bottom, tipY));

        double baseX = tipX - ux * ARROW_LENGTH;
        double baseY = tipY - uy * ARROW_LENGTH;
        double px = -uy;
        double py = ux;

        Polygon arrow = new Polygon(
            new int[] {
                tipX,
                (int) Math.round(baseX + px * ARROW_HALF_WIDTH),
                (int) Math.round(baseX - px * ARROW_HALF_WIDTH)
            },
            new int[] {
                tipY,
                (int) Math.round(baseY + py * ARROW_HALF_WIDTH),
                (int) Math.round(baseY - py * ARROW_HALF_WIDTH)
            },
            3
        );

        Object oldAntialias = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        java.awt.Stroke oldStroke = graphics.getStroke();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(FILL);
        graphics.fillPolygon(arrow);
        graphics.setColor(CYAN);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawPolygon(arrow);

        graphics.setStroke(oldStroke);
        if (oldAntialias != null)
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
        }

        return null;
    }

    private WorldPoint resolveTargetPoint(GuideStep step, WorldPoint playerPoint)
    {
        NPC hintNpc = client.getHintArrowNpc();
        if (hintNpc != null)
        {
            return hintNpc.getWorldLocation();
        }

        WorldPoint hintPoint = client.getHintArrowPoint();
        if (hintPoint != null)
        {
            return hintPoint;
        }

        WorldPoint waypoint = resolveWaypoint(step, playerPoint);
        if (waypoint != null)
        {
            return waypoint;
        }

        String target = normalize(step.getTarget());
        NPC nearest = null;
        int nearestDistance = Integer.MAX_VALUE;

        for (NPC npc : client.getNpcs())
        {
            if (!matchesTarget(target, npc.getName()))
            {
                continue;
            }

            WorldPoint point = npc.getWorldLocation();
            if (point == null || point.getPlane() != playerPoint.getPlane())
            {
                continue;
            }

            int distance = playerPoint.distanceTo(point);
            if (distance < nearestDistance)
            {
                nearestDistance = distance;
                nearest = npc;
            }
        }

        return nearest == null ? null : nearest.getWorldLocation();
    }

    private WorldPoint resolveWaypoint(GuideStep step, WorldPoint playerPoint)
    {
        if (step.getWaypoints() == null || step.getWaypoints().isEmpty())
        {
            return null;
        }

        int nearestIndex = -1;
        int nearestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < step.getWaypoints().size(); i++)
        {
            GuideWaypoint waypoint = step.getWaypoints().get(i);
            if (waypoint.getPlane() != playerPoint.getPlane())
            {
                continue;
            }

            WorldPoint point = new WorldPoint(waypoint.getX(), waypoint.getY(), waypoint.getPlane());
            int distance = playerPoint.distanceTo(point);
            if (distance < nearestDistance)
            {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }

        if (nearestIndex < 0)
        {
            return null;
        }

        int targetIndex = nearestIndex;
        if (nearestDistance <= 4 && nearestIndex + 1 < step.getWaypoints().size())
        {
            targetIndex = nearestIndex + 1;
        }

        GuideWaypoint waypoint = step.getWaypoints().get(targetIndex);
        return new WorldPoint(waypoint.getX(), waypoint.getY(), waypoint.getPlane());
    }

    private static boolean matchesTarget(String normalizedTarget, String candidateName)
    {
        if (normalizedTarget.isEmpty() || candidateName == null || candidateName.trim().isEmpty())
        {
            return false;
        }

        String candidate = normalize(candidateName);
        if (normalizedTarget.equals(candidate)
            || normalizedTarget.contains(candidate)
            || candidate.contains(normalizedTarget))
        {
            return true;
        }

        // Human guide targets often use aliases: "Mining Instructor / Dezzick".
        for (String alias : normalizedTarget.split("\\s*/\\s*"))
        {
            if (!alias.isEmpty() && (alias.equals(candidate) || alias.contains(candidate) || candidate.contains(alias)))
            {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String value)
    {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replaceAll("[^a-z0-9/]+", " ")
                .trim();
    }
}
