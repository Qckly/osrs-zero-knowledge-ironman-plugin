package com.qckly.ironmanguide;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Quest-Helper-style dialogue guidance.
 *
 * Instead of drawing a large rectangle around dialogue rows, the actionable
 * dialogue text itself is recoloured. If DIALOGUE_CHOICE is supplied only that
 * option is highlighted. "Click here to continue" is highlighted universally.
 */
public final class DialogueGuidanceOverlay extends Overlay
{
    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public DialogueGuidanceOverlay(
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

        boolean highlightedOptions = highlightDialogueOptions();
        if (!highlightedOptions)
        {
            highlightContinueWidgets();
        }

        return null;
    }

    private boolean highlightDialogueOptions()
    {
        Widget options = client.getWidget(InterfaceID.Chatmenu.OPTIONS);
        if (!isVisible(options))
        {
            return false;
        }

        GuideStep step = guideState.getCurrentStep();
        String desired = step == null ? "" : normalize(step.getDialogueChoice());

        boolean highlighted = false;
        Widget[] children = options.getChildren();
        if (children == null)
        {
            return false;
        }

        for (Widget child : children)
        {
            if (!isVisible(child) || !hasText(child.getText()))
            {
                continue;
            }

            if (!desired.isEmpty())
            {
                String optionText = normalize(stripTags(child.getText()));
                if (!optionText.contains(desired) && !desired.contains(optionText))
                {
                    continue;
                }
            }

            highlightText(child);
            highlighted = true;
        }

        return highlighted;
    }

    private void highlightContinueWidgets()
    {
        Widget[] roots = client.getWidgetRoots();
        if (roots == null)
        {
            return;
        }

        for (Widget root : roots)
        {
            findAndHighlightContinue(root);
        }
    }

    private void findAndHighlightContinue(Widget widget)
    {
        if (widget == null || widget.isHidden())
        {
            return;
        }

        String text = widget.getText();
        if (hasText(text) && normalize(stripTags(text)).contains("click here to continue"))
        {
            highlightText(widget);
        }

        recurse(widget.getChildren());
        recurse(widget.getDynamicChildren());
        recurse(widget.getStaticChildren());
        recurse(widget.getNestedChildren());
    }

    private void recurse(Widget[] widgets)
    {
        if (widgets == null)
        {
            return;
        }

        for (Widget child : widgets)
        {
            findAndHighlightContinue(child);
        }
    }

    private void highlightText(Widget widget)
    {
        widget.setTextColor(config.textHighlightColor().getRGB());
    }

    private static boolean isVisible(Widget widget)
    {
        return widget != null && !widget.isHidden();
    }

    private static boolean hasText(String text)
    {
        return text != null && !text.trim().isEmpty();
    }

    private static String stripTags(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]+>", " ");
    }

    private static String normalize(String text)
    {
        return text == null
            ? ""
            : text.replace("&nbsp;", " ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
