package net.trueog.gxui.advancements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.trueog.gxui.GUIBase;
import net.trueog.gxui.GUIButton;
import net.trueog.gxui.GUIItem;

// Read-only paginated inventory for advancement-style progress displays.
// Construct with AdvancementMenu.builder(plugin, player, title). Run open() to display.
public class AdvancementMenu extends GUIBase {

    public static final int INVENTORY_SIZE = 54;
    public static final int ADVANCEMENTS_PER_PAGE = 45;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int CLOSE_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 53;

    private static final List<Integer> ADVANCEMENT_SLOTS = createAdvancementSlots();

    private final JavaPlugin plugin;
    private final String baseTitle;
    private final List<AdvancementItem> advancementItems;
    private final int page;

    private AdvancementMenu(JavaPlugin plugin, Player player, String inventoryName,
            List<AdvancementItem> advancementItems, int page)
    {

        super(plugin, player, buildInventoryName(inventoryName, advancementItems, page), INVENTORY_SIZE, true);
        this.plugin = plugin;
        this.baseTitle = inventoryName;
        this.advancementItems = advancementItems;
        this.page = normalizePage(page, getTotalPages(advancementItems.size()));

    }

    public static Builder builder(JavaPlugin plugin, Player player, String inventoryName) {

        return new Builder(plugin, player, inventoryName);

    }

    public int getPage() {

        return page;

    }

    public int getTotalPages() {

        return getTotalPages(advancementItems.size());

    }

    @Override
    public void setupItems() {

        final int firstAdvancement = page * ADVANCEMENTS_PER_PAGE;
        final int lastAdvancement = Math.min(firstAdvancement + ADVANCEMENTS_PER_PAGE, advancementItems.size());
        for (int advancementIndex = firstAdvancement; advancementIndex < lastAdvancement; advancementIndex++) {

            final int pageIndex = advancementIndex - firstAdvancement;
            addItem(ADVANCEMENT_SLOTS.get(pageIndex), buildAdvancementItem(advancementItems.get(advancementIndex)));

        }

        if (page > 0) {

            addItem(PREVIOUS_PAGE_SLOT, buildPageButton(Material.ARROW, "&ePrevious Page", page - 1));

        }

        addItem(CLOSE_SLOT, buildCloseButton());

        if (page + 1 < getTotalPages()) {

            addItem(NEXT_PAGE_SLOT, buildPageButton(Material.ARROW, "&eNext Page", page + 1));

        }

    }

    private GUIItem buildAdvancementItem(AdvancementItem advancementItem) {

        final AdvancementState state = advancementItem.getState();
        final String displayName = state.getColor() + advancementItem.getName();
        final GUIItem guiItem = new GUIItem(state.getDefaultMaterial(), 1, displayName);

        guiItem.lore(toLoreComponents(buildLore(advancementItem)));
        guiItem.playErrorSound(false);

        return guiItem;

    }

    private static List<String> buildLore(AdvancementItem advancementItem) {

        final List<String> lore = new ArrayList<>();
        lore.add(advancementItem.getState().getStatusLine());

        final List<AdvancementAttribute> attributes = advancementItem.getAttributes();
        if (!attributes.isEmpty()) {

            lore.add("");
            lore.add("&7Requirements:");
            attributes.forEach(attribute -> lore.add(formatAttribute(attribute)));

        }

        final List<String> footer = advancementItem.getFooter();
        if (!footer.isEmpty()) {

            lore.add("");
            lore.addAll(footer);

        }

        return lore;

    }

    private static String formatAttribute(AdvancementAttribute attribute) {

        final String color = attribute.isDone() ? "&a" : "&c";
        if (attribute.hasProgress()) {

            return color + attribute.getName() + ": &f" + attribute.getCurrent() + "&7/&f" + attribute.getTarget();

        }

        final String status = attribute.isDone() ? "Done" : "Not done";
        return color + attribute.getName() + ": &f" + status;

    }

