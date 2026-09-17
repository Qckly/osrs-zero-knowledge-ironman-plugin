package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
    private static final int TEXT_WIDTH = 176;
    private static final Color ACCENT = new Color(255, 165, 0);

    private final GuideState guideState;

    private final JLabel chapterLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JLabel instructionLabel = new JLabel();
    private final JLabel whyLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton previousButton = new JButton("←");
    private final JButton doneButton = new JButton("Done");
    private final JButton nextButton = new JButton("→");

    public ZeroKnowledgeIronmanPanel(GuideState guideState)
    {
        super(false);
        this.guideState = guideState;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(8));
        content.add(buildCurrentStepCard());
        content.add(Box.createVerticalStrut(8));
        content.add(buildWhyCard());
        content.add(Box.createVerticalStrut(8));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel pluginTitle = new JLabel("IRONMAN GUIDE");
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setFont(pluginTitle.getFont().deriveFont(Font.BOLD, 13f));
        pluginTitle.setAlignmentX(CENTER_ALIGNMENT);

        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setAlignmentX(CENTER_ALIGNMENT);
        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressLabel.setForeground(Color.GRAY);
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar.setMinimum(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(TEXT_WIDTH, 10));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        header.add(pluginTitle);
        header.add(Box.createVerticalStrut(3));
        header.add(chapterLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(progressLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(progressBar);

        return header;
    }

    private JPanel buildCurrentStepCard()
    {
        JPanel card = createCompactCard();

        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        instructionLabel.setForeground(Color.LIGHT_GRAY);
        instructionLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(sectionLabel("CURRENT STEP"));
        card.add(Box.createVerticalStrut(7));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(instructionLabel);

        return card;
    }

    private JPanel buildWhyCard()
    {
        JPanel card = createCompactCard();

        whyLabel.setForeground(new Color(180, 180, 180));
        whyLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(sectionLabel("WHY"));
        card.add(Box.createVerticalStrut(6));
        card.add(whyLabel);

        return card;
    }

    private JPanel buildControls()
    {
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
        controls.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        controls.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));

        previousButton.setPreferredSize(new Dimension(42, 28));
        previousButton.setMaximumSize(new Dimension(42, 28));
        nextButton.setPreferredSize(new Dimension(42, 28));
        nextButton.setMaximumSize(new Dimension(42, 28));
        doneButton.setPreferredSize(new Dimension(84, 28));
        doneButton.setMaximumSize(new Dimension(84, 28));

        controls.add(previousButton);
        controls.add(Box.createHorizontalGlue());
        controls.add(doneButton);
        controls.add(Box.createHorizontalGlue());
        controls.add(nextButton);

        return controls;
    }

    private static JPanel createCompactCard()
    {
        return new JPanel()
        {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBackground(ColorScheme.DARKER_GRAY_COLOR);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
                    BorderFactory.createEmptyBorder(9, 9, 9, 9)
                ));
                setAlignmentX(LEFT_ALIGNMENT);
            }

            @Override
            public Dimension getMaximumSize()
            {
                Dimension preferred = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, preferred.height);
            }
        };
    }

    private static JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
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

        chapterLabel.setText(htmlCentered(escape(step.getChapter())));
        progressLabel.setText("Step " + (guideState.getCurrentIndex() + 1) + " / " + guideState.getStepCount());
        progressBar.setMaximum(Math.max(1, guideState.getStepCount()));
        progressBar.setValue(guideState.getCurrentIndex() + 1);

        titleLabel.setText(html(escape(step.getTitle())));
        instructionLabel.setText(html(escape(step.getInstruction())));
        whyLabel.setText(html(escape(step.getWhy())));

        previousButton.setEnabled(guideState.hasPrevious());
        nextButton.setEnabled(guideState.hasNext());
        doneButton.setEnabled(guideState.hasNext());
        doneButton.setText(guideState.hasNext() ? "Done" : "Finished");

        revalidate();
        repaint();
    }

    private static String html(String text)
    {
        return "<html><body style='width:" + TEXT_WIDTH + "px;margin:0'>" + text + "</body></html>";
    }

    private static String htmlCentered(String text)
    {
        return "<html><div style='width:" + TEXT_WIDTH + "px;text-align:center'>" + text + "</div></html>";
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
