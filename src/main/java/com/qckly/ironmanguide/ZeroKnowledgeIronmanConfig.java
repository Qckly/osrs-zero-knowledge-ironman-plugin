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
        name = "Colours",
        description = "Colours used by guide highlights",
        position = 10,
        closedByDefault = false
    )
    String coloursSection = "colours";
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

    @ConfigItem(
        keyName = "textHighlightColor",
        name = "Text highlight colour",
        description = "Colour used for dialogue choices and Click here to continue",
        section = coloursSection,
        position = 0
    )
    default Color textHighlightColor()
    {
        return new Color(0, 0, 255);
    }

    @Alpha
    @ConfigItem(
        keyName = "pathOutlineColor",
        name = "Path outline",
        description = "Outline colour of normal path tiles",
        section = coloursSection,
        position = 1
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
        section = coloursSection,
        position = 2
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
        section = coloursSection,
        position = 3
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
        section = coloursSection,
        position = 4
    )
    default Color pathTargetFillColor()
    {
        return new Color(0, 220, 255, 70);
    }
}
