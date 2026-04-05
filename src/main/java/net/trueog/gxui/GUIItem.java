package net.trueog.gxui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import net.trueog.utilitiesog.UtilitiesOG;

@Accessors(fluent = true)
public class GUIItem {

    @Getter
    @Setter
    private Material item;

    @Getter
    @Setter
    private int amount;

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private List<TextComponent> lore;

    private boolean isSkull;
    private String skullOwner;
    private HashMap<Enchantment, Integer> enchantments = new HashMap<>();

    @Getter
    @Setter
    private boolean unbreakable = false;

    @Getter
    @Setter
    private GUIButton button;

    @Getter
    @Setter
    private short type;

    @Getter
    @Setter
    private boolean playErrorSound;

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

    public GUIItem(Material item, int amount, GUIButton button, String displayName, String skullOwner) {

        this.item = item;
        this.amount = amount;
        this.button = button;
        this.displayName = displayName;
        this.skullOwner = skullOwner;

        isSkull = true;
        lore = new ArrayList<>();
        type = -1;
        playErrorSound = true;

    }

    public GUIButton getButton() {

        return button;

    }

    public void setButton(GUIButton button) {

        this.button = button;

    }

    /**
     * @return ItemStack
     */
    public ItemStack build() {

        final ItemStack i;
        final TextComponent displayNameTextComponent = UtilitiesOG.trueogExpand(displayName);
        final List<TextComponent> loreTextComponent = lore;
        try {

            if (!isSkull) {

                i = type != -1 ? new ItemStack(item, amount) : new ItemStack(item, amount);

                final ItemMeta m = i.getItemMeta();
                if (!"Not Set".equals(displayName)) {

                    m.displayName(displayNameTextComponent);

                }

                m.lore(loreTextComponent);

                if (!enchantments.isEmpty()) {

                    enchantments.entrySet().forEach(e -> m.addEnchant(e.getKey(), e.getValue(), true));

                }

                i.setItemMeta(m);

            } else {

                i = new ItemStack(item, amount);
                final SkullMeta m = (SkullMeta) i.getItemMeta();

                m.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwner));

                if (!"Not Set".equals(displayName)) {

                    m.displayName(displayNameTextComponent);

                }

                i.setUnbreakable(unbreakable);
                m.lore(loreTextComponent);
                i.setItemMeta(m);

            }

        } catch (Exception error) {

            error.printStackTrace();

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

        return switch (clickType) {

            case LEFT -> button.leftClick();
            case SHIFT_LEFT -> button.leftClickShift();
            case RIGHT -> button.rightClick();
            case SHIFT_RIGHT -> button.rightClickShift();
            default -> false;

        };

    }

    public GUIItem enchantment(Enchantment ench, Integer level) {

        if (enchantments == null) {

            enchantments = new HashMap<>();

        }

        enchantments.put(ench, level);

        return this;

    }

    public GUIItem duplicateByConstructor() {

        return new GUIItem(item, amount, displayName);

    }

}