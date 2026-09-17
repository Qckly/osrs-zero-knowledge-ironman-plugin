package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Renders Quest-Helper-style world guidance for the current guide target.
 *
 * V2 deliberately selects one primary target instead of highlighting every
 * matching object/NPC in the loaded scene. This keeps the guide readable and
 * makes the world guidance behave like a navigation aid rather than a debug
 * object inspector.
 *
 * Selection policy for now:
 *   1. Match current TARGET text against loaded NPC/object names.
 *   2. Ignore candidates on another plane.
 *   3. Select the nearest matching candidate to the local player.
 *
 * A later route-data pass can replace NEAREST with explicit target IDs,
 * preferred tiles/areas and path-distance scoring for steps that need it.
 */
public final class WorldGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color CYAN_FILL = new Color(0, 220, 255, 25);

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final TutorialStateTracker tutorialStateTracker;

    public WorldGuidanceOverlay(
        Client client,
        GuideState guideState,
        ZeroKnowledgeIronmanConfig config,
        ModelOutlineRenderer modelOutlineRenderer,
        TutorialStateTracker tutorialStateTracker)
    {
        this.client = client;
        this.guideState = guideState;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
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
        if (step == null || player == null || !hasText(step.getTarget()))
        {
            return null;
        }

        String target = normalize(step.getTarget());
        WorldPoint playerPoint = player.getWorldLocation();
        if (playerPoint == null)
        {
            return null;
        }

        Integer exactObjectId = TutorialExactTargetResolver.resolveObjectId(step, tutorialStateTracker);
        GuidanceTarget primary = exactObjectId != null
            ? findExactObjectTarget(exactObjectId, playerPoint)
            : findPrimaryTarget(target, playerPoint);
        if (primary == null)
        {
            return null;
        }

        if (primary.npc != null)
        {
            modelOutlineRenderer.drawOutline(primary.npc, 2, CYAN, 2);
        }
        else if (primary.object != null)
        {
            modelOutlineRenderer.drawOutline(primary.object, 2, CYAN, 2);

            Shape clickbox = primary.object.getClickbox();
            if (clickbox != null)
            {
                OverlayUtil.renderPolygon(
                    graphics,
                    clickbox,
                    CYAN,
                    CYAN_FILL,
                    new BasicStroke(1.5f)
                );
            }
        }

        return null;
    }


    private GuidanceTarget findExactObjectTarget(int objectId, WorldPoint playerPoint)
    {
        GuidanceTarget best = null;

        Tile[][][] sceneTiles = client.getScene().getTiles();
        int plane = client.getPlane();

        if (sceneTiles == null || plane < 0 || plane >= sceneTiles.length)
        {
            return null;
        }

        for (Tile[] row : sceneTiles[plane])
        {
            if (row == null)
            {
                continue;
            }

            for (Tile tile : row)
            {
                if (tile == null)
                {
                    continue;
                }

                best = considerExactObject(best, objectId, playerPoint, tile.getWallObject());
                best = considerExactObject(best, objectId, playerPoint, tile.getDecorativeObject());
                best = considerExactObject(best, objectId, playerPoint, tile.getGroundObject());

                GameObject[] gameObjects = tile.getGameObjects();
                if (gameObjects != null)
                {
                    for (GameObject gameObject : gameObjects)
                    {
                        best = considerExactObject(best, objectId, playerPoint, gameObject);
                    }
                }
            }
        }

        return best;
    }

    private GuidanceTarget considerExactObject(
        GuidanceTarget current,
        int objectId,
        WorldPoint playerPoint,
        TileObject object)
    {
        if (object == null || object.getId() != objectId)
        {
            return current;
        }

        WorldPoint point = object.getWorldLocation();
        if (!isCandidateOnPlayerPlane(playerPoint, point))
        {
            return current;
        }

        return nearer(
            current,
            new GuidanceTarget(null, object, distance(playerPoint, point))
        );
    }

    private GuidanceTarget findPrimaryTarget(String target, WorldPoint playerPoint)
    {
        GuidanceTarget best = null;

        for (NPC npc : client.getNpcs())
        {
            if (!matchesTarget(target, npc.getName()))
            {
                continue;
            }

            WorldPoint point = npc.getWorldLocation();
            if (!isCandidateOnPlayerPlane(playerPoint, point))
            {
                continue;
            }

            best = nearer(best, new GuidanceTarget(npc, null, distance(playerPoint, point)));
        }

        Tile[][][] sceneTiles = client.getScene().getTiles();
        int plane = client.getPlane();

        if (sceneTiles == null || plane < 0 || plane >= sceneTiles.length)
        {
            return best;
        }

        for (Tile[] row : sceneTiles[plane])
        {
            if (row == null)
            {
                continue;
            }

            for (Tile tile : row)
            {
                if (tile == null)
                {
                    continue;
                }

                best = considerObject(best, target, playerPoint, tile.getWallObject());
                best = considerObject(best, target, playerPoint, tile.getDecorativeObject());
                best = considerObject(best, target, playerPoint, tile.getGroundObject());

                GameObject[] gameObjects = tile.getGameObjects();
                if (gameObjects != null)
                {
                    for (GameObject gameObject : gameObjects)
                    {
                        best = considerObject(best, target, playerPoint, gameObject);
                    }
                }
            }
        }

        return best;
    }

    private GuidanceTarget considerObject(
        GuidanceTarget current,
        String target,
        WorldPoint playerPoint,
        TileObject object)
    {
        if (object == null || !matchesTarget(target, objectName(object)))
        {
            return current;
        }

        WorldPoint point = object.getWorldLocation();
        if (!isCandidateOnPlayerPlane(playerPoint, point))
        {
            return current;
        }

        return nearer(
            current,
            new GuidanceTarget(null, object, distance(playerPoint, point))
        );
    }

    private static GuidanceTarget nearer(GuidanceTarget current, GuidanceTarget candidate)
    {
        if (candidate == null)
        {
            return current;
        }

        if (current == null || candidate.distance < current.distance)
        {
            return candidate;
        }

        return current;
    }

    private static int distance(WorldPoint from, WorldPoint to)
    {
        if (from == null || to == null)
        {
            return Integer.MAX_VALUE;
        }

        return from.distanceTo(to);
    }

    private static boolean isCandidateOnPlayerPlane(WorldPoint player, WorldPoint candidate)
    {
        return player != null
            && candidate != null
            && player.getPlane() == candidate.getPlane();
    }

    private String objectName(TileObject object)
    {
        ObjectComposition composition = client.getObjectDefinition(object.getId());
        if (composition == null)
        {
            return null;
        }

        if (composition.getImpostorIds() != null)
        {
            ObjectComposition impostor = composition.getImpostor();
            if (impostor != null)
            {
                composition = impostor;
            }
        }

        return composition.getName();
    }

    private static boolean matchesTarget(String normalizedTarget, String candidateName)
    {
        if (!hasText(candidateName))
        {
            return false;
        }

        String candidate = normalize(candidateName);
        if (candidate.isEmpty() || "null".equals(candidate))
        {
            return false;
        }

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

    private static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private static final class GuidanceTarget
    {
        private final NPC npc;
        private final TileObject object;
        private final int distance;

        private GuidanceTarget(NPC npc, TileObject object, int distance)
        {
            this.npc = npc;
            this.object = object;
            this.distance = distance;
        }
    }
}
