package com.qckly.ironmanguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

/**
 * Main sidebar for the guide.
 *
 * Important: this intentionally uses PluginPanel's native wrapper/scroll pane.
 * RuneLite reserves PANEL_WIDTH for content and SCROLLBAR_WIDTH for the scrollbar.
 * Building another JScrollPane inside a super(false) PluginPanel makes the usable
 * width wrong and causes cards/buttons to run into the navigation icon strip.
 */
public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private static final Color ACCENT = new Color(255, 170, 0);
    private static final Color MUTED = new Color(180, 180, 180);

    private final GuideState guideState;

    private final JLabel chapterLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel stepHeaderLabel = new JLabel();
    private final JTextArea titleText = createTextArea(Font.BOLD, 14f, Color.WHITE);
    private final JTextArea instructionText = createTextArea(Font.PLAIN, 12.5f, Color.LIGHT_GRAY);
    private final JTextArea whyText = createTextArea(Font.PLAIN, 12.5f, MUTED);
    private final JProgressBar progressBar = new JProgressBar();

    private final JButton previousButton = new JButton("Back");
    private final JButton doneButton = new JButton("Done");
    private final JButton nextButton = new JButton("Next");

    public ZeroKnowledgeIronmanPanel(GuideState guideState)
    {
        // Use RuneLite's native PluginPanel wrapping. It handles the 225px content
        // area + 17px scrollbar reservation correctly for the sidebar.
        super();
        this.guideState = guideState;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new DynamicGridLayout(0, 1, 0, 9));

        add(buildHeader());
        add(buildCurrentStepPanel());
        add(buildWhyPanel());

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
        header.setBorder(new EmptyBorder(0, 2, 1, 2));

        JLabel pluginTitle = new JLabel("IRONMAN GUIDE", SwingConstants.CENTER);
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setFont(pluginTitle.getFont().deriveFont(Font.BOLD, 15f));

        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setFont(chapterLabel.getFont().deriveFont(Font.PLAIN, 12f));

        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setForeground(MUTED);
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 11f));

        progressBar.setMinimum(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 7));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));

        header.add(pluginTitle);
        header.add(chapterLabel);
        header.add(progressLabel);
        header.add(progressBar);
        return header;
    }

    private JPanel buildCurrentStepPanel()
    {
        JPanel panel = sectionPanel();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 8));

        stepHeaderLabel.setForeground(ACCENT);
        stepHeaderLabel.setFont(stepHeaderLabel.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(stepHeaderLabel);

        panel.add(titleText);
        panel.add(instructionText);

        JPanel controls = new JPanel(new GridLayout(1, 3, 5, 0));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(2, 0, 0, 0));
        controls.add(previousButton);
        controls.add(doneButton);
        controls.add(nextButton);
        panel.add(controls);

        return panel;
    }

    private JPanel buildWhyPanel()
    {
        JPanel panel = sectionPanel();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 7));

        JLabel heading = new JLabel("WHY THIS MATTERS");
        heading.setForeground(ACCENT);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(heading);
        panel.add(whyText);
        return panel;
    }

    private static JPanel sectionPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            new EmptyBorder(10, 11, 10, 11)
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
        stepHeaderLabel.setText("CURRENT STEP  ·  " + (guideState.getCurrentIndex() + 1) + "/" + guideState.getStepCount());

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
        doneButton.setText(guideState.hasNext() ? "Done" : "Finished");

        revalidate();
        repaint();
    }
}
