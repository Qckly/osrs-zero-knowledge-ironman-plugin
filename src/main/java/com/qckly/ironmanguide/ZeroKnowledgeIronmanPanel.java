package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private final List<GuideStep> steps = GuideRepository.getSteps();
    private final JLabel chapterLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JLabel instructionLabel = new JLabel();
    private final JLabel whyLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton previousButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");

    private int currentIndex = 0;

    public ZeroKnowledgeIronmanPanel()
    {
        super(false);
        setLayout(new BorderLayout(0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 4));
        header.setOpaque(false);

        JLabel pluginTitle = new JLabel("ZERO KNOWLEDGE IRONMAN");
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setHorizontalAlignment(SwingConstants.CENTER);

        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setForeground(Color.GRAY);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar.setStringPainted(false);
        progressBar.setMinimum(0);

        header.add(pluginTitle);
        header.add(chapterLabel);
        header.add(progressLabel);
        header.add(progressBar);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 8));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        titleLabel.setForeground(Color.WHITE);
        instructionLabel.setForeground(Color.LIGHT_GRAY);
        whyLabel.setForeground(Color.GRAY);

        content.add(titleLabel);
        content.add(instructionLabel);
        content.add(whyLabel);

        JPanel controls = new JPanel(new GridLayout(1, 2, 8, 0));
        controls.setOpaque(false);
        controls.setPreferredSize(new Dimension(0, 32));
        controls.add(previousButton);
        controls.add(nextButton);

        previousButton.addActionListener(e -> showStep(currentIndex - 1));
        nextButton.addActionListener(e -> showStep(currentIndex + 1));

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        showStep(0);
    }

    private void showStep(int index)
    {
        if (steps.isEmpty())
        {
            return;
        }

        currentIndex = Math.max(0, Math.min(index, steps.size() - 1));
        GuideStep step = steps.get(currentIndex);

        chapterLabel.setText(step.getChapter());
        progressLabel.setText("Step " + (currentIndex + 1) + " / " + steps.size());
        progressBar.setMaximum(steps.size());
        progressBar.setValue(currentIndex + 1);

        titleLabel.setText(html("<b>" + escape(step.getTitle()) + "</b>"));
        instructionLabel.setText(html(escape(step.getInstruction())));
        whyLabel.setText(html("<span style='color:#9e9e9e'><b>Why:</b> " + escape(step.getWhy()) + "</span>"));

        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < steps.size() - 1);
    }

    private static String html(String text)
    {
        return "<html><body style='width:190px'>" + text + "</body></html>";
    }

    private static String escape(String value)
    {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
