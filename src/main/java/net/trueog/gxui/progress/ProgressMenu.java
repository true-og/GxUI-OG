package net.trueog.gxui.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.trueog.gxui.GUIBase;
import net.trueog.gxui.GUIButton;
import net.trueog.gxui.GUIItem;

// A layered inventory for groups of material-backed goals. Goals are read-only; a
// section may expose one clickable action button on its detail view.
public final class ProgressMenu extends GUIBase {

    public static final int INVENTORY_SIZE = 54;
    public static final int CONTENT_PER_PAGE = 45;
    public static final int BACK_SLOT = 45;
    public static final int PREVIOUS_PAGE_SLOT = 47;
    public static final int CLOSE_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 51;
    public static final int ACTION_SLOT = 52;
    public static final int INFORMATION_SLOT = 53;

    // How long the action slot shows an error before the action button returns.
    private static final long ACTION_ERROR_TICKS = 40L;

    private static final List<Integer> CONTENT_SLOTS = createContentSlots();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final JavaPlugin plugin;
    private final String baseTitle;
    private final List<Section> sections;
    private final int sectionIndex;
    private final int page;

    // True while the action slot displays an error item; clicks on it are ignored.
    private boolean actionErrorActive = false;

    private ProgressMenu(JavaPlugin plugin, Player player, String baseTitle, List<Section> sections, int sectionIndex,
            int page)
    {

        super(plugin, player, buildInventoryName(baseTitle, sections, sectionIndex, page), INVENTORY_SIZE, true);

        this.plugin = plugin;
        this.baseTitle = baseTitle;
        this.sections = sections;
        this.sectionIndex = normalizeSectionIndex(sectionIndex, sections.size());
        this.page = normalizePage(page, getTotalPages(getVisibleItemCount(sections, this.sectionIndex)));

    }

    public static Builder builder(JavaPlugin plugin, Player player, String inventoryName) {

        return new Builder(plugin, player, inventoryName);

    }

    // true when this menu is displaying one section's goals.
    public boolean isDetailView() {

        return sectionIndex >= 0;

    }

    // Get the numeric page number (0 index).
    public int getPage() {

        return page;

    }

    public int getTotalPages() {

        return getTotalPages(getVisibleItemCount(sections, sectionIndex));

    }

    @Override
    public void setupItems() {

        if (isDetailView()) {

            setupDetailItems();

        } else {

            setupIndexItems();

        }

        setupNavigationItems();

    }

    private void setupIndexItems() {

        final int firstSection = page * CONTENT_PER_PAGE;
        final int lastSection = Math.min(firstSection + CONTENT_PER_PAGE, sections.size());
        for (int currentSection = firstSection; currentSection < lastSection; currentSection++) {

            final int pageIndex = currentSection - firstSection;

            addItem(CONTENT_SLOTS.get(pageIndex), buildSectionItem(currentSection));

        }

    }

    private void setupDetailItems() {

        final Section section = sections.get(sectionIndex);
        final List<Goal> goals = section.goals();
        final int firstGoal = page * CONTENT_PER_PAGE;
        final int lastGoal = Math.min(firstGoal + CONTENT_PER_PAGE, goals.size());
        for (int currentGoal = firstGoal; currentGoal < lastGoal; currentGoal++) {

            final int pageIndex = currentGoal - firstGoal;
            addItem(CONTENT_SLOTS.get(pageIndex), buildGoalItem(goals.get(currentGoal)));

        }

        if (!section.description().isEmpty()) {

            addItem(INFORMATION_SLOT, buildInformationItem(section));

        }

        if (section.action() != null) {

            addItem(ACTION_SLOT, buildActionItem(section.action()));

        }

    }

