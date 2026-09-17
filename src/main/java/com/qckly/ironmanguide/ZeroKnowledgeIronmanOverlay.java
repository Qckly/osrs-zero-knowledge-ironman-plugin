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

public final class ZeroKnowledgeIronmanOverlay extends OverlayPanel
{
    private static final Color ACCENT = new Color(255, 165, 0);
    private static final Color BODY = new Color(220, 220, 220);
    private static final Color MUTED = new Color(160, 160, 160);
    private static final int WRAP_AT = 39;

    private final GuideState guideState;
    private final ZeroKnowledgeIronmanConfig config;

    public ZeroKnowledgeIronmanOverlay(GuideState guideState, ZeroKnowledgeIronmanConfig config)
    {
        this.guideState = guideState;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(255, 0));
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

        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text("IRONMAN GUIDE  •  " + (guideState.getCurrentIndex() + 1) + "/" + guideState.getStepCount())
                .color(ACCENT)
                .build()
        );

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(step.getTitle())
                .leftColor(Color.WHITE)
                .build()
        );

        for (String line : wrap(step.getInstruction(), WRAP_AT))
        {
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left(line)
                    .leftColor(BODY)
                    .build()
            );
        }

        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("Open the side panel for details")
                .leftColor(MUTED)
                .build()
        );

        return super.render(graphics);
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
