package com.qckly.ironmanguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
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
    private Map<String, Integer> inventoryItemCounts = Collections.emptyMap();
    private Map<String, Integer> equippedItemCounts = Collections.emptyMap();
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
        Map<String, Integer> nextInventoryItemCounts = Collections.emptyMap();
        Map<String, Integer> nextEquippedItemCounts = Collections.emptyMap();
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

            nextInventoryItemCounts = readItemCounts(client.getItemContainer(InventoryID.INV));
            nextEquippedItemCounts = readItemCounts(client.getItemContainer(InventoryID.WORN));
            nextInventoryItems = nextInventoryItemCounts.keySet();
            nextEquippedItems = nextEquippedItemCounts.keySet();

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
            !nextInventoryItemCounts.equals(inventoryItemCounts) ||
            !nextEquippedItemCounts.equals(equippedItemCounts);

        loggedIn = nextLoggedIn;
        onTutorialIsland = nextOnTutorialIsland;
        tutorialProgress = nextTutorialProgress;
        regionId = nextRegionId;
        inventoryItemCounts = nextInventoryItemCounts;
        equippedItemCounts = nextEquippedItemCounts;
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

    private Map<String, Integer> readItemCounts(ItemContainer container)
    {
        if (container == null)
        {
            return Collections.emptyMap();
        }

        Map<String, Integer> counts = new HashMap<>();
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
                counts.merge(normalized, Math.max(1, item.getQuantity()), Integer::sum);
            }
        }

        return Collections.unmodifiableMap(counts);
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
        return getInventoryQuantity(itemName) > 0;
    }

    public int getInventoryQuantity(String itemName)
    {
        return quantityFor(inventoryItemCounts, normalize(itemName));
    }

    public int getEquippedQuantity(String itemName)
    {
        return quantityFor(equippedItemCounts, normalize(itemName));
    }

    public int getPossessedQuantity(String itemName)
    {
        return getInventoryQuantity(itemName) + getEquippedQuantity(itemName);
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
        return getEquippedQuantity(itemName) > 0;
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

    private static int quantityFor(Map<String, Integer> counts, String requested)
    {
        if (requested == null || requested.isEmpty())
        {
            return 0;
        }

        Integer exact = counts.get(requested);
        if (exact != null)
        {
            return exact;
        }

        // Small normalization bridge for guide prose such as "bronze arrows"
        // vs the RuneLite item name "bronze arrow".
        String singular = requested.endsWith("s") ? requested.substring(0, requested.length() - 1) : requested;
        String plural = requested.endsWith("s") ? requested : requested + "s";

        for (Map.Entry<String, Integer> entry : counts.entrySet())
        {
            String name = entry.getKey();
            if (name.equals(singular) || name.equals(plural))
            {
                return entry.getValue();
            }
        }

        return 0;
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
