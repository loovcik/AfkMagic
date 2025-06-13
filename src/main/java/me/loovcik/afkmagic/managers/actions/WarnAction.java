package me.loovcik.afkmagic.managers.actions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.events.AFKWarnEvent;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;

import java.util.HashMap;
import java.util.Map;

import static me.loovcik.afkmagic.managers.ConfigurationManager.getRandom;
import static me.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;
import static me.loovcik.afkmagic.utils.Extensions.isOnWorld;

public class WarnAction extends Action
{
	/**
	 * {@inheritDoc}
	 * @param player Kontekst gracza
	 * @return True, jeśli należy ostrzec gracza, w przeciwnym razie false
	 */
	@Override
	public boolean check(AFKPlayer player){
		if (plugin.configuration.warn.use && !player.getPlayer().isOp() && !player.isVanished()) {
			if (player.isAFK()) {

				if(hasPlayerPermission(player.getPlayer(), plugin.configuration.warn.bypass.permissions)) return false;
				if (isOnWorld(player, plugin.configuration.warn.bypass.worlds)) return false;
				if (plugin.configuration.warn.bypass.gameMode.spectator && player.getGameMode().equals(GameMode.SPECTATOR)) return false;
				if (plugin.configuration.warn.bypass.gameMode.creative && player.getGameMode().equals(GameMode.CREATIVE)) return false;
				if (plugin.dependencies.worldGuard.isEnabled() && plugin.dependencies.worldGuard.isOnRegion(player, plugin.configuration.warn.bypass.regions.toArray(new String[0]))) return false;
				if (!player.isWarned())
					return player.getCurrentAfkTime() >= plugin.configuration.warn.time.toMilliseconds();
			}
		}
		return false;
	}

	/**
	 * Wysłanie ostrzeżenia dla gracza
	 * @param player Kontekst gracza
	 */
	@Override
	public void success(AFKPlayer player){
		player.setWarned(true);
		AFKWarnEvent event = new AFKWarnEvent(player);
		Bukkit.getScheduler().runTask(plugin, () -> {
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled())
			{
				String rMessage = getRandom(plugin.configuration.warn.messages);
				if (!rMessage.equalsIgnoreCase(""))
				{
					Map<String, String> replacements = new HashMap<>();
					replacements.put("%player%", player.getName());
					ChatHelper.message(player.getPlayer(), rMessage, replacements);
				}
				plugin.commandManager.run(plugin.configuration.warn.commands, player);
			}
		});
	}

	public WarnAction(AFKMagic plugin){
		super(plugin);
	}
}