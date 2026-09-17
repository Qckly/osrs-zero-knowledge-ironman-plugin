package com.qckly.ironmanguide;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates simple guide requirement prose against live inventory/equipment.
 *
 * Supported V1 examples:
 * - Raw shrimps
 * - Small fishing net in inventory
 * - Tinderbox + logs
 * - Hammer + bronze bar
 * - 1 tin ore + 1 copper ore
 *
 * More complex requirements (spells, quest states, skills, coordinates) will
 * later move to typed requirement data. Until then, this evaluator handles the
 * item-based majority without lying about unsupported state.
 */
public final class RequirementEvaluator
{
    public enum Status
    {
        SATISFIED,
        UNSATISFIED,
        UNKNOWN
    }

    private static final Pattern LEADING_COUNT = Pattern.compile("^\\s*(\\d+)\\s*(?:x\\s*)?(.*)$", Pattern.CASE_INSENSITIVE);

    private RequirementEvaluator()
    {
    }

    public static Status evaluate(String requirement, TutorialStateTracker state)
    {
        if (state == null || requirement == null || requirement.trim().isEmpty())
        {
            return Status.UNKNOWN;
        }

        String[] parts = requirement.split("\\+|\\n");
        boolean sawRecognizedItem = false;

        for (String rawPart : parts)
        {
            ParsedRequirement parsed = parse(rawPart);
            if (parsed == null || parsed.itemName.isEmpty())
            {
                continue;
            }

            int quantity = state.getPossessedQuantity(parsed.itemName);
            if (quantity > 0)
            {
                sawRecognizedItem = true;
                if (quantity < parsed.quantity)
                {
                    return Status.UNSATISFIED;
                }
                continue;
            }

            // If the prose looks like a non-item mechanic, don't pretend we can
            // evaluate it yet.
            if (looksNonItem(parsed.itemName))
            {
                continue;
            }

            sawRecognizedItem = true;
            return Status.UNSATISFIED;
        }

        return sawRecognizedItem ? Status.SATISFIED : Status.UNKNOWN;
    }

    private static ParsedRequirement parse(String raw)
    {
        if (raw == null)
        {
            return null;
        }

        String text = raw.trim();
        if (text.isEmpty())
        {
            return null;
        }

        int quantity = 1;
        Matcher matcher = LEADING_COUNT.matcher(text);
        if (matcher.matches())
        {
            quantity = Math.max(1, Integer.parseInt(matcher.group(1)));
            text = matcher.group(2);
        }

        text = text
            .replaceAll("(?i)\\bin (?:your )?inventory\\b", "")
            .replaceAll("(?i)\\bequipped\\b", "")
            .replaceAll("(?i)\\bavailable\\b", "")
            .replaceAll("(?i)\\bsupplied\\b", "")
            .trim();

        return new ParsedRequirement(normalize(text), quantity);
    }

    private static boolean looksNonItem(String normalized)
    {
        return normalized.contains("wind strike")
            || normalized.contains("level ")
            || normalized.contains("quest ")
            || normalized.contains("interface")
            || normalized.contains("prayer")
            || normalized.contains("spell");
    }

    private static String normalize(String value)
    {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private static final class ParsedRequirement
    {
        private final String itemName;
        private final int quantity;

        private ParsedRequirement(String itemName, int quantity)
        {
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }
}
