package uk.hotten.gxui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Accessors(fluent = true)
public class GUIItem {

    @Getter @Setter private Material item;
    @Getter @Setter private int amount;
    @Getter @Setter private String displayName;
    @Getter @Setter private ArrayList<String> lore;

    private boolean isSkull;
    private String skullOwner;

    private HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    @Getter @Setter private boolean unbreakable = false;

    @Getter @Setter private GUIButton button;

    @Getter @Setter private short type;

    @Getter @Setter private boolean playErrorSound;

    public GUIItem() {
        item = Material.AIR;
        amount = 1;
        displayName = "Not Set";
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;
    }

    public GUIItem(Material item) {
        this.item = item;
        amount = 1;
        displayName = "Not Set";
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;
    }
    
	public GUIItem(GUIButton guibutton) {
        amount = 1;
        displayName = "Not Set";
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;
    }

    public GUIItem(Material item, int amount, String displayName) {
        this.item = item;
        this.amount = amount;
        this.displayName = displayName;
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;
    }

    public GUIItem(Material item, int amount, String displayName, String skullOwner) {
        this.item = item;
        this.amount = amount;
        this.displayName = displayName;
        this.skullOwner = skullOwner;
        isSkull = true;
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;
    }

    /**
     * @return ItemStack
     */
    public ItemStack build() {
        ItemStack i;
        TextComponent displayNameTextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
        List<TextComponent> loreTextComponent = GUIBase.convertToTextComponents(lore);
        try {
            if (! isSkull) {
                if (type != -1)
                    i = new ItemStack(item, amount); // TODO: shorts?
                else
                    i = new ItemStack(item, amount);
                ItemMeta m = i.getItemMeta();
                if (! displayName.equals("Not Set"))
                    m.displayName(displayNameTextComponent);
                m.lore(loreTextComponent);
                if (!enchantments.isEmpty())
                    for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet())
                        m.addEnchant(e.getKey(), e.getValue(), true);

                i.setItemMeta(m);
            } else {
                i = new ItemStack(item, amount);
                SkullMeta m = (SkullMeta) i.getItemMeta();
                m.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwner));
                if (!displayName.equals("Not Set"))
                    m.displayName(displayNameTextComponent);
                i.setUnbreakable(unbreakable);
                m.lore(loreTextComponent);
                i.setItemMeta(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.DIRT);
        }

        return i;
    }

    public boolean isPlayErrorSound() {
		return playErrorSound;
	}

	public void setPlayErrorSound(boolean playErrorSound) {
		this.playErrorSound = playErrorSound;
	}

	public boolean isButton() {
        return !(button == null);
    }

    public boolean executeClick(ClickType clickType) {
        switch (clickType) {
            case LEFT:
                return button.leftClick();
            case SHIFT_LEFT:
                return button.leftClickShift();
            case RIGHT:
                return button.rightClick();
            case SHIFT_RIGHT:
                return button.rightClickShift();
            default:
                return false;
        }
    }

    public GUIItem enchantment(Enchantment ench, Integer level) {
        if (enchantments == null)
            enchantments = new HashMap<>();
        enchantments.put(ench, level);
        return this;
    }

    public GUIItem duplicateByConstructor() {
        return new GUIItem(item, amount, displayName);
    }

}