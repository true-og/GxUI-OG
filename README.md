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

## Advancement API

`AdvancementMenu` displays an ordered list of read-only advancement items in a paginated inventory. Items are placed
from the top-left slot downward through each column, reserving the bottom row for previous, close, and next controls.

Java:

```java
Map<String, List<String>> values = new LinkedHashMap<>();
values.put("&cHome Two", List.of("&7Unlock the second home.", "&eRequired: &fWin 1 duel"));
values.put("&cHome Three", List.of("&7Unlock the third home.", "&eRequired: &fBuild a full beacon"));

AdvancementMenu.open(plugin, player, "&6Advancements", values);
```

Kotlin:

```kotlin
val values = linkedMapOf(
    "&cHome Two" to listOf("&7Unlock the second home.", "&eRequired: &fWin 1 duel"),
    "&cHome Three" to listOf("&7Unlock the third home.", "&eRequired: &fBuild a full beacon"),
)

AdvancementMenu.open(plugin, player, "&6Advancements", values)
```

The simple map API displays every entry as `RED_WOOL`. Use an ordered map, such as `LinkedHashMap`, when item order
matters. Callers that need custom state or icons can still pass `List<AdvancementItem>` directly.

## License

Public domain.
