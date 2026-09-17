package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Highlights the actionable choice inside production/action interfaces.
 *
 * V1 supports Smithing by resolving the current guide target text against the
 * visible widget tree and drawing around the most useful clickable parent.
 */
public final class ActionWidgetGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color FILL = new Color(0, 220, 255, 35);

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public ActionWidgetGuidanceOverlay(
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
        setLayer(OverlayLayer.ABOVE_WIDGETS);
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
        if (step == null)
        {
            return null;
        }

        String target = normalize(step.getTarget());

        // Smithing interface: e.g. "Bronze dagger in the Smithing interface".
        if (target.contains("smithing interface"))
        {
            Widget root = client.getWidget(InterfaceID.Smithing.UNIVERSE);
            if (root != null && !root.isHidden())
            {
                String needle = smithingNeedle(target);
                Widget match = findBestTextMatch(root, needle);
                if (match != null)
                {
                    drawWidget(graphics, expandToActionParent(match));
                }
            }
        }

        return null;
    }

    private static String smithingNeedle(String target)
    {
        String cleaned = target
            .replace("in the smithing interface", "")
            .replace("smithing interface", "")
            .replace("bronze ", "")
            .trim();

        return cleaned.isEmpty() ? "dagger" : cleaned;
    }

    private Widget findBestTextMatch(Widget widget, String needle)
    {
        if (widget == null || widget.isHidden())
        {
            return null;
        }

        String text = normalize(stripTags(widget.getText()));
        if (!text.isEmpty() && (text.equals(needle) || text.contains(needle)))
        {
            return widget;
        }

        Widget[] children = widget.getChildren();
        if (children != null)
        {
            for (Widget child : children)
            {
                Widget match = findBestTextMatch(child, needle);
                if (match != null)
                {
                    return match;
                }
            }
        }

        Widget[] dynamicChildren = widget.getDynamicChildren();
        if (dynamicChildren != null)
        {
            for (Widget child : dynamicChildren)
            {
                Widget match = findBestTextMatch(child, needle);
                if (match != null)
                {
                    return match;
                }
            }
        }

        Widget[] staticChildren = widget.getStaticChildren();
        if (staticChildren != null)
        {
            for (Widget child : staticChildren)
            {
                Widget match = findBestTextMatch(child, needle);
                if (match != null)
                {
                    return match;
                }
            }
        }

        Widget[] nestedChildren = widget.getNestedChildren();
        if (nestedChildren != null)
        {
            for (Widget child : nestedChildren)
            {
                Widget match = findBestTextMatch(child, needle);
                if (match != null)
                {
                    return match;
                }
            }
        }

        return null;
    }

    private Widget expandToActionParent(Widget widget)
    {
        Widget best = widget;
        Rectangle bestBounds = safeBounds(best);

        for (Widget parent = widget.getParent(); parent != null; parent = parent.getParent())
        {
            Rectangle bounds = safeBounds(parent);
            if (bounds == null)
            {
                continue;
            }

            // Production choices are typically around 60-120 px wide/high.
            // Stop before climbing into the whole Smithing window.
            if (bounds.width > 150 || bounds.height > 120)
            {
                break;
            }

            if (bestBounds == null || bounds.width >= bestBounds.width || bounds.height >= bestBounds.height)
            {
                best = parent;
                bestBounds = bounds;
            }
        }

        return best;
    }

    private static Rectangle safeBounds(Widget widget)
    {
        if (widget == null || widget.isHidden())
        {
            return null;
        }

        Rectangle bounds = widget.getBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
        {
            return null;
        }

        return bounds;
    }

    private static void drawWidget(Graphics2D graphics, Widget widget)
    {
        Rectangle bounds = safeBounds(widget);
        if (bounds == null)
        {
            return;
        }

        graphics.setColor(FILL);
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        graphics.setColor(CYAN);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    private static String stripTags(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]+>", " ");
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
