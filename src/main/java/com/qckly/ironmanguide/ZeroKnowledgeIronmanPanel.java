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
 * Structured sidebar inspired by the information hierarchy used by Quest Helper:
 * route summary -> active section -> current objective -> visible section steps.
 */
public final class ZeroKnowledgeIronmanPanel extends PluginPanel
{
    private static final Color ACCENT = new Color(234, 145, 0);
    private static final Color ACCENT_TEXT = new Color(255, 180, 0);
    private static final Color MUTED = new Color(180, 180, 180);
    private static final Color COMPLETED = new Color(145, 145, 145);

    private final GuideState guideState;
    private final JProgressBar progressBar = new JProgressBar();

    public ZeroKnowledgeIronmanPanel(GuideState guideState)
    {
        super();
        this.guideState = guideState;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new DynamicGridLayout(0, 1, 0, 7));

        guideState.addListener(() -> SwingUtilities.invokeLater(this::refresh));
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
        add(buildRouteSummary(step));
        add(buildSectionBanner(sectionName(step.getChapter())));
        add(buildCurrentStep(step));
        add(buildSectionSteps(step));
        add(buildControls());

        revalidate();
        repaint();
    }

    private JPanel buildHeader(GuideStep step)
    {
        JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 2));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 2, 2, 2));

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

    private JPanel buildRouteSummary(GuideStep step)
    {
        JPanel panel = darkBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 4));

        panel.add(label("ROUTE STATE", Color.LIGHT_GRAY, Font.PLAIN, 12f));
        panel.add(label("Standard Ironman", Color.WHITE, Font.BOLD, 14f));
        panel.add(label("Active: " + sectionName(step.getChapter()), MUTED, Font.PLAIN, 12.5f));
        return panel;
    }

    private JPanel buildSectionBanner(String section)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ACCENT);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel title = new JLabel(section);
        title.setForeground(Color.BLACK);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCurrentStep(GuideStep step)
    {
        JPanel panel = darkBlock();
        panel.setLayout(new DynamicGridLayout(0, 1, 0, 7));

        JLabel heading = label(
            "CURRENT STEP  ·  " + (guideState.getCurrentIndex() + 1) + "/" + guideState.getStepCount(),
            ACCENT_TEXT,
            Font.BOLD,
            13f
        );
        panel.add(heading);

        JTextArea title = textArea(step.getTitle(), Color.WHITE, Font.BOLD, 16.5f);
        JTextArea instruction = textArea(step.getInstruction(), Color.LIGHT_GRAY, Font.PLAIN, 14.5f);
        panel.add(title);
        panel.add(instruction);

        JLabel whyHeading = label("WHY", ACCENT_TEXT, Font.BOLD, 12.5f);
        JTextArea why = textArea(step.getWhy(), MUTED, Font.PLAIN, 13.5f);
        panel.add(whyHeading);
        panel.add(why);

        return panel;
    }

    private JPanel buildSectionSteps(GuideStep current)
    {
        JPanel panel = new JPanel(new DynamicGridLayout(0, 1, 0, 2));
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
        row.setBackground(active ? ColorScheme.DARKER_GRAY_COLOR : ColorScheme.DARK_GRAY_COLOR);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
            new EmptyBorder(8, 8, 8, 8)
        ));

        String prefix = active ? "› " : completed ? "✓ " : "";
        JTextArea text = textArea(
            prefix + step.getTitle(),
            active ? ACCENT_TEXT : completed ? COMPLETED : Color.WHITE,
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
        controls.setBorder(new EmptyBorder(3, 0, 0, 0));

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

    private static JPanel darkBlock()
    {
        JPanel panel = new JPanel();
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
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
