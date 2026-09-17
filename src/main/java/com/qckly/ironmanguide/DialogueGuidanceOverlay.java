package com.qckly.ironmanguide;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Highlights actionable dialogue UI.
 *
 * V1:
 * - highlights "Click here to continue" wherever that widget appears;
 * - highlights visible dialogue-option rows.
 *
 * A later guide-data pass can add an exact recommended dialogue choice per step
 * so only the correct option is emphasized.
 */
public final class DialogueGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color FILL = new Color(0, 220, 255, 30);

    private final Client client;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public DialogueGuidanceOverlay(
        Client client,
        ZeroKnowledgeIronmanConfig config,
        TutorialStateTracker tutorialStateTracker)
    {
        this.client = client;
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

        boolean drewOptions = drawDialogueOptions(graphics);
        if (!drewOptions)
        {
            drawContinueWidgets(graphics);
        }

        return null;
    }

    private boolean drawDialogueOptions(Graphics2D graphics)
    {
        Widget options = client.getWidget(InterfaceID.Chatmenu.OPTIONS);
        if (!isVisible(options))
        {
            return false;
        }

        boolean drew = false;
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

            drawWidgetBounds(graphics, child);
            drew = true;
        }

        return drew;
    }

    private void drawContinueWidgets(Graphics2D graphics)
    {
        Widget[] roots = client.getWidgetRoots();
        if (roots == null)
        {
            return;
        }

        for (Widget root : roots)
        {
            findAndDrawContinue(graphics, root);
        }
    }

    private void findAndDrawContinue(Graphics2D graphics, Widget widget)
    {
        if (widget == null || widget.isHidden())
        {
            return;
        }

        String text = widget.getText();
        if (hasText(text) && normalize(text).contains("click here to continue"))
        {
            drawWidgetBounds(graphics, widget);
        }

        Widget[] children = widget.getChildren();
        if (children != null)
        {
            for (Widget child : children)
            {
                findAndDrawContinue(graphics, child);
            }
        }

        Widget[] dynamicChildren = widget.getDynamicChildren();
        if (dynamicChildren != null)
        {
            for (Widget child : dynamicChildren)
            {
                findAndDrawContinue(graphics, child);
            }
        }

        Widget[] staticChildren = widget.getStaticChildren();
        if (staticChildren != null)
        {
            for (Widget child : staticChildren)
            {
                findAndDrawContinue(graphics, child);
            }
        }

        Widget[] nestedChildren = widget.getNestedChildren();
        if (nestedChildren != null)
        {
            for (Widget child : nestedChildren)
            {
                findAndDrawContinue(graphics, child);
            }
        }
    }

    private static void drawWidgetBounds(Graphics2D graphics, Widget widget)
    {
        Rectangle bounds = widget.getBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
        {
            return;
        }

        graphics.setColor(FILL);
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        graphics.setColor(CYAN);
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    private static boolean isVisible(Widget widget)
    {
        return widget != null && !widget.isHidden();
    }

    private static boolean hasText(String text)
    {
        return text != null && !text.trim().isEmpty();
    }

    private static String normalize(String text)
    {
        return text == null
            ? ""
            : text.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }
}
