package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Collision-aware tile path guidance inside the currently loaded scene.
 *
 * This replaces the old directional arrow. The route is recalculated only when
 * the player's tile or destination changes, not every frame.
 */
public final class PathGuidanceOverlay extends Overlay
{
    private static final int[][] DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int MAX_VISITED = 7000;
    private static final int MAX_DRAW_TILES = 48;

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    private WorldPoint cachedStart;
    private WorldPoint cachedTarget;
    private List<WorldPoint> cachedPath = Collections.emptyList();

    public PathGuidanceOverlay(
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
        setPriority(PRIORITY_HIGH);
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

        WorldPoint start = player.getWorldLocation();
        WorldPoint target = resolveTargetPoint(step, start);

        if (start == null || target == null || start.getPlane() != target.getPlane())
        {
            clearCache();
            return null;
        }

        boolean distantTarget = !isLoaded(target);

        if (!start.equals(cachedStart) || !target.equals(cachedTarget))
        {
            cachedStart = start;
            cachedTarget = target;

            if (distantTarget)
            {
                WorldPoint sceneWaypoint = resolveLoadedWaypoint(step, start);

                if (sceneWaypoint != null)
                {
                    cachedPath = findPath(start, sceneWaypoint);
                }
                else
                {
                    cachedPath = findPathTowardDistantTarget(start, target);
                }
            }
            else
            {
                cachedPath = findPath(start, target);
            }
        }

        if (cachedPath.isEmpty())
        {
            return null;
        }

        int begin = cachedPath.size() > 1 ? 1 : 0;
        int end = Math.min(cachedPath.size(), begin + MAX_DRAW_TILES);

        for (int i = begin; i < end; i++)
        {
            WorldPoint point = cachedPath.get(i);
            LocalPoint local = LocalPoint.fromWorld(client, point);
            if (local == null)
            {
                continue;
            }

            Polygon poly = Perspective.getCanvasTilePoly(client, local);
            if (poly == null)
            {
                continue;
            }

            boolean last = i == cachedPath.size() - 1;
            Color outlineColor = last
                ? config.pathTargetOutlineColor()
                : config.pathOutlineColor();
            Color fillColor = last
                ? config.pathTargetFillColor()
                : config.pathFillColor();

            OverlayUtil.renderPolygon(
                graphics,
                poly,
                outlineColor,
                fillColor,
                new BasicStroke(last ? 3f : 2f)
            );
        }

        return null;
    }

    /**
     * Builds a collision-aware rolling route toward a destination outside the
     * currently loaded scene.
     *
     * We flood-fill reachable loaded tiles and remember the tile which gets
     * closest to the real world destination. The overlay therefore leads the
     * player to the best scene frontier instead of disappearing. As the player
     * moves and new scene tiles load, the route is recalculated toward the same
     * final destination.
     */
    private List<WorldPoint> findPathTowardDistantTarget(WorldPoint start, WorldPoint finalTarget)
    {
        Queue<WorldPoint> open = new ArrayDeque<>();
        Set<WorldPoint> visited = new HashSet<>();
        Map<WorldPoint, WorldPoint> parent = new HashMap<>();

        open.add(start);
        visited.add(start);

        WorldPoint best = start;
        long bestDistanceSquared = distanceSquared(start, finalTarget);

        while (!open.isEmpty() && visited.size() < MAX_VISITED)
        {
            WorldPoint current = open.remove();

            long currentDistanceSquared = distanceSquared(current, finalTarget);
            if (currentDistanceSquared < bestDistanceSquared)
            {
                bestDistanceSquared = currentDistanceSquared;
                best = current;
            }

            WorldArea area = new WorldArea(current, 1, 1);

            for (int[] direction : DIRECTIONS)
            {
                int dx = direction[0];
                int dy = direction[1];

                if (!area.canTravelInDirection(client.getTopLevelWorldView(), dx, dy))
                {
                    continue;
                }

                WorldPoint next = new WorldPoint(
                    current.getX() + dx,
                    current.getY() + dy,
                    current.getPlane()
                );

                if (visited.contains(next) || !isLoaded(next))
                {
                    continue;
                }

                visited.add(next);
                parent.put(next, current);
                open.add(next);
            }
        }

        if (best.equals(start))
        {
            return Collections.emptyList();
        }

        List<WorldPoint> path = reconstructPath(start, best, parent);
        if (path.isEmpty())
        {
            return path;
        }

        // Keep only a useful forward segment. Once the player reaches the end,
        // scene loading changes and the route is rebuilt toward finalTarget.
        if (path.size() > MAX_DRAW_TILES + 1)
        {
            return new ArrayList<>(path.subList(0, MAX_DRAW_TILES + 1));
        }

        return path;
    }

