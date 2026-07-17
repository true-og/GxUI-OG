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

`ProgressMenu` displays a layered, read-only progress inventory. The first layer lists sections. Clicking a section
opens a second layer containing one material-backed item per goal. Hovering a goal shows its completion state and,
for counter goals, its current and target values. Both layers paginate automatically.

Java:

```java
ProgressMenu.builder(plugin, player, "&aHome Quests")
    .section("Home 2 Quest", ProgressMenu.State.IN_PROGRESS)
        .progress(Material.DIAMOND, "Total Shards", 450, 900)
        .progress(Material.CLOCK, "Hours Played", 12, 24)
        .description("&7Use &e/claimquest &7when ready.")
    .section("Home 6 Quest", ProgressMenu.State.NOT_STARTED)
        .goal(Material.RED_CONCRETE, "How Did We Get Here?", false)
    .open();
```

Required imports:

```java
import net.trueog.gxui.progress.ProgressMenu;
import org.bukkit.Material;
```

Kotlin:

```kotlin
ProgressMenu.builder(plugin, player, "&aHome Quests")
    .section("Home 2 Quest", ProgressMenu.State.IN_PROGRESS)
    .progress(Material.DIAMOND, "Total Shards", 450, 900)
    .progress(Material.CLOCK, "Hours Played", 12, 24)
    .description("&7Use &e/claimquest &7when ready.")
    .section("Home 6 Quest", ProgressMenu.State.NOT_STARTED)
    .goal(Material.RED_CONCRETE, "How Did We Get Here?", false)
    .open()
```

Section icons are always state wool: red for `NOT_STARTED`, yellow for `IN_PROGRESS`, and lime for `COMPLETE`. Goal
icons never change with state; completion is communicated through the display name and hover lore. This keeps each
block related to the goal it represents while making the first layer immediately scannable.

## License

Public domain.
