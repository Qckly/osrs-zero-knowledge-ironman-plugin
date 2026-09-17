package com.qckly.ironmanguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarPlayerID;

/**
 * Reads the live OSRS state we need to synchronize the Tutorial Island route.
 *
 * V1 deliberately exposes raw state first. Once we collect/verify the tutorial
 * progress values against our 000.xx steps, this becomes the deterministic
 * varp -> guide-step synchronizer.
 */
public final class TutorialStateTracker
{
    private static final Set<Integer> TUTORIAL_ISLAND_REGIONS = new HashSet<>(Arrays.asList(
        12336, 12335, 12592, 12080, 12079, 12436
    ));

    private final Client client;
    private final List<Runnable> listeners = new ArrayList<>();

    private boolean loggedIn;
    private boolean onTutorialIsland;
    private int tutorialProgress = -1;
    private int regionId = -1;

    public TutorialStateTracker(Client client)
    {
        this.client = client;
    }

    public void refresh()
    {
        boolean nextLoggedIn = client.getGameState() == GameState.LOGGED_IN;
        boolean nextOnTutorialIsland = false;
        int nextTutorialProgress = -1;
        int nextRegionId = -1;

        if (nextLoggedIn)
        {
            nextTutorialProgress = client.getVarpValue(VarPlayerID.TUTORIAL);

            Player player = client.getLocalPlayer();
            if (player != null)
            {
                nextRegionId = player.getWorldLocation().getRegionID();
                nextOnTutorialIsland = TUTORIAL_ISLAND_REGIONS.contains(nextRegionId);
            }
        }

        boolean changed =
            nextLoggedIn != loggedIn ||
            nextOnTutorialIsland != onTutorialIsland ||
            nextTutorialProgress != tutorialProgress ||
            nextRegionId != regionId;

        loggedIn = nextLoggedIn;
        onTutorialIsland = nextOnTutorialIsland;
        tutorialProgress = nextTutorialProgress;
        regionId = nextRegionId;

        if (changed)
        {
            notifyListeners();
        }
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public boolean isOnTutorialIsland()
    {
        return onTutorialIsland;
    }

    public int getTutorialProgress()
    {
        return tutorialProgress;
    }

    public int getRegionId()
    {
        return regionId;
    }

    public void addListener(Runnable listener)
    {
        listeners.add(listener);
    }

    private void notifyListeners()
    {
        for (Runnable listener : listeners)
        {
            listener.run();
        }
    }
}