    private static long distanceSquared(WorldPoint from, WorldPoint to)
    {
        long dx = (long) to.getX() - from.getX();
        long dy = (long) to.getY() - from.getY();
        return dx * dx + dy * dy;
    }

    private static List<WorldPoint> reconstructPath(
        WorldPoint start,
        WorldPoint reached,
        Map<WorldPoint, WorldPoint> parent)
    {
        List<WorldPoint> path = new ArrayList<>();
        WorldPoint cursor = reached;
        path.add(cursor);

        while (!cursor.equals(start))
        {
            cursor = parent.get(cursor);
            if (cursor == null)
            {
                return Collections.emptyList();
            }
            path.add(cursor);
        }

        Collections.reverse(path);
        return path;
    }

    private List<WorldPoint> findPath(WorldPoint start, WorldPoint target)
    {
        if (start.distanceTo(target) <= 1)
        {
            List<WorldPoint> direct = new ArrayList<>();
            direct.add(start);
            direct.add(target);
            return direct;
        }

        Queue<WorldPoint> open = new ArrayDeque<>();
        Set<WorldPoint> visited = new HashSet<>();
        Map<WorldPoint, WorldPoint> parent = new HashMap<>();

        open.add(start);
        visited.add(start);

        WorldPoint reached = null;

        while (!open.isEmpty() && visited.size() < MAX_VISITED)
        {
            WorldPoint current = open.remove();

            if (current.distanceTo(target) <= 1)
            {
                reached = current;
                break;
            }

            WorldArea area = new WorldArea(current, 1, 1);

            for (int[] direction : DIRECTIONS)
            {
                int dx = direction[0];
                int dy = direction[1];

                if (!area.canTravelInDirection(client.getTopLevelWorldView(), dx, dy))
                {
                    continue;
                }

                WorldPoint next = new WorldPoint(
                    current.getX() + dx,
                    current.getY() + dy,
                    current.getPlane()
                );

                if (visited.contains(next) || !isLoaded(next))
                {
                    continue;
                }

                visited.add(next);
                parent.put(next, current);
                open.add(next);
            }
        }

        if (reached == null)
        {
            return Collections.emptyList();
        }

        List<WorldPoint> path = reconstructPath(start, reached, parent);
        if (path.isEmpty())
        {
            return path;
        }

        // Append the actual target tile only when it is walkable/adjacent so the
        // final destination is visually obvious.
        if (!path.get(path.size() - 1).equals(target))
        {
            path.add(target);
        }

        return path;
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

        WorldPoint loadedWaypoint = resolveLoadedWaypoint(step, playerPoint);
        if (loadedWaypoint != null)
        {
            return loadedWaypoint;
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

    private WorldPoint resolveLoadedWaypoint(GuideStep step, WorldPoint playerPoint)
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

            WorldPoint point = new WorldPoint(
                waypoint.getX(),
                waypoint.getY(),
                waypoint.getPlane()
            );

            if (!isLoaded(point))
            {
                continue;
            }

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
        if (nearestDistance <= 4)
        {
            for (int i = nearestIndex + 1; i < step.getWaypoints().size(); i++)
            {
                GuideWaypoint next = step.getWaypoints().get(i);
                WorldPoint nextPoint = new WorldPoint(next.getX(), next.getY(), next.getPlane());
                if (next.getPlane() == playerPoint.getPlane() && isLoaded(nextPoint))
                {
                    targetIndex = i;
                    break;
                }
            }
        }

        GuideWaypoint waypoint = step.getWaypoints().get(targetIndex);
        return new WorldPoint(waypoint.getX(), waypoint.getY(), waypoint.getPlane());
    }

    private boolean isLoaded(WorldPoint point)
    {
        return point != null && LocalPoint.fromWorld(client, point) != null;
    }

    private void clearCache()
    {
        cachedStart = null;
        cachedTarget = null;
        cachedPath = Collections.emptyList();
    }

    private static boolean matchesTarget(String normalizedTarget, String candidateName)
    {
        if (normalizedTarget.isEmpty() || candidateName == null || candidateName.trim().isEmpty())
        {
            return false;
        }

        String candidate = normalize(candidateName);
        return normalizedTarget.equals(candidate)
            || normalizedTarget.contains(candidate)
            || candidate.contains(normalizedTarget);
    }

    private static String normalize(String value)
    {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
