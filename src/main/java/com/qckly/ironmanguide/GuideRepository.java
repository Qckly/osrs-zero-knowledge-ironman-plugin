package com.qckly.ironmanguide;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GuideRepository
{
    private static final List<GuideStep> STEPS = Collections.unmodifiableList(Arrays.asList(
        new GuideStep(
            "ch01-step-001",
            "Chapter 1 — Fresh Account",
            "Welcome to the route",
            "Follow the ordered steps. The finished guide data will replace these starter steps.",
            "This confirms the RuneLite guide engine and navigation are working."
        ),
        new GuideStep(
            "ch01-step-002",
            "Chapter 1 — Fresh Account",
            "Reach Lumbridge",
            "Finish Tutorial Island and arrive in Lumbridge.",
            "Lumbridge is the starting point for the first efficient Ironman route segment."
        ),
        new GuideStep(
            "ch01-step-003",
            "Chapter 1 — Fresh Account",
            "Prepare for progression",
            "Continue to the next guide step once the real Chapter 1 route is imported.",
            "The plugin is intentionally data-driven so the guide can grow without rewriting the UI."
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
