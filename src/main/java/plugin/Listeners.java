// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.
package plugin;

// Import libraries.
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

// Hook into Bukkit's Listener.
public class Listeners implements Listener {

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {

		// Utilities_OG utilities = (Utilities_OG) Bukkit.getPluginManager().getPlugin("Utilities-OG");
		new SpectatorGui(TemplateOG.getPlugin(), event.getPlayer()).open(true);

	}

}
