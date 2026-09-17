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

    public static String resolve(TutorialStateTracker state)
    {
        if (state == null || !state.isLoggedIn() || !state.isOnTutorialIsland())
        {
            return null;
        }

        int progress = state.getTutorialProgress();

        // --- A/B: Character creation + Survival Expert ---------------------
        if (progress == 10 || progress == 20)
        {
            if (state.hasInventoryItem("small fishing net"))
            {
                return "000.06";
            }
            return "000.05";
        }

        if (progress == 30)
        {
            // OSRS is explicitly waiting for the inventory tutorial here.
            return "000.06";
        }

        if (progress == 40)
        {
            // If the shrimp already exists, the fishing action is complete even
            // if the next UI transition has not yet updated the varp.
            if (state.hasAnyInventoryItem("raw shrimps", "shrimps"))
            {
                return "000.08";
            }
            return "000.07";
        }

        if (progress == 50)
        {
            return state.isSkillsVisible() ? "000.09" : "000.08";
        }

        if (progress == 60)
        {
            if (state.hasInventoryItem("bronze axe") && state.hasInventoryItem("tinderbox"))
            {
                return "000.10";
            }
            return "000.09";
        }

        // Survival crafting/cooking phase. Inventory is more useful than a
        // coarse varp here, so we combine both.
        if (progress > 60 && progress < 120)
        {
            boolean hasAxe = state.hasInventoryItem("bronze axe");
            boolean hasTinderbox = state.hasInventoryItem("tinderbox");
            boolean hasLogs = state.hasInventoryItem("logs");
            boolean hasRawShrimp = state.hasInventoryItem("raw shrimps");
            boolean hasCookedShrimp = state.hasInventoryItem("shrimps");

            if (!hasAxe || !hasTinderbox)
            {
                return "000.09";
            }

            if (hasCookedShrimp && !hasRawShrimp)
            {
                return "000.13";
            }

            if (hasLogs)
            {
                return "000.11";
            }

            if (hasRawShrimp)
            {
                // With the logs consumed, the player is normally at the
                // fire/cooking part. We cannot yet prove a fire exists, so this
                // is the furthest safe step.
                return "000.12";
            }

            return "000.10";
        }

        // --- C: Master Chef ------------------------------------------------
        if (progress == 120 || progress == 130)
        {
            return "000.13";
        }

        if (progress >= 140 && progress < 200)
        {
            if (state.hasInventoryItem("bread"))
            {
                return "000.17";
            }

            if (state.hasInventoryItem("bread dough"))
            {
                return "000.16";
            }

            if (state.hasInventoryItem("pot of flour") && state.hasInventoryItem("bucket of water"))
            {
                return "000.15";
            }

            return "000.14";
        }

        // --- D: Quest Guide ------------------------------------------------
        if (progress == 200 || progress == 210)
        {
            return "000.17";
        }

        if (progress == 220)
        {
            return "000.18";
        }

        if (progress == 230)
        {
            return state.isQuestListVisible() ? "000.20" : "000.19";
        }

        if (progress >= 240 && progress < 260)
        {
            return "000.20";
        }

        // --- E: Mining Instructor -----------------------------------------
        if (progress >= 260 && progress < 370)
        {
            boolean pickaxe = state.hasInventoryItem("bronze pickaxe");
            boolean tin = state.hasInventoryItem("tin ore");
            boolean copper = state.hasInventoryItem("copper ore");
            boolean bar = state.hasInventoryItem("bronze bar");
            boolean hammer = state.hasInventoryItem("hammer");
            boolean dagger = state.hasInventoryItem("bronze dagger");

            if (!pickaxe)
            {
                return "000.21";
            }

            if (dagger)
            {
                return "000.27";
            }

            if (bar && hammer)
            {
                return "000.26";
            }

            if (bar)
            {
                return "000.25";
            }

            if (tin && copper)
            {
                return "000.24";
            }

            if (tin)
            {
                return "000.23";
            }

            return "000.22";
        }

        // --- F: Combat Instructor -----------------------------------------
        if (progress >= 370 && progress < 510)
        {
            boolean daggerEquipped = state.hasEquippedItem("bronze dagger");
            boolean sword = state.hasInventoryItem("bronze sword") || state.hasEquippedItem("bronze sword");
            boolean shield = state.hasInventoryItem("wooden shield") || state.hasEquippedItem("wooden shield");
            boolean swordEquipped = state.hasEquippedItem("bronze sword");
            boolean shieldEquipped = state.hasEquippedItem("wooden shield");
            boolean bow = state.hasInventoryItem("shortbow") || state.hasEquippedItem("shortbow");
            boolean arrows = state.hasInventoryItem("bronze arrow") || state.hasEquippedItem("bronze arrow");
            boolean bowEquipped = state.hasEquippedItem("shortbow");
            boolean arrowsEquipped = state.hasEquippedItem("bronze arrow");

            if (!daggerEquipped && !sword && !shield)
            {
                return "000.27";
            }

            if (!daggerEquipped && !sword)
            {
                return "000.28";
            }

            if (sword && shield && !swordEquipped && !shieldEquipped)
            {
                return "000.30";
            }

            if (!bow)
            {
                if (swordEquipped && shieldEquipped && state.isCombatOptionsVisible())
                {
                    return "000.32";
                }

                // We cannot yet distinguish rat-pit entry vs first melee kill
                // perfectly. Keep the persisted step rather than guess.
                return null;
            }

            if (!bowEquipped || !arrowsEquipped)
            {
                return "000.35";
            }

            if (bow && arrows)
            {
                return "000.36";
            }
        }

        // --- G: Bank + Account Guide --------------------------------------
        if (progress >= 510 && progress < 540)
        {
            if (progress == 510)
            {
                return "000.37";
            }

            if (progress == 520)
            {
                return "000.38";
            }

            if (progress >= 530)
            {
                return "000.39";
            }
        }

        // --- H: Brother Brace ---------------------------------------------
        if (progress >= 540 && progress < 610)
        {
            if (progress < 600)
            {
                return "000.40";
            }

            return "000.42";
        }

        // --- I/J/K: Magic, Ironman, leaving the island --------------------
        // These states are deliberately left conservative until we add widget
        // and account-mode detection. The persisted step remains the fallback.
        if (progress >= 610 && progress < 1000)
        {
            return null;
        }

        return null;
    }
}
