package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private static final int TEXT_WIDTH = 188;
    private static final Color ACCENT = new Color(255, 165, 0);

    private final GuideState guideState;

    private final JLabel chapterLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JLabel instructionLabel = new JLabel();
    private final JLabel whyLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton previousButton = new JButton("←");
    private final JButton doneButton = new JButton("Mark done");
    private final JButton nextButton = new JButton("→");

    public ZeroKnowledgeIronmanPanel(GuideState guideState)
    {
        super(false);
        this.guideState = guideState;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildHeader());
        root.add(Box.createVerticalStrut(10));
        root.add(buildCurrentStepCard());
        root.add(Box.createVerticalStrut(10));
        root.add(buildWhyCard());
        root.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(root);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(scrollPane, BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);

        previousButton.addActionListener(e -> guideState.previous());
        nextButton.addActionListener(e -> guideState.next());
        doneButton.addActionListener(e -> guideState.next());

        guideState.addListener(() -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private JPanel buildHeader()
    {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel pluginTitle = new JLabel("ZERO KNOWLEDGE IRONMAN");
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setFont(pluginTitle.getFont().deriveFont(Font.BOLD));
        pluginTitle.setAlignmentX(CENTER_ALIGNMENT);

        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setAlignmentX(CENTER_ALIGNMENT);
        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressLabel.setForeground(Color.GRAY);
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar.setMinimum(0);
        progressBar.setStringPainted(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setPreferredSize(new Dimension(0, 12));
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        header.add(pluginTitle);
        header.add(Box.createVerticalStrut(3));
        header.add(chapterLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(progressLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(progressBar);

        return header;
    }

    private JPanel buildCurrentStepCard()
    {
        JPanel card = createCard();

        JLabel section = sectionLabel("CURRENT OBJECTIVE");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        instructionLabel.setForeground(Color.LIGHT_GRAY);

        card.add(section);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(instructionLabel);

        return card;
    }

    private JPanel buildWhyCard()
    {
        JPanel card = createCard();
        card.add(sectionLabel("WHY THIS MATTERS"));
        card.add(Box.createVerticalStrut(8));

        whyLabel.setForeground(new Color(180, 180, 180));
        card.add(whyLabel);
        return card;
    }

    private JPanel buildControls()
    {
        JPanel controls = new JPanel(new BorderLayout(6, 0));
        controls.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        previousButton.setPreferredSize(new Dimension(44, 30));
        nextButton.setPreferredSize(new Dimension(44, 30));
        doneButton.setPreferredSize(new Dimension(0, 30));

        JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        middle.setOpaque(false);
        middle.add(doneButton);

        controls.add(previousButton, BorderLayout.WEST);
        controls.add(middle, BorderLayout.CENTER);
        controls.add(nextButton, BorderLayout.EAST);
        return controls;
    }

    private static JPanel createCard()
    {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private static JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void refresh()
    {
        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            return;
        }

        chapterLabel.setText(step.getChapter());
        progressLabel.setText("Step " + (guideState.getCurrentIndex() + 1) + " / " + guideState.getStepCount());
        progressBar.setMaximum(Math.max(1, guideState.getStepCount()));
        progressBar.setValue(guideState.getCurrentIndex() + 1);

        titleLabel.setText(html("<b>" + escape(step.getTitle()) + "</b>"));
        instructionLabel.setText(html(escape(step.getInstruction())));
        whyLabel.setText(html(escape(step.getWhy())));

        previousButton.setEnabled(guideState.hasPrevious());
        nextButton.setEnabled(guideState.hasNext());
        doneButton.setEnabled(guideState.hasNext());
        doneButton.setText(guideState.hasNext() ? "Mark done" : "Final step");

        revalidate();
        repaint();
    }

    private static String html(String text)
    {
        return "<html><body style='width:" + TEXT_WIDTH + "px'>" + text + "</body></html>";
    }

    private static String escape(String value)
    {
        if (value == null)
        {
            return "";
        }

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