    // Temporarily replace this detail view's action button with an error item.
    // Main thread only; a no-op once the menu is closed.
    public void showActionError(String message) {

        Objects.requireNonNull(message, "message");

        if (!isDetailView()) {

            return;

        }

        final Action action = sections.get(sectionIndex).action();
        final Inventory inventory = getInventory();
        if (action == null || inventory == null || !isViewing(inventory)) {

            return;

        }

        actionErrorActive = true;

        final GUIItem errorItem = new GUIItem(Material.RED_STAINED_GLASS_PANE, 1, "&c&lError");
        errorItem.lore(toLoreComponents(addLinebreaks(message, 30, "&7")));
        inventory.setItem(ACTION_SLOT, errorItem.build());
        playActionErrorSound();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            actionErrorActive = false;

            if (isViewing(inventory)) {

                inventory.setItem(ACTION_SLOT, buildActionItem(action).build());

            }

        }, ACTION_ERROR_TICKS);

    }

    private boolean isViewing(Inventory inventory) {

        return getPlayer().getOpenInventory().getTopInventory() == inventory;

    }

    private void playActionErrorSound() {

        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 100f, 0.7f);

    }

    private void setupNavigationItems() {

        if (isDetailView()) {

            addItem(BACK_SLOT, buildBackButton());

        }

        if (page > 0) {

            addItem(PREVIOUS_PAGE_SLOT, buildPageButton("&ePrevious Page", page - 1));

        }

        addItem(CLOSE_SLOT, buildCloseButton());

        if (page + 1 < getTotalPages()) {

            addItem(NEXT_PAGE_SLOT, buildPageButton("&eNext Page", page + 1));

        }

    }

    private GUIItem buildSectionItem(int targetSection) {

        final Section section = sections.get(targetSection);
        final State state = section.state();
        final GUIItem guiItem = new GUIItem(state.getMaterial(), 1, state.getColor() + section.name());

        final List<String> lore = new ArrayList<>();
        lore.add(state.getStatusLine());

        if (!section.description().isEmpty()) {

            lore.add("");
            lore.addAll(section.description());

        }

        lore.add("");
        lore.add("&eClick to view requirements.");

        guiItem.lore(toLoreComponents(lore));
        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> openSection(targetSection, 0)));

        return guiItem;

    }

    private GUIItem buildGoalItem(Goal goal) {

        final String color = goal.complete() ? "&a" : "&c";
        final GUIItem guiItem = new GUIItem(goal.material(), 1, color + goal.name());

        final List<String> lore = new ArrayList<>();
        lore.add(goal.complete() ? "&aCompleted" : "&cIncomplete");
        lore.add("");

        if (goal.hasProgress()) {

            lore.add("&7Progress: &f" + goal.current() + "&7/&f" + goal.target());

        } else {

            lore.add("&7Status: &f" + (goal.complete() ? "Complete" : "Incomplete"));

        }

        guiItem.lore(toLoreComponents(lore));
        guiItem.playErrorSound(false);

        return guiItem;

    }

    private GUIItem buildInformationItem(Section section) {

        final GUIItem guiItem = new GUIItem(Material.PAPER, 1, "&eInformation");

        guiItem.lore(toLoreComponents(section.description()));
        guiItem.playErrorSound(false);

        return guiItem;

    }

    private GUIItem buildActionItem(Action action) {

        final GUIItem guiItem = new GUIItem(action.material(), 1, action.name());

        guiItem.lore(toLoreComponents(action.lore()));
        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> {

            if (actionErrorActive) {

                playActionErrorSound();

                return;

            }

            action.handler().accept(this, getPlayer());

        }));

        return guiItem;

    }

    private GUIItem buildBackButton() {

        final GUIItem guiItem = new GUIItem(Material.ARROW, 1, "&eBack to Menu");

        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> openIndex(getIndexPage(sectionIndex))));

        return guiItem;

    }

    private GUIItem buildCloseButton() {

        final GUIItem guiItem = new GUIItem(Material.BARRIER, 1, "&cClose");

        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> getPlayer().closeInventory()));

        return guiItem;

    }

    private GUIItem buildPageButton(String displayName, int targetPage) {

        final GUIItem guiItem = new GUIItem(Material.ARROW, 1, displayName);

        guiItem.lore(toLoreComponents(List.of("&7Page " + (targetPage + 1) + " of " + getTotalPages())));
        guiItem.playErrorSound(false);
        guiItem.button(createButton(() -> openCurrentLayer(targetPage)));

        return guiItem;

    }

    private void openCurrentLayer(int targetPage) {

        if (isDetailView()) {

            openSection(sectionIndex, targetPage);

        } else {

            openIndex(targetPage);

        }

    }

    private void openIndex(int targetPage) {

        new ProgressMenu(plugin, getPlayer(), baseTitle, sections, -1, targetPage).open(false);

    }

    private void openSection(int targetSection, int targetPage) {

        new ProgressMenu(plugin, getPlayer(), baseTitle, sections, targetSection, targetPage).open(false);

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

    private static List<TextComponent> toLoreComponents(List<String> lore) {

        return lore.stream().map(ProgressMenu::toTextComponent).toList();

    }

    private static TextComponent toTextComponent(String line) {

        final Component component = LEGACY.deserialize(line);

        return (TextComponent) component;

    }

    private static String buildInventoryName(String baseTitle, List<Section> sections, int sectionIndex, int page) {

        Objects.requireNonNull(baseTitle, "baseTitle");
        Objects.requireNonNull(sections, "sections");
        final int normalizedSection = normalizeSectionIndex(sectionIndex, sections.size());
        final String title = normalizedSection < 0 ? baseTitle : sections.get(normalizedSection).name();
        final int itemCount = getVisibleItemCount(sections, normalizedSection);
        final int totalPages = getTotalPages(itemCount);
        if (totalPages == 1) {

            return title;

        }

        final int normalizedPage = normalizePage(page, totalPages);
        return title + " &7(" + (normalizedPage + 1) + "/" + totalPages + ")";

    }

    private static int getVisibleItemCount(List<Section> sections, int sectionIndex) {

        return sectionIndex < 0 ? sections.size() : sections.get(sectionIndex).goals().size();

    }

    private static int getTotalPages(int itemCount) {

        return Math.max(1, (int) Math.ceil((double) itemCount / CONTENT_PER_PAGE));

    }

    private static int normalizePage(int page, int totalPages) {

        if (page < 0) {

            return 0;

        }

        return page >= totalPages ? totalPages - 1 : page;

    }

    private static int normalizeSectionIndex(int sectionIndex, int sectionCount) {

        return sectionIndex >= 0 && sectionIndex < sectionCount ? sectionIndex : -1;

    }

    private static int getIndexPage(int sectionIndex) {

        return sectionIndex / CONTENT_PER_PAGE;

    }

    private static List<Integer> createContentSlots() {

        final List<Integer> slots = new ArrayList<>(CONTENT_PER_PAGE);
        for (int row = 0; row < 5; row++) {

            for (int column = 0; column < 9; column++) {

                slots.add(row * 9 + column);

            }

        }

        return Collections.unmodifiableList(slots);

    }

    // Section state and its fixed first-layer wool material.
    public enum State {

        NOT_STARTED(Material.RED_WOOL, "&c", "&cNot Started"), IN_PROGRESS(Material.YELLOW_WOOL, "&e", "&eIn Progress"),
        COMPLETE(Material.LIME_WOOL, "&a", "&aCompleted");

        private final Material material;
        private final String color;
        private final String statusLine;

        State(Material material, String color, String statusLine) {

            this.material = material;
            this.color = color;
            this.statusLine = statusLine;

        }

        private Material getMaterial() {

            return material;

        }

        private String getColor() {

            return color;

        }

        private String getStatusLine() {

            return statusLine;

        }

    }

    private record Goal(Material material, String name, boolean complete, long current, long target,
            boolean hasProgress)
    {

        private Goal(Material material, String name, boolean complete) {

            this(material, name, complete, 0L, 0L, false);

        }

        private Goal(Material material, String name, long current, long target) {

            this(material, name, current >= target, current, target, true);

        }

        private Goal {

            Objects.requireNonNull(material, "material");
            Objects.requireNonNull(name, "name");

        }

    }

    // A clickable button shown at ACTION_SLOT on a section's detail view.
    private record Action(Material material, String name, List<String> lore, BiConsumer<ProgressMenu, Player> handler) {

        private Action {

            Objects.requireNonNull(material, "material");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(handler, "handler");

            lore = List.copyOf(Objects.requireNonNull(lore, "lore"));

        }

    }

    // action is null when the section has no button.
    private record Section(String name, State state, List<Goal> goals, List<String> description, Action action) {

        private Section {

            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(state, "state");

            goals = List.copyOf(Objects.requireNonNull(goals, "goals"));
            description = List.copyOf(Objects.requireNonNull(description, "description"));

        }

    }

    // Builder for layered ProgressMenu.
    public static final class Builder {

        private final JavaPlugin plugin;
        private final Player player;
        private final String title;
        private final List<Section> sections = new ArrayList<>();

        private String pendingName;
        private State pendingState;
        private List<Goal> pendingGoals;
        private List<String> pendingDescription;
        private Action pendingAction;

        private Builder(JavaPlugin plugin, Player player, String title) {

            this.plugin = Objects.requireNonNull(plugin, "plugin");
            this.player = Objects.requireNonNull(player, "player");
            this.title = Objects.requireNonNull(title, "title");

        }

        // Start a section. Its first-layer item is always the state's color of wool
        // (red, yellow, green).
        public Builder section(String name, State state) {

            flushPending();

            pendingName = Objects.requireNonNull(name, "name");
            pendingState = Objects.requireNonNull(state, "state");
            pendingGoals = new ArrayList<>();
            pendingDescription = new ArrayList<>();
            pendingAction = null;

            return this;

        }

        // A counter for which completion is derived from current >= target.
        public Builder progress(Material material, String name, long current, long target) {

            requirePending();

            pendingGoals.add(new Goal(material, name, current, target));

            return this;

        }

        public Builder goal(Material material, String name, boolean complete) {

            requirePending();

            pendingGoals.add(new Goal(material, name, complete));

            return this;

        }

        public Builder description(String... lines) {

            requirePending();

            Collections.addAll(pendingDescription, lines);

            return this;

        }

        // Give the current section one clickable button at ACTION_SLOT of its detail
        // view; calling it again replaces the button. The handler receives the clicked
        // menu instance so it can call showActionError.
        public Builder action(Material material, String name, List<String> lore,
                BiConsumer<ProgressMenu, Player> handler)
        {

            requirePending();

            pendingAction = new Action(material, name, lore, handler);

            return this;

        }

        public Builder action(Material material, String name, List<String> lore, Consumer<Player> handler) {

            Objects.requireNonNull(handler, "handler");

            return action(material, name, lore, (menu, clicker) -> handler.accept(clicker));

        }

        // Open the first-layer section.
        public ProgressMenu open() {

            flushPending();

            return openAt(-1);

        }

        // Open straight into one section's detail view (0-indexed, in the order the
        // sections were added). Out-of-range indexes fall back to the first layer.
        public ProgressMenu openSection(int sectionIndex) {

            flushPending();

            return openAt(sectionIndex);

        }

        private ProgressMenu openAt(int sectionIndex) {

            final ProgressMenu menu = new ProgressMenu(plugin, player, title, List.copyOf(sections), sectionIndex, 0);
            menu.open(false);

            return menu;

        }

        private void requirePending() {

            if (pendingName == null) {

                throw new IllegalStateException("Call section(name, state) before adding goals or descriptions");

            }

        }

        private void flushPending() {

            if (pendingName == null) {

                return;

            }

            sections.add(new Section(pendingName, pendingState, pendingGoals, pendingDescription, pendingAction));

            pendingName = null;
            pendingState = null;
            pendingGoals = null;
            pendingDescription = null;
            pendingAction = null;

        }

    }

}