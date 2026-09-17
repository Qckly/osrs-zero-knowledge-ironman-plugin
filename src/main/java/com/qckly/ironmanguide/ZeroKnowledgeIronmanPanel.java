package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
    private static final int CONTENT_WIDTH = 196;
    private static final int TEXT_WIDTH = 166;
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
        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        content.add(centered(buildHeader()));
        content.add(Box.createVerticalStrut(8));
        content.add(centered(buildCurrentStepCard()));
        content.add(Box.createVerticalStrut(8));
        content.add(centered(buildWhyCard()));
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
        JPanel header = fixedWidthPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel pluginTitle = new JLabel("IRONMAN GUIDE", SwingConstants.CENTER);
        pluginTitle.setForeground(Color.WHITE);
        pluginTitle.setFont(pluginTitle.getFont().deriveFont(Font.BOLD, 13f));
        pluginTitle.setAlignmentX(CENTER_ALIGNMENT);
        pluginTitle.setMaximumSize(new Dimension(CONTENT_WIDTH, pluginTitle.getPreferredSize().height));

        chapterLabel.setForeground(Color.LIGHT_GRAY);
        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chapterLabel.setAlignmentX(CENTER_ALIGNMENT);
        chapterLabel.setMaximumSize(new Dimension(CONTENT_WIDTH, 40));

        progressLabel.setForeground(Color.GRAY);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setMaximumSize(new Dimension(CONTENT_WIDTH, progressLabel.getPreferredSize().height));

        progressBar.setMinimum(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(CONTENT_WIDTH, 10));
        progressBar.setMinimumSize(new Dimension(CONTENT_WIDTH, 10));
        progressBar.setMaximumSize(new Dimension(CONTENT_WIDTH, 10));
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
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        outer.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));

        JPanel controls = new JPanel(new GridLayout(1, 3, 6, 0));
        controls.setOpaque(false);
        controls.setPreferredSize(new Dimension(CONTENT_WIDTH, 30));
        controls.setMaximumSize(new Dimension(CONTENT_WIDTH, 30));

        controls.add(previousButton);
        controls.add(doneButton);
        controls.add(nextButton);

        JPanel centered = new JPanel();
        centered.setOpaque(false);
        centered.setLayout(new BoxLayout(centered, BoxLayout.X_AXIS));
        centered.add(Box.createHorizontalGlue());
        centered.add(controls);
        centered.add(Box.createHorizontalGlue());

        outer.add(centered, BorderLayout.CENTER);
        return outer;
    }

    private static JPanel createCompactCard()
    {
        JPanel card = fixedWidthPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
        return card;
    }

    private static JPanel fixedWidthPanel()
    {
        return new JPanel()
        {
            @Override
            public Dimension getPreferredSize()
            {
                Dimension preferred = super.getPreferredSize();
                return new Dimension(CONTENT_WIDTH, preferred.height);
            }

            @Override
            public Dimension getMinimumSize()
            {
                Dimension preferred = getPreferredSize();
                return new Dimension(CONTENT_WIDTH, preferred.height);
            }

            @Override
            public Dimension getMaximumSize()
            {
                Dimension preferred = getPreferredSize();
                return new Dimension(CONTENT_WIDTH, preferred.height);
            }
        };
    }

    private static JPanel centered(JPanel child)
    {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setAlignmentX(CENTER_ALIGNMENT);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(child);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, child.getPreferredSize().height));
        return wrapper;
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
