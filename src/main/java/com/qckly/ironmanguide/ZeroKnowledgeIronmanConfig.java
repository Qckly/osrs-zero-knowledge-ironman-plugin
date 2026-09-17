package com.qckly.ironmanguide;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("zeroknowledgeironman")
public interface ZeroKnowledgeIronmanConfig extends Config
{
    @ConfigSection(
        name = "Path colours",
        description = "Colours used by the tile-by-tile route guidance",
        position = 10,
        closedByDefault = false
    )
    String pathColoursSection = "pathColours";
    @ConfigItem(
        keyName = "autoAdvance",
        name = "Auto-advance",
        description = "Automatically advance when a supported step is completed"
    )
    default boolean autoAdvance()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showObjectiveOverlay",
        name = "Objective overlay",
        description = "Show the current guide objective in the game view",
        position = 1
    )
    default boolean showObjectiveOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showWorldGuidance",
        name = "World guidance",
        description = "Show supported NPC, object and tile guidance overlays",
        position = 2
    )
    default boolean showWorldGuidance()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
        keyName = "pathOutlineColor",
        name = "Path outline",
        description = "Outline colour of normal path tiles",
        section = pathColoursSection,
        position = 0
    )
    default Color pathOutlineColor()
    {
        return new Color(0, 220, 255, 255);
    }

    @Alpha
    @ConfigItem(
        keyName = "pathFillColor",
        name = "Path fill",
        description = "Fill colour and opacity of normal path tiles",
        section = pathColoursSection,
        position = 1
    )
    default Color pathFillColor()
    {
        return new Color(0, 220, 255, 35);
    }

    @Alpha
    @ConfigItem(
        keyName = "pathTargetOutlineColor",
        name = "Final tile outline",
        description = "Outline colour of the final destination tile",
        section = pathColoursSection,
        position = 2
    )
    default Color pathTargetOutlineColor()
    {
        return new Color(0, 220, 255, 255);
    }

    @Alpha
    @ConfigItem(
        keyName = "pathTargetFillColor",
        name = "Final tile fill",
        description = "Fill colour and opacity of the final destination tile",
        section = pathColoursSection,
        position = 3
    )
    default Color pathTargetFillColor()
    {
        return new Color(0, 220, 255, 70);
    }
}
