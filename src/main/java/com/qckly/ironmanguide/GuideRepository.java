package com.qckly.ironmanguide;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Embedded guide data used by the first playable plugin prototype.
 *
 * Source of truth: Qckly/osrs-zero-knowledge-ironman-guide
 * Chapter 1 — Account Foundation.
 *
 * The repository is intentionally simple for now. A later guide-engine pass will
 * move requirements, items, locations and completion conditions into structured
 * fields instead of keeping everything inside prose.
 */
public final class GuideRepository
{
    private static final List<GuideStep> STEPS = Collections.unmodifiableList(Arrays.asList(
        new GuideStep(
            "ch01-step-001",
            "Chapter 1 — Tutorial Island",
            "Complete Tutorial Island normally",
            "Follow the Tutorial Island instructors and complete the introductory sequence. Do not worry about optimising the tiny XP rewards here.",
            "The important early goal is learning the basic interfaces and reaching the Ironman choice near the end of the island."
        ),
        new GuideStep(
            "ch01-step-002",
            "Chapter 1 — Tutorial Island",
            "Choose Standard Ironman",
            "Before leaving Tutorial Island, speak to Paul, the Ironman tutor, open the Ironman setup interface and select Standard Ironman for this route.",
            "Ironman restrictions must be selected before leaving Tutorial Island. A normal account cannot simply become an Ironman later."
        ),
        new GuideStep(
            "ch01-step-003",
            "Chapter 1 — Tutorial Island",
            "Finish the island and reach Lumbridge",
            "Complete the remaining tutorial and let the Magic Instructor send you to the mainland. Stop once you arrive in Lumbridge.",
            "This is the first clean checkpoint of the route: a fresh Standard Ironman standing in Lumbridge."
        ),
        new GuideStep(
            "ch01-step-004",
            "Chapter 1 — Lumbridge Setup",
            "Learn the Lumbridge bank",
            "Enter Lumbridge Castle and go upstairs to the bank on the top floor. Deposit anything you do not need while moving around.",
            "This is your first reliable bank and the route will repeatedly use it during the opening loop."
        ),
        new GuideStep(
            "ch01-step-005",
            "Chapter 1 — Lumbridge Setup",
            "Buy a spade and hammer",
            "Visit the Lumbridge General Store north of the castle. Buy 1 spade and 1 hammer and keep both. Sell only low-value starter gear the route does not need if you require coins.",
            "The spade is needed almost immediately for X Marks the Spot, while both tools are repeatedly useful on an Ironman."
        ),
        new GuideStep(
            "ch01-step-006",
            "Chapter 1 — Lumbridge Setup",
            "Start X Marks the Spot",
            "Speak to Veos in Lumbridge and begin X Marks the Spot. Do not treat it as a separate trip; advance it while moving through other early route locations.",
            "The quest gives fast Quest Point progress, 200 coins and a flexible 300 XP antique lamp."
        ),
        new GuideStep(
            "ch01-step-007",
            "Chapter 1 — Lumbridge Setup",
            "Start The Restless Ghost",
            "Speak to Father Aereck in Lumbridge church and start The Restless Ghost. Do not make a special long detour to finish it yet.",
            "Quest Prayer XP is much more valuable than grinding low-level monsters for bones at level 1."
        ),
        new GuideStep(
            "ch01-step-008",
            "Chapter 1 — Lumbridge Setup",
            "Start Rune Mysteries",
            "Speak to Duke Horacio upstairs in Lumbridge Castle and begin Rune Mysteries. Keep the air talisman until the route explicitly consumes or replaces it.",
            "Rune Mysteries opens early Runecraft progression and fits naturally into later travel toward Varrock and the Wizards' Tower."
        ),
        new GuideStep(
            "ch01-step-009",
            "Chapter 1 — Lumbridge Setup",
            "Take useful free Lumbridge supplies",
            "While already inside the castle, take convenient free supplies from the kitchen/cellar area: a jug, a bucket, water for both and basic food. Do not wait around for low-value respawns.",
            "We take free supplies only when geographically efficient; waiting several minutes for tiny savings is slower than moving the route forward."
        ),
        new GuideStep(
            "ch01-step-010",
            "Chapter 1 — Lumbridge Setup",
            "Bank logs and ashes",
            "Build a tiny early reserve near Lumbridge: bank at least 7 normal logs and at least 4 ashes. Avoid turning this into a long gathering grind.",
            "These common items appear in later requirements, and banking a small reserve now prevents unnecessary backtracking."
        ),
        new GuideStep(
            "ch01-step-011",
            "Chapter 1 — Lumbridge Setup",
            "Reach 15 Firemaking",
            "Burn normal logs near a convenient bank until you reach 15 Firemaking. Stop at 15; do not begin a long Wintertodt grind here.",
            "This is a cheap prerequisite-style setup that smooths later progression without committing the account to an outdated single-minigame route."
        ),
        new GuideStep(
            "ch01-step-012",
            "Chapter 1 — Lumbridge Setup",
            "Make about 1,000 arrow shafts",
            "Use spare normal logs and a knife to fletch roughly 1,000 arrow shafts, then bank them. Do not keep chopping thousands of extra logs beyond this target.",
            "This gives cheap early Fletching XP and creates an ammunition component we can reuse later instead of repeating the same gathering trip."
        ),
        new GuideStep(
            "ch01-step-013",
            "Chapter 1 — Lumbridge Setup",
            "Remember the XP-lamp rule",
            "Default policy: use flexible XP rewards on Herblore whenever the reward allows it and Herblore is eligible. Do not spend lamps randomly just to clear inventory space.",
            "Herblore is awkward to train early on an Ironman, so flexible XP has unusually high value there. The X Marks the Spot lamp can be reclaimed later if needed."
        ),
        new GuideStep(
            "ch01-step-014",
            "Chapter 1 — Local Quest Sweep",
            "Start Cook's Assistant",
            "Speak to the Cook in Lumbridge Castle kitchen and start Cook's Assistant. Collect and retain its simple ingredients as the route naturally encounters them.",
            "Cook's Assistant is fast early progress and eventually forms part of the Recipe for Disaster prerequisite chain."
        ),
        new GuideStep(
            "ch01-step-015",
            "Chapter 1 — Local Quest Sweep",
            "Keep useful future quest items",
            "From now on, keep cheap or awkward-to-replace items when the guide flags them for later. Do not casually sell gems, herbs, seeds or unusual quest-looking items just because your coin stack is low.",
            "Efficient Ironman routing reduces future travel before the future quest is even started."
        ),
        new GuideStep(
            "ch01-step-016",
            "Chapter 1 — Local Quest Sweep",
            "Move toward Draynor",
            "Prepare spade, basic food, coins, any available starter runes and the current X Marks the Spot items. Bank unnecessary weight, then walk west/south-west toward Draynor while advancing clue locations that fall naturally on the route.",
            "Draynor is the next compact hub and this movement lets us combine quest progress with travel instead of making isolated trips."
        ),
        new GuideStep(
            "ch01-step-017",
            "Chapter 1 — Draynor",
            "Learn Draynor as a hub",
            "On arrival, locate Draynor bank first. Do not spend the last of your coins on travel items yet; early cash has several competing uses.",
            "Draynor later connects the route to Port Sarim, boats, Farming access, quest chains and the Chronicle teleport system."
        ),
        new GuideStep(
            "ch01-step-018",
            "Chapter 1 — Draynor",
            "Complete X Marks the Spot",
            "Finish the clue chain and return the ancient casket to Veos. If Herblore is available, use the 300 XP antique lamp on Herblore. If it is not available and the lamp blocks inventory, destroy it and reclaim it from Veos later rather than wasting the XP.",
            "The quest gives 1 Quest Point, 200 coins and flexible XP while fitting directly into the early travel route."
        ),
        new GuideStep(
            "ch01-step-019",
            "Chapter 1 — Route Principles",
            "Do not grind melee levels yet",
            "Do not stand at cows, goblins or guards training Attack and Strength unless a later step explicitly sends you there for another reason.",
            "Waterfall Quest, Tree Gnome Village, Fight Arena and The Grand Tree can leap over weak early melee training while also unlocking useful account progression."
        ),
        new GuideStep(
            "ch01-step-020",
            "Chapter 1 — Route Principles",
            "Do not lock into one minigame meta",
            "Do not automatically rush a long Wintertodt grind or copy an old fixed route. Follow this guide's numbered order while it evaluates Wintertodt, Tempoross, Guardians of the Rift, Varlamore and newer options against the route's actual needs.",
            "Modern Ironman progression has more viable early paths than older guides assumed, so activities are included because they serve the route, not because they are traditional."
        ),
        new GuideStep(
            "ch01-step-021",
            "Chapter 1 — First Milestone",
            "Understand the first progression backbone",
            "The opening route is building toward: cheap Quest Points and XP, Herblore unlock, early Agility and transport, Kandarin combat-XP quests, spirit trees and gnome gliders, Ardougne cloak 1, fairy-ring preparation, Prayer infrastructure and eventually Recipe for Disaster / Barrows Gloves. Continue strictly in numbered order.",
            "This is the dependency backbone for the next drafting block, not permission to skip ahead and create hidden requirement gaps."
        )
    ));

    private GuideRepository()
    {
    }

    public static List<GuideStep> getSteps()
    {
        return STEPS;
    }
}
