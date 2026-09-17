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
 * Highlights actionable top-level interface tabs for guide steps such as
 * "Open the Quest List", "Open Skills", "Open Inventory", etc.
 */
public final class InterfaceGuidanceOverlay extends Overlay
{
    private static final Color CYAN = new Color(0, 220, 255);
    private static final Color FILL = new Color(0, 220, 255, 35);

    private final Client client;
    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public InterfaceGuidanceOverlay(
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

        String key = step.getUiTarget() != null
            ? step.getUiTarget().toLowerCase()
            : (step.getTitle() + " " + step.getTarget() + " " + step.getInstruction()).toLowerCase();

        if (key.contains("combat options") || key.equals("combat"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE0),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE0));
        }
        else if (key.contains("quest list"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE2),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE2));
        }
        else if (key.contains("skills") || key.contains("stats"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE1),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE1));
        }
        else if (key.contains("inventory"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE3),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE3));
        }
        else if (key.contains("equipment"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE4),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE4));
        }
        else if (key.contains("prayer"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE5),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE5));
        }
        else if (key.contains("magic") || key.contains("spell"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE6),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE6));
        }
        else if (key.contains("friends list") || key.equals("friends"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE9),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE9));
        }
        else if (key.contains("settings") || key.contains("options"))
        {
            drawFirstVisible(graphics,
                client.getWidget(InterfaceID.Toplevel.STONE11),
                client.getWidget(InterfaceID.ToplevelOsrsStretch.STONE11));
        }

        return null;
    }

    private void drawFirstVisible(Graphics2D graphics, Widget... widgets)
    {
        if (widgets == null)
        {
            return;
        }

        for (Widget widget : widgets)
        {
            if (widget == null || widget.isHidden())
            {
                continue;
            }

            Rectangle bounds = widget.getBounds();
            if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
            {
                continue;
            }

            graphics.setColor(FILL);
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            graphics.setColor(CYAN);
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
            return;
        }
    }
}
