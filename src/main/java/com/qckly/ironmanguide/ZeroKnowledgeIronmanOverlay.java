package com.qckly.ironmanguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Compact in-game objective overlay.
 *
 * This intentionally follows the information hierarchy that makes Quest Helper
 * comfortable to use: the current task is the title, the immediate instruction
 * is underneath, and secondary guide prose stays out of the game view.
 */
public final class ZeroKnowledgeIronmanOverlay extends OverlayPanel
{
    private static final Color OBJECTIVE = Color.WHITE;
    private static final Color ACTION = new Color(255, 170, 0);
    private static final Color LABEL = new Color(220, 220, 220);
    private static final Color MUTED = new Color(170, 170, 170);

    // Quest Helper-style compact overlay rather than a wide paragraph panel.
    private static final int OVERLAY_WIDTH = 190;
    private static final int WRAP_AT = 27;

    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;

    public ZeroKnowledgeIronmanOverlay(GuideState guideState, ZeroKnowledgeIronmanConfig config)
    {
        this.guideState = guideState;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(OVERLAY_WIDTH, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showObjectiveOverlay())
        {
            return null;
        }

        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();

        // The task itself is the visual headline, just like a Quest Helper step.
        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text(step.getTitle())
                .color(OBJECTIVE)
                .build()
        );

        // Show only the immediate action in the game view. Long explanations belong
        // in the sidebar. This prevents the overlay from becoming a wall of text.
        String actionText = firstSentence(step.getInstruction());
        for (String line : wrap(actionText, WRAP_AT))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left(line)
                    .leftColor(ACTION)
                    .build()
            );
        }

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("Step")
                .leftColor(LABEL)
                .right((guideState.getCurrentIndex() + 1) + " / " + guideState.getStepCount())
                .rightColor(MUTED)
                .build()
        );

        return super.render(graphics);
    }

    private static String firstSentence(String text)
    {
        if (text == null)
        {
            return "";
        }

        String trimmed = text.trim();
        int fullStop = trimmed.indexOf('.');
        if (fullStop >= 0 && fullStop + 1 < trimmed.length())
        {
            return trimmed.substring(0, fullStop + 1);
        }

        return trimmed;
    }

    private static List<String> wrap(String text, int maxChars)
    {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty())
        {
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words)
        {
            if (current.length() > 0 && current.length() + 1 + word.length() > maxChars)
            {
                lines.add(current.toString());
                current.setLength(0);
            }

            if (current.length() > 0)
            {
                current.append(' ');
            }
            current.append(word);
        }

        if (current.length() > 0)
        {
            lines.add(current.toString());
        }

        return lines;
    }
}
