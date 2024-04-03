package uk.hotten.gxui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

	private JavaPlugin plugin;

	private HashMap<Integer, GUIItem> inventoryContents;

	@Getter private String inventoryName;
	@Getter private int inventorySize;
	@Getter private boolean fillEmpty;

	@Getter private Sound openSound;
	@Getter private float openSoundFloat;
	@Getter private Sound errorSound;
	@Getter private float errorSoundFloat;

	@Getter private Player player;

	private Inventory inventory;

	private boolean isOpen = false; // prevents accidental un-registration from other GUIs that have the same name

	private HashMap<Integer, String> itemErrors;
	private HashMap<Integer, Integer> itemTimings;

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

	public GUIBase(JavaPlugin plugin, Player player, String inventoryName, int inventorySize, boolean fillEmpty, Sound openSound, float openSoundFloat, Sound errorSound, float errorSoundFloat) {
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
		if (! isOpen)
			Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		if (openSound != null)
			player.playSound(player.getLocation(), openSound, 100f, openSoundFloat);
		isOpen = true;

		Inventory playersInventory = player.getInventory();
		InventoryHolder inventoryContainer = playersInventory.getHolder(false);
		TextComponent nameHandler = LegacyComponentSerializer.legacyAmpersand().deserialize(inventoryName);
		inventory = Bukkit.createInventory(inventoryContainer, inventorySize, nameHandler);

		inventoryContents.clear();
		setupItems();

		for (Map.Entry<Integer, GUIItem> i : inventoryContents.entrySet()) {
			inventory.setItem(i.getKey(), i.getValue().build());
		}

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
					Inventory inventory = ((HumanEntity) player).getOpenInventory().getTopInventory();
					Component inventoryTitle = player.getOpenInventory().title();
					String inventoryTitleText = inventoryTitle.examinableName();
					if (! inventoryTitleText.equalsIgnoreCase(inventoryName)) {
						cancel();
						return;
					}

					inventory.clear();
					inventoryContents.clear();
					setupItems();
					int counter = 0;
					for (Map.Entry<Integer, GUIItem> i : inventoryContents.entrySet()) {
						if (itemErrors.containsKey(i.getKey())) {

							GUIItem error = new GUIItem(Material.RED_STAINED_GLASS_PANE, 1, addLinebreaks(itemErrors.get(i.getKey()), 30, "&7").get(counter), "&c&lERROR");

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
						for (int i = 0; i<inventorySize; i++) {
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

	public static ArrayList<TextComponent> convertToTextComponents(List<String> stringList) {
		ArrayList<TextComponent> textComponents = new ArrayList<>();

		for (String str : stringList) {
			TextComponent textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(str);
			textComponents.add(textComponent);
		}

		return textComponents;
	}

	public void addItem(Integer slot, GUIItem item) {
		inventoryContents.put(slot, item);
	}

	private ItemStack fillSlot() {
		ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta m = i.getItemMeta();
		TextComponent emptySlotDisplayName = LegacyComponentSerializer.legacyAmpersand().deserialize(" ");
		m.displayName(emptySlotDisplayName);
		i.setItemMeta(m);
		return i;
	}
	
	@EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().getItemMeta().isUnbreakable()) {
            event.setCancelled(true);
        }
    }
  
	/*@EventHandler
    public void inventoryClickHandler(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory i = e.getClickedInventory();
        if(e.getCurrentItem().getItemMeta().isUnbreakable()) {
            if(i.getTitle() != p.getInventory().getTitle()) {
                e.setCancelled(true);
            }
        }
    }*/

    @EventHandler
    public void inventoryDragHandler(InventoryDragEvent event) {
        if(event.getCursor().getItemMeta().isUnbreakable()) {
            event.setCancelled(true);
        }
    }

	@EventHandler
	public void playerInventoryClickHandler(InventoryClickEvent event) {
		Player clickedPlayer = (Player) event.getWhoClicked();
		ClickType clickType = event.getClick();
		Inventory inventory = event.getClickedInventory();
		ItemStack item = event.getCurrentItem();
		Component inventoryName = event.getView().title();
		String inventoryTitleText = inventoryName.examinableName();

		if (clickedPlayer != player)
			return;

		if (inventory == null)
			return;

		if (inventoryTitleText.equalsIgnoreCase(inventoryName.examinableName())) {
			if (item == null || ! item.hasItemMeta()) {
				if (errorSound != null)
					player.playSound(player.getLocation(), errorSound, 50f, errorSoundFloat);
				event.setCancelled(true);
				return;
			}

			if (itemErrors.containsKey(event.getSlot())) {
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 50f, 0.7f);
				event.setCancelled(true);
				return;
			}

			if (inventoryContents.containsKey(event.getSlot())) {
				GUIItem guiItem = inventoryContents.get(event.getSlot());
				event.setCancelled(true);

				if (! guiItem.isButton()) {
					if (guiItem.isPlayErrorSound() && errorSound != null)
						player.playSound(player.getLocation(), errorSound, 50f, errorSoundFloat);
						event.setCancelled(true);
					return;
				}

				boolean exec = guiItem.executeClick(clickType);
				if (! exec) {
					if (guiItem.isPlayErrorSound() && errorSound != null)
						player.playSound(player.getLocation(), errorSound, 100f, errorSoundFloat);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void close(InventoryCloseEvent event) {
		Player closer = (Player) event.getPlayer();
		Component inventoryTitle = player.getOpenInventory().title();
		String inventoryTitleText = inventoryTitle.examinableName();
		if (inventoryTitleText.equalsIgnoreCase(inventoryName) && isOpen) {
			if (closer == player) {
				HandlerList.unregisterAll(this);
				isOpen = false;
			}
		}
	}

	public void showErrorItem(Player player, Integer item, String error, boolean sound) {
		itemErrors.put(item, error);
		itemTimings.put(item, 0);
		if (sound)
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 100f, 0.7f);
	}

	public ArrayList<String> addLinebreaks(String input, int maxLineLength, String toAppendAfterNewline) {
		ArrayList<String> result = new ArrayList<>();

		StringTokenizer tok = new StringTokenizer(input, " ");
		StringBuilder output = new StringBuilder();
		output.insert(0, toAppendAfterNewline);
		int lineLen = 0;
		while (tok.hasMoreTokens()) {
			String word = tok.nextToken();

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
