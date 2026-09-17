package com.qckly.ironmanguide;

/**
 * Maps the live Tutorial Island varp to the exact guide step.
 *
 * IMPORTANT:
 * Tutorial Island already exposes a dedicated progression varp. Prefer that
 * server-driven state over inventory heuristics. Inventory/equipment checks are
 * used only inside a single varp state where the game intentionally groups more
 * than one action together.
 *
 * Reference: OSRS Tutorial Island state varp (281 / VarPlayerID.TUTORIAL).
 */
public final class TutorialStepResolver
{
    private TutorialStepResolver()
    {
    }

    public static String resolve(TutorialStateTracker state)
    {
        if (state == null || !state.isLoggedIn() || !state.isOnTutorialIsland())
        {
            return null;
        }

        final int progress = state.getTutorialProgress();

        switch (progress)
        {
            // Character creation / Gielinor Guide
            case 1:
                return "000.01";
            case 2:
                return "000.03";
            case 3:
                return "000.03A";
            case 7:
                return "000.03B";
            case 10:
                return "000.04";

            // Survival Expert
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
            case 70:
                return "000.10"; // Cut tree
            case 80:
                return "000.11"; // Make fire
            case 90:
                return "000.12"; // Cook raw shrimp
            case 120:
            case 130:
                return "000.13";

            // Master Chef
            case 140:
                return "000.14";
            case 150:
                return "000.15";
            case 160:
                return "000.16";
            case 170:
            case 200:
                return "000.17";

            // Quest Guide
            case 220:
                return "000.18";
            case 230:
                return "000.19";
            case 240:
                return "000.19A";
            case 250:
                return "000.20";

            // Mining Instructor
            case 260:
                return "000.21";
            case 300:
                return "000.22";
            case 310:
                return "000.23";
            case 320:
                return "000.24";
            case 330:
                return "000.25";
            case 340:
                return "000.26";
            case 350:
                return "000.26A";
            case 360:
            case 370:
                return "000.27";

            // Combat Instructor
            case 390:
                return "000.28";
            case 400:
                return "000.28A";
            case 405:
                return "000.28B";
            case 410:
                return "000.29";
            case 420:
                return "000.30";
            case 430:
                return "000.31";
            case 440:
                return "000.32";
            case 450:
            case 460:
                return "000.33";
            case 470:
                return "000.34";
            case 480:
                // The game groups equipping the bow/arrows and starting the
                // ranged attack in one tutorial state.
                if (state.hasEquippedItem("shortbow") && state.hasEquippedItem("bronze arrow"))
                {
                    return "000.36";
                }
                return "000.35";
            case 490:
                return "000.36";
            case 500:
                return "000.37";

            // Bank / Account Guide
            case 510:
                return "000.38";
            case 520:
                return "000.39";
            case 530:
                return "000.40";
            case 531:
                return "000.41";
            case 532:
                return "000.41A";
            case 540:
                return "000.42";

            // Brother Brace
            case 550:
                return "000.42";
            case 560:
                return "000.43";
            case 570:
                return "000.43A";
            case 580:
                return "000.43B";
            case 600:
                return "000.43C";
            case 610:
                return "000.44";

            // Magic Instructor
            case 620:
                return "000.45";
            case 630:
                return "000.45A";
            case 640:
                return "000.45B";
            case 650:
                return "000.46";

            // 670 means the Magic Instructor is ready to send the player off
            // Tutorial Island. Ironman setup happens while this varp remains
            // unchanged, so do not force-advance beyond the first Ironman step.
            case 670:
                return "000.47";

            // 1000 = Tutorial Island completed.
            case 1000:
                return null;

            default:
                // Unknown or newly-added state: keep the persisted step instead
                // of guessing and moving the player to the wrong instruction.
                return null;
        }
    }
}
