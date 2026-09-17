package com.qckly.ironmanguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads the playable route directly from embedded guide modules.
 *
 * The guide repository is the source of truth; Java UI code should not contain
 * hundreds of hard-coded route steps. New modules can follow the same Markdown
 * field format and be loaded here.
 */
public final class GuideRepository
{
    private static final String TUTORIAL_RESOURCE = "/guide/000-learning-the-ropes-tutorial-island.md";
    private static final List<GuideStep> STEPS = Collections.unmodifiableList(loadTutorial());

    private GuideRepository()
    {
    }

    public static List<GuideStep> getSteps()
    {
        return STEPS;
    }

    private static List<GuideStep> loadTutorial()
    {
        InputStream input = GuideRepository.class.getResourceAsStream(TUTORIAL_RESOURCE);
        if (input == null)
        {
            return Collections.singletonList(new GuideStep(
                "error",
                "Guide data",
                "Guide module could not be loaded",
                "Missing resource: " + TUTORIAL_RESOURCE,
                "",
                null,
                null,
                null,
                false
            ));
        }

        List<GuideStep> steps = new ArrayList<>();
        String section = "Tutorial Island";
        StepBuilder current = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String trimmed = line.trim();

                if (trimmed.startsWith("## ") && !trimmed.startsWith("### "))
                {
                    if (isRouteSection(trimmed))
                    {
                        section = normalizeSection(trimmed.substring(3));
                    }
                    continue;
                }

                if (trimmed.startsWith("### "))
                {
                    if (current != null)
                    {
                        steps.add(current.build());
                    }
                    current = StepBuilder.fromHeading(trimmed.substring(4), section);
                    continue;
                }

                if (current == null || trimmed.isEmpty() || trimmed.equals("---"))
                {
                    continue;
                }

                if (trimmed.startsWith("**TARGET:**"))
                {
                    current.target = fieldValue(trimmed, "**TARGET:**");
                }
                else if (trimmed.startsWith("**REQUIRED:**"))
                {
                    current.addRequirement(fieldValue(trimmed, "**REQUIRED:**"));
                }
                else if (trimmed.startsWith("**EQUIPPED:**"))
                {
                    current.addRequirement(fieldValue(trimmed, "**EQUIPPED:**"));
                }
                else if (trimmed.startsWith("**DO:**"))
                {
                    current.instruction = fieldValue(trimmed, "**DO:**");
                }
                else if (trimmed.startsWith("**COMPLETE WHEN:**"))
                {
                    current.completeWhen = fieldValue(trimmed, "**COMPLETE WHEN:**");
                }
                else if (trimmed.startsWith("**WHY:**"))
                {
                    current.addWhy(fieldValue(trimmed, "**WHY:**"));
                }
                else if (trimmed.startsWith("**BEGINNER NOTE:**"))
                {
                    current.addWhy("Beginner note: " + fieldValue(trimmed, "**BEGINNER NOTE:**"));
                }
                else if (trimmed.startsWith("**NOTE:**"))
                {
                    current.addWhy("Note: " + fieldValue(trimmed, "**NOTE:**"));
                }
                else if (trimmed.startsWith("**REMEMBER:**"))
                {
                    current.addWhy("Remember: " + fieldValue(trimmed, "**REMEMBER:**"));
                }
                else if (trimmed.startsWith("**SAFETY:**"))
                {
                    current.addWhy("Safety: " + fieldValue(trimmed, "**SAFETY:**"));
                }
                else if (trimmed.startsWith("**HARD STOP:**"))
                {
                    current.addWhy("HARD STOP: " + fieldValue(trimmed, "**HARD STOP:**"));
                }
                else if (trimmed.startsWith("**OPTIONAL:**"))
                {
                    current.optional = true;
                    current.instruction = fieldValue(trimmed, "**OPTIONAL:**");
                }
                else if (trimmed.startsWith("**MAIN ROUTE:**"))
                {
                    current.addWhy("Main route: " + fieldValue(trimmed, "**MAIN ROUTE:**"));
                }
            }

            if (current != null)
            {
                steps.add(current.build());
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Unable to load guide module " + TUTORIAL_RESOURCE, ex);
        }

        return steps;
    }

    private static boolean isRouteSection(String heading)
    {
        if (heading.length() < 6)
        {
            return false;
        }

        char first = heading.charAt(3);
        return first >= 'A' && first <= 'K' && heading.contains("—");
    }

    private static String normalizeSection(String heading)
    {
        int dash = heading.indexOf('—');
        if (dash >= 0 && dash + 1 < heading.length())
        {
            return heading.substring(dash + 1).trim();
        }
        return heading.trim();
    }

    private static String fieldValue(String line, String prefix)
    {
        return cleanMarkdown(line.substring(prefix.length()).trim());
    }

    private static String cleanMarkdown(String text)
    {
        return text
            .replace("**", "")
            .replace("  ", " ")
            .trim();
    }

    private static final class StepBuilder
    {
        private final String id;
        private final String section;
        private final String title;
        private String instruction = "";
        private String why = "";
        private String target;
        private String requirement;
        private String completeWhen;
        private boolean optional;

        private StepBuilder(String id, String section, String title, boolean optional)
        {
            this.id = id;
            this.section = section;
            this.title = title;
            this.optional = optional;
        }

        private static StepBuilder fromHeading(String heading, String section)
        {
            boolean optional = heading.startsWith("OPTIONAL ");
            String normalized = optional ? heading.substring("OPTIONAL ".length()) : heading;
            int dash = normalized.indexOf('—');

            String id;
            String title;
            if (dash >= 0)
            {
                id = normalized.substring(0, dash).trim();
                title = normalized.substring(dash + 1).trim();
            }
            else
            {
                id = normalized.trim();
                title = normalized.trim();
            }

            return new StepBuilder(id, section, cleanMarkdown(title), optional);
        }

        private void addRequirement(String value)
        {
            if (value == null || value.isEmpty())
            {
                return;
            }
            requirement = requirement == null || requirement.isEmpty()
                ? value
                : requirement + "\n" + value;
        }

        private void addWhy(String value)
        {
            if (value == null || value.isEmpty())
            {
                return;
            }
            why = why.isEmpty() ? value : why + "\n" + value;
        }

        private GuideStep build()
        {
            String finalInstruction = instruction == null || instruction.isEmpty()
                ? (optional ? "Optional branch. Complete it or skip it and continue the main route." : "Follow the current tutorial prompt.")
                : instruction;

            String finalCompleteWhen = completeWhen;
            if (optional && (finalCompleteWhen == null || finalCompleteWhen.isEmpty()))
            {
                finalCompleteWhen = "Optional branch completed or intentionally skipped.";
            }

            return new GuideStep(
                id,
                "Learning the Ropes — " + section,
                title,
                finalInstruction,
                why,
                target,
                requirement,
                finalCompleteWhen,
                optional
            );
        }
    }
}
