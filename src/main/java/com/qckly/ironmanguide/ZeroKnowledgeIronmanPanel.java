package com.qckly.ironmanguide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Quest-helper-inspired route panel driven by structured guide data.
 */
public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private static final Color ORANGE = new Color(234, 145, 0);
    private static final Color ORANGE_TEXT = new Color(255, 174, 0);
    private static final Color MUTED = new Color(175, 175, 175);
    private static final Color COMPLETED = new Color(135, 135, 135);
    private static final Color RED = new Color(255, 65, 65);
    private static final Color GREEN = new Color(100, 205, 120);

    private final GuideState guideState;
    private final TutorialStateTracker tutorialStateTracker;
    private final JProgressBar progressBar = new JProgressBar();

    public ZeroKnowledgeIronmanPanel(GuideState guideState, TutorialStateTracker tutorialStateTracker)
    {
        super();
        this.guideState = guideState;
        this.tutorialStateTracker = tutorialStateTracker;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setLayout(new DynamicGridLayout(0, 1, 0, 5));

        guideState.addListener(() -> SwingUtilities.invokeLater(this::refresh));
        tutorialStateTracker.addListener(() -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private void refresh()
    {
        removeAll();

        GuideStep step = guideState.getCurrentStep();
        if (step == null)
        {
            revalidate();
            repaint();
            return;
        }

        add(buildHeader(step));
        add(buildLiveStateBlock());
        add(buildStateBlock(step));

        if (hasText(step.getTarget()))
        {
            add(buildSingleValueBlock("Target:", step.getTarget(), Color.WHITE, Font.BOLD));
        }

        if (hasText(step.getRequirement()))
        {
            add(buildRequirementsBlock(step.getRequirement()));
        }

        if (hasText(step.getCompleteWhen()))
        {
            add(buildSingleValueBlock("Complete when:", step.getCompleteWhen(), GREEN, Font.PLAIN));
        }

        if (hasText(step.getWhy()))
        {
            add(buildSingleValueBlock("Why this matters:", step.getWhy(), Color.LIGHT_GRAY, Font.PLAIN));
        }

        add(buildSectionBanner(sectionName(step.getChapter())));
        add(buildActionBlock(step));
        add(buildSectionSteps(step));
        add(buildControls());

        revalidate();
        repaint();
    }

    private JPanel buildHeader(GuideStep step)
    {
        JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 2));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(1, 1, 4, 1));

        JLabel title = new JLabel("IRONMAN GUIDE", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));

        JLabel chapter = new JLabel(step.getChapter(), SwingConstants.CENTER);
        chapter.setForeground(Color.LIGHT_GRAY);
        chapter.setFont(chapter.getFont().deriveFont(Font.PLAIN, 13.5f));

        JLabel progress = new JLabel(
            "Step " + (guideState.getCurrentIndex() + 1) + " of " + guideState.getStepCount(),
            SwingConstants.CENTER
        );
        progress.setForeground(MUTED);
        progress.setFont(progress.getFont().deriveFont(Font.PLAIN, 12.5f));

        progressBar.setMinimum(0);
        progressBar.setMaximum(Math.max(1, guideState.getStepCount()));
        progressBar.setValue(guideState.getCurrentIndex() + 1);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 7));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));

        panel.add(title);
        panel.add(chapter);
        panel.add(progress);
        panel.add(progressBar);
        return panel;
    }

    private JPanel buildLiveStateBlock()
    {
        JPanel panel = questHelperBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        panel.add(label("Live game state", Color.LIGHT_GRAY, Font.PLAIN, 13f));

        String status;
        Color statusColor;
        if (!tutorialStateTracker.isLoggedIn())
        {
            status = "Not logged in";
            statusColor = MUTED;
        }
        else if (tutorialStateTracker.isOnTutorialIsland())
        {
            status = "Tutorial Island detected";
            statusColor = GREEN;
        }
        else
        {
            status = "Outside Tutorial Island";
            statusColor = ORANGE_TEXT;
        }

        JPanel statusStrip = valueStripPanel();
        statusStrip.add(textArea(status, statusColor, Font.BOLD, 13.5f), BorderLayout.CENTER);
        panel.add(statusStrip);

        panel.add(valueStrip("Tutorial progress: " + tutorialStateTracker.getTutorialProgress()));
        panel.add(valueStrip("Region: " + tutorialStateTracker.getRegionId()));
        return panel;
    }

    private JPanel buildStateBlock(GuideStep step)
    {
        JPanel panel = questHelperBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        panel.add(label("Route State", Color.LIGHT_GRAY, Font.PLAIN, 13f));
        panel.add(valueStrip("Standard Ironman"));
        panel.add(valueStrip((step.isOptional() ? "Optional: " : "Active: ") + sectionName(step.getChapter())));
        return panel;
    }

    private JPanel buildRequirementsBlock(String requirement)
    {
        JPanel panel = questHelperBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
        panel.add(label("Requirements:", Color.LIGHT_GRAY, Font.PLAIN, 13f));

        String[] parts = requirement.split("\\+|\\n");
        for (String rawPart : parts)
        {
            String part = rawPart.trim();
            if (part.isEmpty())
            {
                continue;
            }

            RequirementEvaluator.Status status =
                RequirementEvaluator.evaluate(part, tutorialStateTracker);

            Color color = status == RequirementEvaluator.Status.SATISFIED
                ? GREEN
                : status == RequirementEvaluator.Status.UNSATISFIED
                    ? RED
                    : Color.LIGHT_GRAY;

            String prefix = status == RequirementEvaluator.Status.SATISFIED ? "✓ " : "";
            JPanel strip = valueStripPanel();
            strip.add(textArea(prefix + part, color, Font.BOLD, 13.5f), BorderLayout.CENTER);
            panel.add(strip);
        }

        return panel;
    }

    private JPanel buildSingleValueBlock(String heading, String value, Color valueColor, int valueStyle)
    {
        JPanel panel = questHelperBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
        panel.add(label(heading, Color.LIGHT_GRAY, Font.PLAIN, 13f));

        JPanel strip = valueStripPanel();
        strip.add(textArea(value, valueColor, valueStyle, 13.5f), BorderLayout.CENTER);
        panel.add(strip);
        return panel;
    }

    private JPanel buildSectionBanner(String section)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ORANGE);
        panel.setBorder(new EmptyBorder(9, 10, 9, 10));

        JLabel title = new JLabel(section);
        title.setForeground(Color.BLACK);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15.5f));
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionBlock(GuideStep step)
    {
        JPanel panel = questHelperBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        panel.add(label(step.isOptional() ? "Optional:" : "Do this now:", Color.LIGHT_GRAY, Font.PLAIN, 13f));

        JPanel titleStrip = valueStripPanel();
        titleStrip.add(textArea(step.getTitle(), ORANGE_TEXT, Font.BOLD, 15f), BorderLayout.CENTER);
        panel.add(titleStrip);

        JPanel instructionStrip = valueStripPanel();
        instructionStrip.add(textArea(step.getInstruction(), Color.LIGHT_GRAY, Font.PLAIN, 14f), BorderLayout.CENTER);
        panel.add(instructionStrip);
        return panel;
    }

    private JPanel buildSectionSteps(GuideStep current)
    {
        JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 0));
        panel.setOpaque(false);

        String chapter = current.getChapter();
        for (int i = 0; i < guideState.getStepCount(); i++)
        {
            GuideStep step = guideState.getStep(i);
            if (step == null || !chapter.equals(step.getChapter()))
            {
                continue;
            }
            panel.add(buildStepRow(step, i));
        }
        return panel;
    }

    private JPanel buildStepRow(GuideStep step, int index)
    {
        boolean active = index == guideState.getCurrentIndex();
        boolean completed = index < guideState.getCurrentIndex();

        JPanel row = new JPanel(new BorderLayout());
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
            new EmptyBorder(8, 7, 8, 7)
        ));

        String prefix = completed ? "✓ " : step.isOptional() ? "◇ " : "";
        JTextArea text = textArea(
            prefix + step.getTitle(),
            active ? ORANGE_TEXT : completed ? COMPLETED : Color.LIGHT_GRAY,
            active ? Font.BOLD : Font.PLAIN,
            13.5f
        );
        row.add(text, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                guideState.setCurrentIndex(index);
            }
        });
        return row;
    }

    private JPanel buildControls()
    {
        JPanel controls = new JPanel(new GridLayout(1, 3, 5, 0));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(4, 0, 1, 0));

        JButton back = new JButton("Back");
        JButton done = new JButton(guideState.hasNext() ? "Done" : "Finished");
        JButton next = new JButton("Next");

        back.setEnabled(guideState.hasPrevious());
        done.setEnabled(guideState.hasNext());
        next.setEnabled(guideState.hasNext());

        back.addActionListener(e -> guideState.previous());
        done.addActionListener(e -> guideState.next());
        next.addActionListener(e -> guideState.next());

        controls.add(back);
        controls.add(done);
        controls.add(next);
        return controls;
    }

    private static JPanel questHelperBlock()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        return panel;
    }

    private static JPanel valueStripPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(7, 7, 7, 7));
        return panel;
    }

    private static JPanel valueStrip(String text)
    {
        JPanel panel = valueStripPanel();
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13.5f));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel label(String text, Color color, int style, float size)
    {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(style, size));
        return label;
    }

    private static JTextArea textArea(String text, Color color, int style, float size)
    {
        JTextArea area = new JTextArea(text == null ? "" : text);
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
        area.setCaretPosition(0);
        return area;
    }

    private static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private static String sectionName(String chapter)
    {
        if (chapter == null || chapter.trim().isEmpty())
        {
            return "Current Route";
        }

        int dash = chapter.lastIndexOf('—');
        if (dash >= 0 && dash + 1 < chapter.length())
        {
            return chapter.substring(dash + 1).trim();
        }
        return chapter;
    }
}
