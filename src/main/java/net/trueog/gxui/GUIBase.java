package net.trueog.gxui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public abstract class GUIBase implements Listener {

    private final JavaPlugin plugin;

    private final HashMap<Integer, GUIItem> inventoryContents;

    @Getter
    private final String inventoryName;

    @Getter
    private final int inventorySize;

    @Getter
    private final boolean fillEmpty;

    @Getter
    private final Sound openSound;

    @Getter
    private float openSoundFloat;

    @Getter
    private final Sound errorSound;

    @Getter
    private final float errorSoundFloat;

    @Getter
    private final Player player;

    private Inventory inventory;

    // Prevents accidental deregistration from other GUIs that have the same name.
    private boolean isOpen = false;

    private final HashMap<Integer, String> itemErrors;
    private final HashMap<Integer, Integer> itemTimings;

    public GUIBase(JavaPlugin plugin, Player player, String inventoryName, int inventorySize, boolean fillEmpty) {

        this.plugin = plugin;
        this.player = player;
        this.inventoryName = inventoryName;
        this.inventorySize = inventorySize;
        this.fillEmpty = fillEmpty;
        inventoryContents = new HashMap<>();

        openSound = null;

        errorSound = Sound.ENTITY_ITEM_BREAK;
        errorSoundFloat = (float) 0.6;

        itemErrors = new HashMap<>();
        itemTimings = new HashMap<>();

    }

    public GUIBase(JavaPlugin plugin, Player player, String inventoryName, int inventorySize, boolean fillEmpty,
            Sound openSound, float openSoundFloat, Sound errorSound, float errorSoundFloat)
    {

        this.plugin = plugin;
        this.player = player;
        this.inventoryName = inventoryName;
        this.inventorySize = inventorySize;
        this.fillEmpty = fillEmpty;

        inventoryContents = new HashMap<>();

        this.openSound = openSound;
        this.openSoundFloat = openSoundFloat;

        this.errorSound = errorSound;
        this.errorSoundFloat = errorSoundFloat;

        itemErrors = new HashMap<>();
        itemTimings = new HashMap<>();

    }

    public void open(boolean refresh) {

        if (!isOpen) {

            Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        }

        if (openSound != null) {

            player.playSound(player.getLocation(), openSound, 100f, openSoundFloat);

        }

        isOpen = true;

        final Inventory playersInventory = player.getInventory();
        final InventoryHolder inventoryContainer = playersInventory.getHolder(false);
        final TextComponent nameHandler = LegacyComponentSerializer.legacyAmpersand().deserialize(inventoryName);

        inventory = Bukkit.createInventory(inventoryContainer, inventorySize, nameHandler);

        inventoryContents.clear();
        setupItems();

        inventoryContents.entrySet().forEach(i -> inventory.setItem(i.getKey(), i.getValue().build()));

        if (fillEmpty) {

            for (int i = 0; i < inventorySize; i++) {

                if (inventory.getItem(i) == null) {

                    inventory.setItem(i, fillSlot());

                }

            }

        }

        player.openInventory(inventory);

        if (refresh) {

            new BukkitRunnable() {

                @Override
                public void run() {

                    final Inventory inventory = ((HumanEntity) player).getOpenInventory().getTopInventory();
                    final Component inventoryTitle = player.getOpenInventory().title();
                    final String inventoryTitleText = inventoryTitle.examinableName();
                    if (!StringUtils.equalsIgnoreCase(inventoryTitleText, inventoryName)) {

                        cancel();
                        return;

                    }

                    inventory.clear();
                    inventoryContents.clear();
                    setupItems();
                    int counter = 0;
                    for (Map.Entry<Integer, GUIItem> i : inventoryContents.entrySet()) {

                        if (itemErrors.containsKey(i.getKey())) {

                            final GUIItem error = new GUIItem(Material.RED_STAINED_GLASS_PANE, 1,
                                    addLinebreaks(itemErrors.get(i.getKey()), 30, "&7").get(counter), "&c&lERROR");

                            counter++;
                            int tick = itemTimings.get(i.getKey());
                            tick++;

                            itemTimings.remove(i.getKey());
                            itemTimings.put(i.getKey(), tick);

                            if (tick >= 9) {

                                itemErrors.remove(i.getKey());
                                itemTimings.remove(i.getKey());
                                inventory.setItem(i.getKey(), i.getValue().build());
                                continue;

                            }

                            inventory.setItem(i.getKey(), error.build());

                            continue;

                        }

                        inventory.setItem(i.getKey(), i.getValue().build());

                    }

                    if (fillEmpty) {

                        for (int i = 0; i < inventorySize; i++) {

                            if (inventory.getItem(i) == null) {

                                inventory.setItem(i, fillSlot());

                            }

                        }

                    }

                }

            }.runTaskTimer(plugin, 0, 5);

        }

    }

    public abstract void setupItems();

    public void addItem(Integer slot, GUIItem item) {

        inventoryContents.put(slot, item);

    }

    private ItemStack fillSlot() {

        final ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        final ItemMeta m = i.getItemMeta();
        final TextComponent emptySlotDisplayName = LegacyComponentSerializer.legacyAmpersand().deserialize(" ");

        m.displayName(emptySlotDisplayName);
        i.setItemMeta(m);

        return i;

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        if (event.getItemDrop().getItemStack().getItemMeta().isUnbreakable()) {

            event.setCancelled(true);

        }

    }

    @EventHandler
    public void inventoryDragHandler(InventoryDragEvent event) {

        if (event.getCursor().getItemMeta().isUnbreakable()) {

            event.setCancelled(true);

        }

    }

    @EventHandler
    public void playerInventoryClickHandler(InventoryClickEvent event) {

        final Player clickedPlayer = (Player) event.getWhoClicked();
        final ClickType clickType = event.getClick();
        final Inventory clickedInventory = event.getClickedInventory();
        final ItemStack item = event.getCurrentItem();
        final Inventory topInventory = event.getView().getTopInventory();
        if (clickedPlayer != player) {

            return;

        }

        if (clickedInventory == null) {

            return;

        }

        if (topInventory != inventory) {

            return;

        }

        // Prevent taking or moving placeholders/GUI items from this GUI.
        if (event.getRawSlot() < topInventory.getSize()) {

            event.setCancelled(true);

            if (item == null || !item.hasItemMeta()) {

                if (errorSound != null) {

                    player.playSound(player.getLocation(), errorSound, 50f, errorSoundFloat);

                }

                return;

            }

            if (itemErrors.containsKey(event.getSlot())) {

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 50f, 0.7f);
                event.setCancelled(true);
                return;

            }

            if (inventoryContents.containsKey(event.getSlot())) {

                final GUIItem guiItem = inventoryContents.get(event.getSlot());
                if (!guiItem.isButton()) {

                    if (guiItem.isPlayErrorSound() && errorSound != null) {

                        player.playSound(player.getLocation(), errorSound, 50f, errorSoundFloat);

                    }

                    event.setCancelled(true);

                    return;

                }

                final boolean exec = guiItem.executeClick(clickType);
                final boolean condition = !exec && guiItem.isPlayErrorSound() && errorSound != null;
                if (condition) {

                    player.playSound(player.getLocation(), errorSound, 100f, errorSoundFloat);

                }

            }

            return;

        }

        if (event.isShiftClick()) {

            event.setCancelled(true);

        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void close(InventoryCloseEvent event) {

        final Player closer = (Player) event.getPlayer();
        final Component inventoryTitle = player.getOpenInventory().title();
        final String inventoryTitleText = inventoryTitle.examinableName();
        final boolean condition = StringUtils.equalsIgnoreCase(inventoryTitleText, inventoryName) && isOpen
                && closer == player;
        if (!condition) {

            return;

        }

        HandlerList.unregisterAll(this);

        isOpen = false;

    }

    public void showErrorItem(Player player, Integer item, String error, boolean sound) {

        itemErrors.put(item, error);
        itemTimings.put(item, 0);

        if (sound) {

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 100f, 0.7f);

        }

    }

    public ArrayList<String> addLinebreaks(String input, int maxLineLength, String toAppendAfterNewline) {

        final ArrayList<String> result = new ArrayList<>();
        final StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder();

        output.insert(0, toAppendAfterNewline);
        int lineLen = 0;
        while (tok.hasMoreTokens()) {

            final String word = tok.nextToken();

            if (lineLen + word.length() > maxLineLength) {

                result.add(output.toString());
                output = new StringBuilder();
                output.append(toAppendAfterNewline);
                lineLen = 0;

            }

            output.append(word).append(" ");
            lineLen += word.length();

        }

        result.add(output.toString());

        return result;

    }

}