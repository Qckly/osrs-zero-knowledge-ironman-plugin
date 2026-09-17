package com.qckly.ironmanguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Compact in-game objective overlay inspired by Quest Helper's information hierarchy.
 * The sidebar contains the full explanation; this box only tells the player what to
 * do now and what is needed for the current step.
 */
public final class ZeroKnowledgeIronmanOverlay extends OverlayPanel
{
    private static final Color BACKGROUND = new Color(30, 30, 30, 215);
    private static final Color TITLE = Color.WHITE;
    private static final Color ACTION = new Color(255, 170, 0);
    private static final Color LABEL = new Color(220, 220, 220);
    private static final Color REQUIREMENT = new Color(255, 90, 90);
    private static final Color MUTED = new Color(165, 165, 165);

    private static final int OVERLAY_WIDTH = 205;
    private static final int WRAP_AT = 30;

    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;

    public ZeroKnowledgeIronmanOverlay(GuideState guideState, ZeroKnowledgeIronmanConfig config)
    {
        this.guideState = guideState;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(OVERLAY_WIDTH, 0));
        panelComponent.setBackgroundColor(BACKGROUND);
        panelComponent.setBorder(new Rectangle(7, 7, 7, 7));
        panelComponent.setGap(new Point(0, 3));
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

        // Main objective title.
        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text(step.getTitle())
                .color(TITLE)
                .build()
        );

        // One concise immediate-action sentence in orange.
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

        // Visible Quest Helper-style requirements section. These are intentionally
        // lightweight until GuideStep gains structured requirement/item fields.
        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("Requirements:")
                .leftColor(LABEL)
                .build()
        );

        for (String requirement : requirementsFor(step))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left(requirement)
                    .leftColor(REQUIREMENT)
                    .build()
            );
        }

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("Step " + (guideState.getCurrentIndex() + 1) + " / " + guideState.getStepCount())
                .leftColor(MUTED)
                .build()
        );

        return super.render(graphics);
    }

    private static List<String> requirementsFor(GuideStep step)
    {
        List<String> requirements = new ArrayList<>();

        switch (step.getId())
        {
            case "ch01-step-002":
                requirements.add("Standard Ironman selected");
                break;
            case "ch01-step-005":
                requirements.add("1 x Spade");
                requirements.add("1 x Hammer");
                requirements.add("Coins");
                break;
            case "ch01-step-010":
                requirements.add("7 x Normal logs");
                requirements.add("4 x Ashes");
                break;
            case "ch01-step-011":
                requirements.add("15 Firemaking");
                break;
            case "ch01-step-012":
                requirements.add("Knife");
                requirements.add("~1,000 arrow shafts");
                break;
            case "ch01-step-016":
                requirements.add("Spade");
                requirements.add("Basic food");
                requirements.add("Coins");
                break;
            default:
                requirements.add("None");
                break;
        }

        return requirements;
    }

    private static String firstSentence(String text)
    {
        if (text == null)
        {
            return "";
        }

        String trimmed = text.trim();
        int fullStop = trimmed.indexOf('.');
        if (fullStop >= 0)
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
