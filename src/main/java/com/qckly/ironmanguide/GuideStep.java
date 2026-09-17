import java.util.Collections;
import java.util.List;

package com.qckly.ironmanguide;

public final class GuideStep
{
    private final String id;
    private final String chapter;
    private final String title;
    private final String instruction;
    private final String why;
    private final String target;
    private final String requirement;
    private final String recommendedItems;
    private final String completeWhen;
    private final List<GuideWaypoint> waypoints;
    private final boolean optional;

    public GuideStep(String id, String chapter, String title, String instruction, String why)
    {
        this(id, chapter, title, instruction, why, null, null, null, null, Collections.emptyList(), false);
    }

    public GuideStep(
        String id,
        String chapter,
        String title,
        String instruction,
        String why,
        String target,
        String requirement,
        String recommendedItems,
        String completeWhen,
        List<GuideWaypoint> waypoints,
        boolean optional)
    {
        this.id = id;
        this.chapter = chapter;
        this.title = title;
        this.instruction = instruction;
        this.why = why;
        this.target = target;
        this.requirement = requirement;
        this.recommendedItems = recommendedItems;
        this.completeWhen = completeWhen;
        this.waypoints = waypoints == null ? Collections.emptyList() : Collections.unmodifiableList(waypoints);
        this.optional = optional;
    }

    public String getId()
    {
        return id;
    }

    public String getChapter()
    {
        return chapter;
    }

    public String getTitle()
    {
        return title;
    }

    public String getInstruction()
    {
        return instruction;
    }

    public String getWhy()
    {
        return why;
    }

    public String getTarget()
    {
        return target;
    }

    public String getRequirement()
    {
        return requirement;
    }

    public String getRecommendedItems()
    {
        return recommendedItems;
    }

    public String getCompleteWhen()
    {
        return completeWhen;
    }

    public List<GuideWaypoint> getWaypoints()
    {
        return waypoints;
    }

    public boolean isOptional()
    {
        return optional;
    }
}