    private GUIItem buildCloseButton() {

        final GUIItem guiItem = new GUIItem(Material.BARRIER, 1, "&cClose");
        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> getPlayer().closeInventory()));

        return guiItem;

    }

    private GUIItem buildPageButton(Material material, String displayName, int targetPage) {

        final GUIItem guiItem = new GUIItem(material, 1, displayName);
        guiItem.lore(toLoreComponents(List.of("&7Page " + (targetPage + 1) + " of " + getTotalPages())));
        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> openPage(targetPage)));

        return guiItem;

    }

    private void openPage(int targetPage) {

        new AdvancementMenu(plugin, getPlayer(), baseTitle, advancementItems, targetPage).open(false);

    }

    private GUIButton createButton(Runnable clickHandler) {

        return new GUIButton() {

            @Override
            public boolean leftClick() {

                return handleClick();

            }

            @Override
            public boolean leftClickShift() {

                return handleClick();

            }

            @Override
            public boolean rightClick() {

                return handleClick();

            }

            @Override
            public boolean rightClickShift() {

                return handleClick();

            }

            private boolean handleClick() {

                clickHandler.run();
                return true;

            }

        };

    }

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private List<TextComponent> toLoreComponents(List<String> lore) {

        return lore.stream().map(AdvancementMenu::toTextComponent).toList();

    }

    private static TextComponent toTextComponent(String line) {

        final Component component = LEGACY.deserialize(line);
        // LegacyComponentSerializer always produces a TextComponent root.
        return (TextComponent) component;

    }

    private static String buildInventoryName(String inventoryName, List<AdvancementItem> advancementItems, int page) {

        Objects.requireNonNull(inventoryName, "inventoryName");
        final int totalPages = getTotalPages(Objects.requireNonNull(advancementItems, "advancementItems").size());
        if (totalPages == 1) {

            return inventoryName;

        }

        final int normalizedPage = normalizePage(page, totalPages);
        return inventoryName + " &7(" + (normalizedPage + 1) + "/" + totalPages + ")";

    }

    private static int getTotalPages(int itemCount) {

        return Math.max(1, (int) Math.ceil((double) itemCount / ADVANCEMENTS_PER_PAGE));

    }

    private static int normalizePage(int page, int totalPages) {

        if (page < 0) {

            return 0;

        }

        return page >= totalPages ? totalPages - 1 : page;

    }

    private static List<Integer> createAdvancementSlots() {

        final List<Integer> slots = new ArrayList<>(ADVANCEMENTS_PER_PAGE);
        // Row-first ordering: fill left-to-right across the top row, then wrap down.
        for (int row = 0; row < 5; row++) {

            for (int column = 0; column < 9; column++) {

                slots.add(row * 9 + column);

            }

        }

        return Collections.unmodifiableList(slots);

    }

    private static int parsePage(String[] args) {

        if (args == null || args.length == 0) {

            return 0;

        }

        try {

            return Math.max(0, Integer.parseInt(args[0]) - 1);

        } catch (NumberFormatException ignored) {

            return 0;

        }

    }

    // Fluent builder for an AdvancementMenu. Plugins only depend on this class,
    // AdvancementState, and Java primitives.
    public static final class Builder {

        private final JavaPlugin plugin;
        private final Player player;
        private final String title;
        private final List<AdvancementItem> items = new ArrayList<>();

        private String pendingName;
        private AdvancementState pendingState;
        private List<AdvancementAttribute> pendingAttributes;
        private List<String> pendingFooter;

        private Builder(JavaPlugin plugin, Player player, String title) {

            this.plugin = Objects.requireNonNull(plugin, "plugin");
            this.player = Objects.requireNonNull(player, "player");
            this.title = Objects.requireNonNull(title, "title");

        }

        // Start a new advancement slot. Following attribute/footer calls attach to this
        // one.
        public Builder add(String name, AdvancementState state) {

            flushPending();
            this.pendingName = Objects.requireNonNull(name, "name");
            this.pendingState = Objects.requireNonNull(state, "state");
            this.pendingAttributes = new ArrayList<>();
            this.pendingFooter = new ArrayList<>();
            return this;

        }

        // Add a progress-counter attribute (e.g. 250/1000). Coloured by current >=
        // target.
        public Builder progress(String name, long current, long target) {

            requirePending();
            pendingAttributes.add(new AdvancementAttribute(name, current, target));
            return this;

        }

        // Add a boolean attribute.
        public Builder bool(String name, boolean done) {

            requirePending();
            pendingAttributes.add(new AdvancementAttribute(name, done));
            return this;

        }

        // Append free-form footer lines (formatting allowed).
        public Builder footer(String... lines) {

            requirePending();
            Collections.addAll(pendingFooter, lines);
            return this;

        }

        // Open at the page parsed from command args (e.g. "/cmd 2").
        public AdvancementMenu open(String[] args) {

            return openAt(parsePage(args));

        }

        // Open at page 1.
        public AdvancementMenu open() {

            return openAt(0);

        }

        private AdvancementMenu openAt(int page) {

            flushPending();
            final AdvancementMenu menu = new AdvancementMenu(plugin, player, title, List.copyOf(items), page);
            menu.open(false);
            return menu;

        }

        private void requirePending() {

            if (pendingName == null) {

                throw new IllegalStateException("Call add(name, state) before adding attributes or footer lines");

            }

        }

        private void flushPending() {

            if (pendingName == null) {

                return;

            }

            items.add(new AdvancementItem(pendingName, pendingState, pendingAttributes, pendingFooter));
            pendingName = null;
            pendingState = null;
            pendingAttributes = null;
            pendingFooter = null;

        }

    }

}