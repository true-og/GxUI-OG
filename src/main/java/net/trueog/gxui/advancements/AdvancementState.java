package net.trueog.gxui.advancements;

import org.bukkit.Material;

// Visual state of an advancement slot. Drives material colour and lore status line.
public enum AdvancementState {

    COMPLETE(Material.LIME_WOOL, "&a", "&aCompleted"), IN_PROGRESS(Material.YELLOW_WOOL, "&e", "&eIn Progress"),
    LOCKED(Material.RED_WOOL, "&c", "&cLocked");

    private final Material defaultMaterial;
    private final String color;
    private final String statusLine;

    AdvancementState(Material defaultMaterial, String color, String statusLine) {

        this.defaultMaterial = defaultMaterial;
        this.color = color;
        this.statusLine = statusLine;

    }

    public Material getDefaultMaterial() {

        return defaultMaterial;

    }

    String getColor() {

        return color;

    }

    String getStatusLine() {

        return statusLine;

    }

}
