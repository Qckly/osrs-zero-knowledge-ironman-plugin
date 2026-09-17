package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/**
 * Highlights inventory items required by the current guide action.
 *
 * This complements WorldGuidanceOverlay: scene targets such as NPCs, trees and
 * doors are highlighted in the world, while steps such as "use tinderbox on
 * logs" highlight the relevant inventory items instead.
 */
public final class InventoryGuidanceOverlay extends WidgetItemOverlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color CYAN_FILL = new Color(0, 220, 255, 35);

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public InventoryGuidanceOverlay(
        Client client,
        GuideState guideState,
        ZeroKnowledgeIronmanConfig config,
        TutorialStateTracker tutorialStateTracker)
    {
        this.client = client;
        this.guideState = guideState;
        this.config = config;
        this.tutorialStateTracker = tutorialStateTracker;

        showOnInventory();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        boolean forceVisibleForTutorialQa =
            tutorialStateTracker != null && tutorialStateTracker.isOnTutorialIsland();

        if (!forceVisibleForTutorialQa && !config.showWorldGuidance())
        {
            return;
        }

        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            return;
        }

        ItemComposition composition = client.getItemDefinition(itemId);
        if (composition == null || composition.getName() == null)
        {
            return;
        }

        String itemName = normalize(composition.getName());
        if (!inventoryTargets(step).contains(itemName))
        {
            return;
        }

        Rectangle bounds = widgetItem.getCanvasBounds();
        if (bounds == null)
        {
            return;
        }

        graphics.setColor(CYAN_FILL);
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        graphics.setColor(CYAN);
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    private Set<String> inventoryTargets(GuideStep step)
    {
        Set<String> targets = new LinkedHashSet<>();

        String target = normalize(step.getTarget());
        if (target.contains("inventory"))
        {
            addCandidate(target
                .replace("in your inventory", "")
                .replace("in inventory", "")
                .trim(), targets);
        }

        String requirement = step.getRequirement();
        if (requirement != null)
        {
            for (String part : requirement.split("\\+|\\n"))
            {
                String candidate = normalize(part)
                    .replaceAll("^\\d+\\s*x?\\s*", "")
                    .replace("in your inventory", "")
                    .replace("in inventory", "")
                    .replace("equipped", "")
                    .trim();

                addCandidate(candidate, targets);
            }
        }

        return targets;
    }

    private void addCandidate(String candidate, Set<String> targets)
    {
        if (candidate == null || candidate.isEmpty())
        {
            return;
        }

        // Only treat prose as an inventory target if that item actually exists
        // in the player's inventory/equipment snapshot. This prevents generic
        // requirements such as "Prayer interface" from becoming fake item
        // highlights.
        if (tutorialStateTracker.getPossessedQuantity(candidate) > 0)
        {
            targets.add(normalizeExistingItemName(candidate));
        }
    }

    private String normalizeExistingItemName(String candidate)
    {
        String normalized = normalize(candidate);

        for (String item : tutorialStateTracker.getInventoryItems())
        {
            if (sameItemName(normalized, item))
            {
                return item;
            }
        }

        for (String item : tutorialStateTracker.getEquippedItems())
        {
            if (sameItemName(normalized, item))
            {
                return item;
            }
        }

        return normalized;
    }

    private static boolean sameItemName(String requested, String actual)
    {
        if (requested.equals(actual))
        {
            return true;
        }

        String requestedSingular = requested.endsWith("s")
            ? requested.substring(0, requested.length() - 1)
            : requested;
        String actualSingular = actual.endsWith("s")
            ? actual.substring(0, actual.length() - 1)
            : actual;

        return requestedSingular.equals(actualSingular);
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
