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
        "settings",
        "account management"
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

        boolean spellTarget = isSpellTarget(step, needle);

        Widget match = findPreferredMatch(step, needle);
        if (match == null && !spellTarget)
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
            drawWidget(graphics, spellTarget ? match : expandToActionParent(match));
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

        if (target.contains("magic interface") || isSpellTarget(step, needle))
        {
            return findExactActionMatch(
                client.getWidget(InterfaceID.MagicSpellbook.UNIVERSE),
                needle
            );
        }

        return null;
    }

    private boolean isSpellTarget(GuideStep step, String needle)
    {
        String target = normalize(step.getTarget());
        String title = normalize(step.getTitle());

        return target.contains("magic interface")
            || title.contains("cast ")
            || title.contains("teleport")
            || needle.contains("strike")
            || needle.contains("bolt")
            || needle.contains("blast")
            || needle.contains("wave")
            || needle.contains("surge")
            || needle.contains("teleport");
    }

    private Widget findExactActionMatch(Widget widget, String needle)
    {
        if (widget == null || widget.isHidden())
        {
            return null;
        }

        if (exactWidgetMatch(widget, needle))
        {
            return widget;
        }

        Widget match = findExactIn(widget.getChildren(), needle);
        if (match != null) return match;

        match = findExactIn(widget.getDynamicChildren(), needle);
        if (match != null) return match;

        match = findExactIn(widget.getStaticChildren(), needle);
        if (match != null) return match;

        return findExactIn(widget.getNestedChildren(), needle);
    }

    private Widget findExactIn(Widget[] widgets, String needle)
    {
        if (widgets == null)
        {
            return null;
        }

        for (Widget child : widgets)
        {
            Widget match = findExactActionMatch(child, needle);
            if (match != null)
            {
                return match;
            }
        }

        return null;
    }

    private static boolean exactWidgetMatch(Widget widget, String needle)
    {
        String name = normalize(stripTags(widget.getName()));
        String text = normalize(stripTags(widget.getText()));

        if (needle.equals(name) || needle.equals(text))
        {
            return true;
        }

        String[] actions = widget.getActions();
        if (actions == null)
        {
            return false;
        }

        for (String action : actions)
        {
            String normalizedAction = normalize(stripTags(action));
            if (normalizedAction.equals(needle)
                || normalizedAction.equals("cast " + needle)
                || normalizedAction.endsWith(" " + needle))
            {
                return true;
            }
        }

        return false;
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
        // Action/name matches are strong signals and are safe for buttons,
        // spells, production choices and other clickable interface elements.
        if (containsNeedle(widget.getName(), needle) || actionsContain(widget, needle))
        {
            return true;
        }

        // Plain text alone is not enough: NPC/dialogue text frequently mentions
        // interface names (for example "Account Management") and must never be
        // treated as a clickable UI target. Only accept a text match when this
        // widget or a small parent widget is actually actionable.
        if (containsNeedle(widget.getText(), needle))
        {
            if (hasActions(widget))
            {
                return true;
            }

            Widget parent = widget.getParent();
            if (parent != null)
            {
                Rectangle bounds = safeBounds(parent);
                if (bounds != null
                    && bounds.width <= 220
                    && bounds.height <= 160
                    && hasActions(parent))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean actionsContain(Widget widget, String needle)
    {
        String[] actions = widget.getActions();
        if (actions == null)
        {
            return false;
        }

        for (String action : actions)
        {
            if (containsNeedle(action, needle))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean hasActions(Widget widget)
    {
        String[] actions = widget.getActions();
        if (actions == null)
        {
            return false;
        }

        for (String action : actions)
        {
            if (action != null && !action.trim().isEmpty())
            {
                return true;
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
