package com.qckly.ironmanguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

/**
 * Reads live OSRS state used by the guide progression engine.
 *
 * Although this class starts with Tutorial Island, the inventory/equipment
 * snapshot helpers are deliberately generic so they can later be moved into
 * the global account-state tracker without changing resolver code.
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
    private Set<String> inventoryItems = Collections.emptySet();
    private Set<String> equippedItems = Collections.emptySet();
    private boolean inventoryVisible;
    private boolean skillsVisible;
    private boolean questListVisible;
    private boolean combatOptionsVisible;
    private boolean prayerVisible;
    private boolean magicVisible;

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
        Set<String> nextInventoryItems = Collections.emptySet();
        Set<String> nextEquippedItems = Collections.emptySet();
        boolean nextInventoryVisible = false;
        boolean nextSkillsVisible = false;
        boolean nextQuestListVisible = false;
        boolean nextCombatOptionsVisible = false;
        boolean nextPrayerVisible = false;
        boolean nextMagicVisible = false;

        if (nextLoggedIn)
        {
            nextTutorialProgress = client.getVarpValue(VarPlayerID.TUTORIAL);

            Player player = client.getLocalPlayer();
            if (player != null)
            {
                nextRegionId = player.getWorldLocation().getRegionID();
                nextOnTutorialIsland = TUTORIAL_ISLAND_REGIONS.contains(nextRegionId);
            }

            nextInventoryItems = readItemNames(client.getItemContainer(InventoryID.INV));
            nextEquippedItems = readItemNames(client.getItemContainer(InventoryID.WORN));

            Widget inventoryWidget = client.getWidget(InterfaceID.Inventory.ITEMS);
            nextInventoryVisible = isVisible(inventoryWidget);

            nextSkillsVisible = isVisible(client.getWidget(InterfaceID.Stats.UNIVERSE));
            nextQuestListVisible = isVisible(client.getWidget(InterfaceID.Questlist.UNIVERSE));
            nextCombatOptionsVisible = isVisible(client.getWidget(InterfaceID.CombatInterface.LEVEL));
            nextPrayerVisible = isVisible(client.getWidget(InterfaceID.Prayerbook.UNIVERSE));
            nextMagicVisible = isVisible(client.getWidget(InterfaceID.MagicSpellbook.UNIVERSE));
        }

        boolean changed =
            nextLoggedIn != loggedIn ||
            nextOnTutorialIsland != onTutorialIsland ||
            nextTutorialProgress != tutorialProgress ||
            nextRegionId != regionId ||
            nextInventoryVisible != inventoryVisible ||
            nextSkillsVisible != skillsVisible ||
            nextQuestListVisible != questListVisible ||
            nextCombatOptionsVisible != combatOptionsVisible ||
            nextPrayerVisible != prayerVisible ||
            nextMagicVisible != magicVisible ||
            !nextInventoryItems.equals(inventoryItems) ||
            !nextEquippedItems.equals(equippedItems);

        loggedIn = nextLoggedIn;
        onTutorialIsland = nextOnTutorialIsland;
        tutorialProgress = nextTutorialProgress;
        regionId = nextRegionId;
        inventoryItems = nextInventoryItems;
        equippedItems = nextEquippedItems;
        inventoryVisible = nextInventoryVisible;
        skillsVisible = nextSkillsVisible;
        questListVisible = nextQuestListVisible;
        combatOptionsVisible = nextCombatOptionsVisible;
        prayerVisible = nextPrayerVisible;
        magicVisible = nextMagicVisible;

        if (changed)
        {
            notifyListeners();
        }
    }

    private Set<String> readItemNames(ItemContainer container)
    {
        if (container == null)
        {
            return Collections.emptySet();
        }

        Set<String> names = new HashSet<>();
        for (Item item : container.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            ItemComposition composition = client.getItemDefinition(item.getId());
            if (composition == null)
            {
                continue;
            }

            String normalized = normalize(composition.getName());
            if (!normalized.isEmpty() && !"null".equals(normalized))
            {
                names.add(normalized);
            }
        }

        return Collections.unmodifiableSet(names);
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

    public boolean isInventoryVisible()
    {
        return inventoryVisible;
    }

    public boolean isSkillsVisible()
    {
        return skillsVisible;
    }

    public boolean isQuestListVisible()
    {
        return questListVisible;
    }

    public boolean isCombatOptionsVisible()
    {
        return combatOptionsVisible;
    }

    public boolean isPrayerVisible()
    {
        return prayerVisible;
    }

    public boolean isMagicVisible()
    {
        return magicVisible;
    }

    public boolean hasInventoryItem(String itemName)
    {
        return inventoryItems.contains(normalize(itemName));
    }

    public boolean hasAnyInventoryItem(String... itemNames)
    {
        if (itemNames == null)
        {
            return false;
        }

        for (String itemName : itemNames)
        {
            if (hasInventoryItem(itemName))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasEquippedItem(String itemName)
    {
        return equippedItems.contains(normalize(itemName));
    }

    public Set<String> getInventoryItems()
    {
        return inventoryItems;
    }

    public Set<String> getEquippedItems()
    {
        return equippedItems;
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

    private static boolean isVisible(Widget widget)
    {
        return widget != null && !widget.isHidden();
    }

    private static String normalize(String value)
    {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
