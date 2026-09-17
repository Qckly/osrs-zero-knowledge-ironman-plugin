package com.qckly.ironmanguide;

/**
 * Converts live Tutorial Island state into a guide step only when the mapping is
 * considered reliable.
 *
 * Unknown/ambiguous states intentionally return null. The plugin then keeps the
 * persisted last step instead of guessing and potentially moving the user
 * backwards or forwards incorrectly.
 */
public final class TutorialStepResolver
{
    private TutorialStepResolver()
    {
    }

    public static String resolve(TutorialStateTracker tracker)
    {
        if (tracker == null || !tracker.isLoggedIn() || !tracker.isOnTutorialIsland())
        {
            return null;
        }

        int progress = tracker.getTutorialProgress();

        // High-confidence Survival Expert states.
        // Observed/live + established Tutorial Island progress flow:
        // 10/20 -> talk to Survival Expert
        // 30    -> open inventory
        // 40    -> catch shrimp
        // 50    -> open Skills
        // 60    -> talk to Brynna again
        switch (progress)
        {
            case 10:
            case 20:
                return "000.05";
            case 30:
                return "000.06";
            case 40:
                return "000.07";
            case 50:
                return "000.08";
            case 60:
                return "000.09";

            // High-confidence section-entry states from the tutorial progress
            // state machine. Finer-grained completion detection will later use
            // inventory, equipment, widgets and scene state.
            case 120:
            case 130:
                return "000.13";
            case 140:
                return "000.14";
            case 200:
            case 210:
                return "000.17";
            case 220:
                return "000.18";
            case 230:
                return "000.19";
            default:
                return null;
        }
    }
}
