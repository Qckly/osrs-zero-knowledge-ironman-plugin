package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Generic action-widget guidance.
 *
 * Guide data supplies UI_TARGET. We then resolve the target against visible
 * game widgets using text, widget name and widget actions. This covers
 * production menus, nested buttons, spell choices, account-mode choices, etc.
 */
public final class ActionWidgetGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color FILL = new Color(0, 220, 255, 35);

    private static final Set<String> TOP_LEVEL_TARGETS = new HashSet<>(Arrays.asList(
        "combat options",
        "skills",
        "quest list",
        "inventory",
        "worn equipment",
        "prayer",
        "magic",
        "friends list",
        "settings"
    ));

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
        if (step == null || !hasText(step.getUiTarget()))
        {
            return null;
        }

        String needle = normalize(step.getUiTarget());
        if (TOP_LEVEL_TARGETS.contains(needle))
        {
            return null;
        }

        Widget match = findPreferredMatch(step, needle);
        if (match == null)
        {
            Widget[] roots = client.getWidgetRoots();
            if (roots != null)
            {
                for (Widget root : roots)
                {
                    match = findBestMatch(root, needle);
                    if (match != null)
                    {
                        break;
                    }
                }
            }
        }

        if (match != null)
        {
            drawWidget(graphics, expandToActionParent(match));
        }

        return null;
    }

    private Widget findPreferredMatch(GuideStep step, String needle)
    {
        String target = normalize(step.getTarget());

        if (target.contains("smithing interface"))
        {
            return findBestMatch(client.getWidget(InterfaceID.Smithing.UNIVERSE), needle);
        }

        return null;
    }

    private Widget findBestMatch(Widget widget, String needle)
    {
        if (widget == null || widget.isHidden())
        {
            return null;
        }

        if (matchesWidget(widget, needle))
        {
            return widget;
        }

        Widget match = findIn(widget.getChildren(), needle);
        if (match != null) return match;

        match = findIn(widget.getDynamicChildren(), needle);
        if (match != null) return match;

        match = findIn(widget.getStaticChildren(), needle);
        if (match != null) return match;

        return findIn(widget.getNestedChildren(), needle);
    }

    private Widget findIn(Widget[] widgets, String needle)
    {
        if (widgets == null)
        {
            return null;
        }

        for (Widget child : widgets)
        {
            Widget match = findBestMatch(child, needle);
            if (match != null)
            {
                return match;
            }
        }

        return null;
    }

    private static boolean matchesWidget(Widget widget, String needle)
    {
        if (containsNeedle(widget.getText(), needle) || containsNeedle(widget.getName(), needle))
        {
            return true;
        }

        String[] actions = widget.getActions();
        if (actions != null)
        {
            for (String action : actions)
            {
                if (containsNeedle(action, needle))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean containsNeedle(String value, String needle)
    {
        String normalized = normalize(stripTags(value));
        return !normalized.isEmpty() &&
            (normalized.equals(needle) || normalized.contains(needle) || needle.contains(normalized));
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

            // Avoid highlighting an entire modal/window instead of the action.
            if (bounds.width > 180 || bounds.height > 140)
            {
                break;
            }

            if (bestBounds == null ||
                (bounds.width >= bestBounds.width && bounds.height >= bestBounds.height))
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

    private static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
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
