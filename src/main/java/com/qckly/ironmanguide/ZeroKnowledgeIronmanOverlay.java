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
 * Compact in-game objective overlay driven by structured guide data.
 *
 * During Tutorial Island QA the overlay is forced visible while the client is
 * detected on Tutorial Island, so persisted RuneLite config cannot make the
 * guide appear to randomly disappear between dev restarts.
 */
public final class ZeroKnowledgeIronmanOverlay extends OverlayPanel
{
    private static final Color BACKGROUND = new Color(30, 30, 30, 215);
    private static final Color TITLE = Color.WHITE;
    private static final Color ACTION = new Color(255, 170, 0);
    private static final Color LABEL = new Color(220, 220, 220);
    private static final Color REQUIREMENT_MISSING = new Color(255, 90, 90);
    private static final Color REQUIREMENT_OK = new Color(100, 205, 120);
    private static final Color TARGET = new Color(235, 235, 235);
    private static final Color MUTED = new Color(165, 165, 165);

    private static final int OVERLAY_WIDTH = 205;
    private static final int WRAP_AT = 30;

    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;
    private final TutorialStateTracker tutorialStateTracker;

    public ZeroKnowledgeIronmanOverlay(
        GuideState guideState,
        ZeroKnowledgeIronmanConfig config,
        TutorialStateTracker tutorialStateTracker)
    {
        this.guideState = guideState;
        this.config = config;
        this.tutorialStateTracker = tutorialStateTracker;

        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(OVERLAY_WIDTH, 0));
        panelComponent.setBackgroundColor(BACKGROUND);
        panelComponent.setBorder(new Rectangle(7, 7, 7, 7));
        panelComponent.setGap(new Point(0, 3));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        boolean forceVisibleForTutorialQa =
            tutorialStateTracker != null && tutorialStateTracker.isOnTutorialIsland();

        if (!forceVisibleForTutorialQa && !config.showObjectiveOverlay())
        {
            return null;
        }

        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text(step.getTitle())
                .color(TITLE)
                .build()
        );

        for (String line : wrap(firstSentence(step.getInstruction()), WRAP_AT))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left(line)
                    .leftColor(ACTION)
                    .build()
            );
        }

        if (hasText(step.getTarget()))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left("Target:")
                    .leftColor(LABEL)
                    .build()
            );

            for (String line : wrap(step.getTarget(), WRAP_AT))
            {
                panelComponent.getChildren().add(
                    LineComponent.builder()
                        .left(line)
                        .leftColor(TARGET)
                        .build()
                );
            }
        }

        if (hasText(step.getRequirement()))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left("Requirements:")
                    .leftColor(LABEL)
                    .build()
            );

            for (String requirement : step.getRequirement().split("\\+|\\n"))
            {
                String part = requirement.trim();
                if (part.isEmpty())
                {
                    continue;
                }

                RequirementEvaluator.Status status =
                    RequirementEvaluator.evaluate(part, tutorialStateTracker);

                Color requirementColor = status == RequirementEvaluator.Status.SATISFIED
                    ? REQUIREMENT_OK
                    : status == RequirementEvaluator.Status.UNSATISFIED
                        ? REQUIREMENT_MISSING
                        : LABEL;

                String display = status == RequirementEvaluator.Status.SATISFIED
                    ? "✓ " + part
                    : part;

                for (String line : wrap(display, WRAP_AT))
                {
                    panelComponent.getChildren().add(
                        LineComponent.builder()
                            .left(line)
                            .leftColor(requirementColor)
                            .build()
                    );
                }
            }
        }

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left((step.isOptional() ? "Optional • " : "") + "Step " +
                    (guideState.getCurrentIndex() + 1) + " / " + guideState.getStepCount())
                .leftColor(MUTED)
                .build()
        );

        if (forceVisibleForTutorialQa && !config.showObjectiveOverlay())
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left("QA: overlay forced on")
                    .leftColor(MUTED)
                    .build()
            );
        }

        return super.render(graphics);
    }

    private static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
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
