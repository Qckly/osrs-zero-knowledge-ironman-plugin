package com.qckly.ironmanguide;

/**
 * Exact Tutorial Island world targets for steps where human-readable guide text
 * is too ambiguous for safe object matching.
 *
 * Exact IDs always take priority over fuzzy name matching in WorldGuidanceOverlay.
 */
public final class TutorialExactTargetResolver
{
    private TutorialExactTargetResolver()
    {
    }

    public static Integer resolveObjectId(GuideStep step, TutorialStateTracker state)
    {
        if (step == null || state == null || !state.isOnTutorialIsland())
        {
            return null;
        }

        String id = step.getId();
        int progress = state.getTutorialProgress();

        if ("000.13".equals(id))
        {
            // Leaving the Survival Expert section is a two-part route:
            // 120 -> open the gate from the survival area
            // 130 -> open the Master Chef building door
            if (progress == 120)
            {
                return 9470;
            }

            if (progress == 130)
            {
                return 9709;
            }
        }

        // Future exact object mappings belong here rather than in UI code.
        return null;
    }
}
