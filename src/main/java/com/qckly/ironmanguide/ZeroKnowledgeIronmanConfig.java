package com.qckly.ironmanguide;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("zeroknowledgeironman")
public interface ZeroKnowledgeIronmanConfig extends Config
{
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
}
