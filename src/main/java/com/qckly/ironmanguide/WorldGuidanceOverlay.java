package com.qckly.ironmanguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Renders Quest-Helper-style world guidance for the current guide target.
 *
 * V1 matches the current step TARGET text against loaded NPC and scene-object
 * names. Later revisions can move to explicit target IDs/coordinates for full
 * determinism, but this gives us real live guidance on Tutorial Island now.
 */
public final class WorldGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color CYAN_FILL = new Color(0, 220, 255, 25);

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    public WorldGuidanceOverlay(
        Client client,
        GuideState guideState,
        ZeroKnowledgeIronmanConfig config,
        ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.guideState = guideState;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showWorldGuidance())
        {
            return null;
        }

        GuideStep step = guideState.getCurrentStep();
        if (step == null || !hasText(step.getTarget()))
        {
            return null;
        }

        String target = normalize(step.getTarget());

        renderMatchingNpcs(target);
        renderMatchingObjects(graphics, target);

        return null;
    }

    private void renderMatchingNpcs(String target)
    {
        for (NPC npc : client.getNpcs())
        {
            String name = npc.getName();
            if (!matchesTarget(target, name))
            {
                continue;
            }

            modelOutlineRenderer.drawOutline(npc, 2, CYAN, 2);
        }
    }

    private void renderMatchingObjects(Graphics2D graphics, String target)
    {
        Tile[][][] sceneTiles = client.getScene().getTiles();
        int plane = client.getPlane();

        if (sceneTiles == null || plane < 0 || plane >= sceneTiles.length)
        {
            return;
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

                renderObjectIfMatching(graphics, target, tile.getWallObject());
                renderObjectIfMatching(graphics, target, tile.getDecorativeObject());
                renderObjectIfMatching(graphics, target, tile.getGroundObject());

                GameObject[] gameObjects = tile.getGameObjects();
                if (gameObjects != null)
                {
                    for (GameObject gameObject : gameObjects)
                    {
                        renderObjectIfMatching(graphics, target, gameObject);
                    }
                }
            }
        }
    }

    private void renderObjectIfMatching(Graphics2D graphics, String target, TileObject object)
    {
        if (object == null)
        {
            return;
        }

        String name = objectName(object);
        if (!matchesTarget(target, name))
        {
            return;
        }

        modelOutlineRenderer.drawOutline(object, 2, CYAN, 2);

        Shape clickbox = object.getClickbox();
        if (clickbox != null)
        {
            OverlayUtil.renderPolygon(graphics, clickbox, CYAN, CYAN_FILL, new java.awt.BasicStroke(1.5f));
        }
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

        // Exact/substring matching works well with our human-readable targets:
        // "Fishing spot in the nearby pond" -> "Fishing spot"
        // "Survival Expert / Brynna" -> "Survival Expert"
        // "Any normal Tree in the survival area" -> "Tree"
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
}
