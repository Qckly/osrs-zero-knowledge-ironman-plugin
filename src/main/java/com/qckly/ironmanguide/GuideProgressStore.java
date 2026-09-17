package com.qckly.ironmanguide;

import net.runelite.client.config.ConfigManager;

/**
 * Persists progression by stable guide step ID.
 *
 * RS-profile storage is preferred so different RuneScape characters can keep
 * independent guide progress. A normal config fallback is also maintained for
 * development/startup states where the RS profile is not available yet.
 */
public final class GuideProgressStore
{
    private static final String GROUP = "zeroknowledgeironman";
    private static final String LAST_STEP_KEY = "lastStepId";

    private final ConfigManager configManager;

    public GuideProgressStore(ConfigManager configManager)
    {
        this.configManager = configManager;
    }

    public String loadLastStepId()
    {
        String profileValue = null;

        if (configManager.getRSProfileKey() != null)
        {
            profileValue = configManager.getRSProfileConfiguration(GROUP, LAST_STEP_KEY);
        }

        if (hasText(profileValue))
        {
            return profileValue.trim();
        }

        String fallback = configManager.getConfiguration(GROUP, LAST_STEP_KEY);
        return hasText(fallback) ? fallback.trim() : null;
    }

    public void saveLastStepId(String stepId)
    {
        if (!hasText(stepId))
        {
            return;
        }

        String value = stepId.trim();

        // Keep a fallback for dev startup / profile transitions.
        configManager.setConfiguration(GROUP, LAST_STEP_KEY, value);

        if (configManager.getRSProfileKey() != null)
        {
            configManager.setRSProfileConfiguration(GROUP, LAST_STEP_KEY, value);
        }
    }

    private static boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }
}
