# GxUI-OG

[TrueOG Network](https://true-og.net)'s GUI Library, originally written by @Gxorge.

See [Template-OG](https://github.com/true-og/Template-OG) or [KotlinTemplate-OG](https://github.com/true-og/KotlinTemplate-OG) usage and import examples.

## Building

> ./gradlew clean build eclipse --warning-mode all

## Plugins using GxUI-OG

[DuelEnhancer-OG](https://github.com/true-og/duelenhancer-og)
[Splegg-OG](https://github.com/true-og/splegg-og)
[SudoSigns-OG](https://github.com/true-og/sudosigns-og)
[TheHerobrine-OG](https://github.com/true-og/TheHerobrine-OG/)

## Core GUI API

Extend `GUIBase`, add `GUIItem`s in `setupItems()`, and call `open(false)`.

Java:

```java
public final class ExampleMenu extends GUIBase {
    public ExampleMenu(JavaPlugin plugin, Player player) {
        super(plugin, player, "&6Example Menu", 27, true);
    }

    @Override
    public void setupItems() {
        GUIItem item = new GUIItem(Material.DIAMOND, 1, "&bReward");
        item.lore(List.of(UtilitiesOG.trueogExpand("&7Click to claim.")));
        item.button(new GUIButton() {
            @Override public boolean leftClick() { getPlayer().sendMessage("Claimed"); return true; }
            @Override public boolean leftClickShift() { return leftClick(); }
            @Override public boolean rightClick() { return leftClick(); }
            @Override public boolean rightClickShift() { return leftClick(); }
        });

        addItem(13, item);
    }
}

new ExampleMenu(plugin, player).open(false);
```

Kotlin:

```kotlin
class ExampleMenu(plugin: JavaPlugin, player: Player) : GUIBase(plugin, player, "&6Example Menu", 27, true) {
    override fun setupItems() {
        val item = GUIItem(Material.DIAMOND, 1, "&bReward")
        item.lore(listOf(UtilitiesOG.trueogExpand("&7Click to claim.")))
        item.button(object : GUIButton {
            override fun leftClick(): Boolean { player.sendMessage("Claimed"); return true }
            override fun leftClickShift() = leftClick()
            override fun rightClick() = leftClick()
            override fun rightClickShift() = leftClick()
        })

        addItem(13, item)
    }
}

ExampleMenu(plugin, player).open(false)
```

Notes:

* `fillEmpty = true` fills unused slots with gray glass panes.
* GUI item clicks are cancelled automatically.
* A `GUIButton` method should return `true` when the click was handled.
* Non-button items can call `item.playErrorSound(false)` to make clicks silent.

## Progress menu API

`ProgressMenu` displays a layered progress inventory. The first layer lists sections. Clicking a section opens a
second layer containing one material-backed item per goal. Hovering a goal shows its completion state and, for
counter goals, its current and target values. Both layers paginate automatically. Goals themselves are read-only;
a section may optionally expose one clickable action button on its detail view.

Java:

```java
ProgressMenu.builder(plugin, player, "&aHome Quests")
    .section("Home 2 Quest", ProgressMenu.State.IN_PROGRESS)
        .progress(Material.DIAMOND, "Total Shards", 450, 900)
        .progress(Material.CLOCK, "Hours Played", 12, 24)
        .description("&7Click &aClaim &7when ready.")
        .action(Material.NETHER_STAR, "&aClaim Quest", List.of("&7Claims this quest."), (menu, clicker) -> {
            if (!canClaim(clicker)) {
                menu.showActionError("You must meet all requirements first.");
                return;
            }
            claim(clicker);
            clicker.closeInventory();
        })
    .section("Home 6 Quest", ProgressMenu.State.NOT_STARTED)
        .goal(Material.RED_CONCRETE, "How Did We Get Here?", false)
    .open();
```

Required imports:

```java
import java.util.List;
import net.trueog.gxui.progress.ProgressMenu;
import org.bukkit.Material;
```

Kotlin:

```kotlin
ProgressMenu.builder(plugin, player, "&aHome Quests")
    .section("Home 2 Quest", ProgressMenu.State.IN_PROGRESS)
    .progress(Material.DIAMOND, "Total Shards", 450, 900)
    .progress(Material.CLOCK, "Hours Played", 12, 24)
    .description("&7Click &aClaim &7when ready.")
    .action(Material.NETHER_STAR, "&aClaim Quest", listOf("&7Claims this quest.")) { menu, clicker ->
        if (!canClaim(clicker)) {
            menu.showActionError("You must meet all requirements first.")
        } else {
            claim(clicker)
            clicker.closeInventory()
        }
    }
    .section("Home 6 Quest", ProgressMenu.State.NOT_STARTED)
    .goal(Material.RED_CONCRETE, "How Did We Get Here?", false)
    .openSection(0) // Open straight into "Home 2 Quest"; use open() for the section list.
```

### Actions

- `action(material, name, lore, handler)` gives the current section one button, rendered at `ProgressMenu.ACTION_SLOT`
  (slot 52, bottom row) on that section's detail view only. Calling it again for the same section replaces the button.
  `handler` is either a `BiConsumer<ProgressMenu, Player>` or a `Consumer<Player>`; in Kotlin the lambda's arity picks
  the overload. Every page change creates a fresh `ProgressMenu`, so use the `menu` argument (not a captured instance)
  when you need to talk back to the open inventory.
- `openSection(index)` opens the builder directly in the detail view of the `index`-th section (0-based, in the order
  they were added). Out-of-range indexes fall back to the first layer. The Back button still returns to the section
  list.
- `menu.showActionError(message)` swaps the action button for a red "Error" pane carrying `message` for about two
  seconds, plays an error sound, and ignores clicks on the slot until the button returns. Call it on the main thread
  while the menu is still open; otherwise it does nothing.

Section icons are always state wool: red for `NOT_STARTED`, yellow for `IN_PROGRESS`, and lime for `COMPLETE`. Goal
icons never change with state; completion is communicated through the display name and hover lore. This keeps each
block related to the goal it represents while making the first layer immediately scannable.

## License

Public domain.
