// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.

package plugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

// Extending this class is standard bukkit boilerplate for any plugin, or else the server software won't load the classes.
public class TemplateOG extends JavaPlugin {

	private static TemplateOG plugin;

	// What to do when the plugin is run by the server.
	public void onEnable() {

		// Register the plugin instance.
		plugin = this;

		// Register the event.
		getServer().getPluginManager().registerEvents(new Listeners(), this);

		// Register the placeholder.
		registerExpansion();

	}

	public static TemplateOG getPlugin() {

		// Pass instance of main to other classes.
		return plugin;

	}

	public static void registerExpansion() {
		Expansion.Builder builder = Expansion.builder("diamondbank_og_balance");
		builder.filter(Player.class);

		builder.audiencePlaceholder("name", (audience, ctx, queue) -> {
			final Player player = (Player) audience;
			final TagResolver playerResolver = MiniPlaceholders.getAudiencePlaceholders(player);
			Utils.diamondBankOGPlaceholderMessage(player, "&BYour balance is: " + checkPlayerBalance(player.getUniqueId()), playerResolver);
			TextComponent nameHandler = LegacyComponentSerializer.legacyAmpersand().deserialize(player.getName());
			return Tag.selfClosingInserting(nameHandler);
		}).globalPlaceholder("tps", (ctx, queue) -> Tag.selfClosingInserting(Component.text(Bukkit.getTPS()[0]))).build();

		Expansion expansion = builder.build();
		expansion.register();

	}

	public static double checkPlayerBalance(UUID playerUUID) {

		/*DiamondBankAPI diamondBankPlugin = Bukkit.getPluginManager().getPlugin("DiamondBankOG");

		diamondBankPlugin.getPlayerBalance(playerUUID)
		.thenAccept(balance -> {
			return balance;
		}).exceptionally(error -> {
			// Handle potential errors...
			getLogger().error("Failed to get player balance", error);
			return null; 
		});*/
		
		return 1.0;

	}

}