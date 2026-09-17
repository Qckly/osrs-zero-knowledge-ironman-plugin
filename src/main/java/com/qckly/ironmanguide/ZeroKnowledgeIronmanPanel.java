package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

/**
 * Main sidebar for the guide.
 *
 * The panel intentionally follows RuneLite's native sidebar layout model:
 * one full-width vertical column, content anchored to the top, and no fixed
 * pixel-width cards or HTML layout tricks.
 */
public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private static final Color ACCENT = new Color(255, 170, 0);
    private static final Color MUTED = new Color(180, 180, 180);

    private final GuideState guideState;

    private final JLabel chapterLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel stepHeaderLabel = new JLabel();
    private final JTextArea titleText = createTextArea(Font.BOLD, 15f, Color.WHITE);
    private final JTextArea instructionText = createTextArea(Font.PLAIN, 13f, Color.LIGHT_GRAY);
    private final JTextArea whyText = createTextArea(Font.PLAIN, 12.5f, MUTED);
    private final JProgressBar progressBar = new JProgressBar();

    private final JButton previousButton = new JButton("Back");
    private final JButton doneButton = new JButton("Complete");
    private final JButton nextButton = new JButton("Next");

    public ZeroKnowledgeIronmanPanel(GuideState guideState)
    {
        super(false);
        this.guideState = guideState;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 8));

        mainPanel.add(buildHeader());
        mainPanel.add(buildCurrentStepPanel());
        mainPanel.add(buildWhyPanel());

        // Anchor the guide to the top instead of stretching its children over
        // the full sidebar height.
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        northWrapper.add(mainPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(northWrapper);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        previousButton.addActionListener(e -> guideState.previous());
        doneButton.addActionListener(e -> guideState.next());
        nextButton.addActionListener(e -> guideState.next());

        guideState.addListener(() -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private JPanel buildHeader()
    {
        JPanel header = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
        header.setOpaque(false);

        JLabel pluginTitle = new JLabel("IRONMAN GUIDE", SwingConstants.CENTER);
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setFont(pluginTitle.getFont().deriveFont(Font.BOLD, 16f));

        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setFont(chapterLabel.getFont().deriveFont(Font.PLAIN, 13f));

        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setForeground(MUTED);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 12f));

        progressBar.setMinimum(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        header.add(pluginTitle);
        header.add(chapterLabel);
        header.add(progressLabel);
        header.add(progressBar);
        return header;
    }

    private JPanel buildCurrentStepPanel()
    {
        JPanel panel = sectionPanel();
        panel.setLayout(new BorderLayout(0, 8));

        stepHeaderLabel.setForeground(ACCENT);
        stepHeaderLabel.setFont(stepHeaderLabel.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(stepHeaderLabel, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new DynamicGridLayout(0, 1, 0, 7));
        textPanel.setOpaque(false);
        textPanel.add(titleText);
        textPanel.add(instructionText);
        panel.add(textPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 3, 6, 0));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(3, 0, 0, 0));
        controls.add(previousButton);
        controls.add(doneButton);
        controls.add(nextButton);
        panel.add(controls, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildWhyPanel()
    {
        JPanel panel = sectionPanel();
        panel.setLayout(new BorderLayout(0, 7));

        JLabel heading = new JLabel("WHY THIS MATTERS");
        heading.setForeground(ACCENT);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(whyText, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel sectionPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private static JTextArea createTextArea(int style, float size, Color color)
    {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(color);
        area.setFont(area.getFont().deriveFont(style, size));
        area.setBorder(null);
        area.setMargin(new java.awt.Insets(0, 0, 0, 0));
        area.setColumns(1);
        return area;
    }

    private void refresh()
    {
        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            return;
        }

        chapterLabel.setText(step.getChapter());
        progressLabel.setText("Step " + (guideState.getCurrentIndex() + 1) + " of " + guideState.getStepCount());
        stepHeaderLabel.setText("CURRENT STEP  •  " + (guideState.getCurrentIndex() + 1) + "/" + guideState.getStepCount());

        progressBar.setMaximum(Math.max(1, guideState.getStepCount()));
        progressBar.setValue(guideState.getCurrentIndex() + 1);

        titleText.setText(step.getTitle());
        instructionText.setText(step.getInstruction());
        whyText.setText(step.getWhy());

        titleText.setCaretPosition(0);
        instructionText.setCaretPosition(0);
        whyText.setCaretPosition(0);

        previousButton.setEnabled(guideState.hasPrevious());
        nextButton.setEnabled(guideState.hasNext());
        doneButton.setEnabled(guideState.hasNext());
        doneButton.setText(guideState.hasNext() ? "Complete" : "Finished");

        revalidate();
        repaint();
    }
}
