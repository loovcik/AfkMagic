package me.loovcik.afkmagic.managers.actions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.events.AFKKickEvent;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.types.Time;
import me.loovcik.core.ChatHelper;

import java.util.HashMap;
import java.util.Map;

import static me.loovcik.afkmagic.managers.ConfigurationManager.getRandom;
import static me.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;
import static me.loovcik.afkmagic.utils.Extensions.isOnWorld;

public class KickAction extends Action
{
	/**
	 * {@inheritDoc}
	 * @param player Kontekst gracza
	 * @return True, jeśli należy ostrzec gracza, w przeciwnym razie false
	 */
	@Override
	public boolean check(AFKPlayer player){
		if (plugin.configuration.kick.use && !player.getPlayer().isOp() && !player.isVanished()) {
			if (player.isAFK()) {
				if(hasPlayerPermission(player.getPlayer(), plugin.configuration.kick.bypass.permissions)) return false;
				if (isOnWorld(player, plugin.configuration.kick.bypass.worlds)) return false;
				if (plugin.configuration.kick.bypass.gameMode.spectator && player.getGameMode().equals(GameMode.SPECTATOR)) return false;
				if (plugin.configuration.kick.bypass.gameMode.creative && player.getGameMode().equals(GameMode.CREATIVE)) return false;
				if (plugin.dependencies.worldGuard.isEnabled() && plugin.dependencies.worldGuard.isOnRegion(player, plugin.configuration.kick.bypass.regions.toArray(new String[0]))) return false;
				if (plugin.configuration.kick.minPlayersToActivate != -1 && plugin.configuration.kick.minPlayersToActivate > 0 && plugin.getServer().getOnlinePlayers().size() < plugin.configuration.kick.minPlayersToActivate) return false;
				if (!player.isKicked())
					return player.getCurrentAfkTime() >= plugin.configuration.kick.time.toMilliseconds();
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
		player.setKicked(true);
		String reason;
		if (plugin.configuration.kick.reason.equalsIgnoreCase("")) reason = "You're kicked, because AFK time";
		else reason = plugin.configuration.kick.reason;
		AFKKickEvent event = new AFKKickEvent(player, reason);
		Bukkit.getScheduler().runTask(plugin, () -> {
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled())
			{
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%player%", player.getName());
				replacements.put("%reason%", reason);
				replacements.put("%time%", Time.ofMillis(player.getCurrentAfkTime()).format());

				String rMessage = getRandom(plugin.configuration.kick.broadcast.messages);
				if (plugin.configuration.kick.broadcast.use && !rMessage.equalsIgnoreCase("")){
					ChatHelper.broadcast(rMessage, replacements);
				}
				plugin.commandManager.run(plugin.configuration.kick.commands, player);
				if (plugin.configuration.kick.useDefault){
					if (Bukkit.getOfflinePlayer(player.getUniqueId()).isOnline()){
						player.setKickCount(player.getKickCount()+1);
						plugin.commandManager.run("lkick %player% %reason%", replacements);
					}
				}
			}
		});
	}

	public KickAction(AFKMagic plugin){
		super(plugin);
	}
}