package me.gabriella.gabsgui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
public class GUIItem {

    @Getter @Setter private Material item;
    @Getter @Setter private int amount;
    @Getter @Setter private String displayName;
    @Getter @Setter private ArrayList<String> lore;

    private boolean isSkull;
    private String skullOwner;

    private HashMap<Enchantment, Integer> enchantments = new HashMap<>();

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

    public ItemStack build() {
        ItemStack i;
        if (!isSkull) {
            if (type != -1)
                i = new ItemStack(item, amount, type);
            else
                i = new ItemStack(item, amount);
            ItemMeta m = i.getItemMeta();
            if (!displayName.equals("Not Set"))
                m.setDisplayName(displayName);
            m.setLore(lore);

            if (!enchantments.isEmpty())
                for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet())
                    m.addEnchant(e.getKey(), e.getValue(), true);

            i.setItemMeta(m);
        } else {
            i = new ItemStack(item, amount, (short) 3);
            SkullMeta m = (SkullMeta) i.getItemMeta();
            m.setOwner(skullOwner);
            if (!displayName.equals("Not Set"))
                m.setDisplayName(displayName);
            m.setLore(lore);

            i.setItemMeta(m);
        }

        return i;
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
}