package me.loovcik.afkmagic.managers.actions;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.events.AFKRoomEnterEvent;
import me.loovcik.afkmagic.events.AFKRoomExitEvent;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.types.Time;
import me.loovcik.core.ChatHelper;

import java.util.HashMap;
import java.util.Map;

import static me.loovcik.afkmagic.managers.ConfigurationManager.getRandom;
import static me.loovcik.afkmagic.utils.Extensions.*;

public class RoomAction extends Action
{
	private boolean consoleWarnedNoLocation = false;

	/**
	 * {@inheritDoc}
	 * @param player Kontekst gracza
	 * @return True, jeśli należy ostrzec gracza, w przeciwnym razie false
	 */
	@Override
	public boolean check(AFKPlayer player){
		if (plugin.configuration.room.use && !player.getPlayer().isOp() && !player.isVanished()) {
			if (player.isAFK()) {
				if(hasPlayerPermission(player.getPlayer(), plugin.configuration.room.bypass.permissions)) return false;
				if (isOnWorld(player, plugin.configuration.room.bypass.worlds)) return false;
				if (plugin.configuration.room.bypass.gameMode.spectator && player.getGameMode().equals(GameMode.SPECTATOR)) return false;
				if (plugin.configuration.room.bypass.gameMode.creative && player.getGameMode().equals(GameMode.CREATIVE)) return false;
				if (plugin.dependencies.worldGuard.isEnabled() && plugin.dependencies.worldGuard.isOnRegion(player, plugin.configuration.room.bypass.regions.toArray(new String[0]))) return false;
				if (player.getCurrentAfkTime() >= plugin.configuration.room.time.toMilliseconds()) {
					if (!player.isInsideAfkRoom()){
						if (!plugin.configuration.room.location.isSet()){
							if (!consoleWarnedNoLocation)
							{
								ChatHelper.console(plugin.configuration.messages.room.noRoom);
								consoleWarnedNoLocation = true;
							}
							return false;
						}
						return true;
					}
					return false;
				}
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
		AFKRoomEnterEvent event = new AFKRoomEnterEvent(player);
		player.setTeleporting(true);
		Bukkit.getScheduler().runTask(plugin, () -> {
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled())
			{
				player.setLastLocation(player.getPlayer().getLocation());
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%player%", player.getName());
				replacements.put("%location%", locationToString(player.getLastLocation()));
				replacements.put("%time%", Time.ofMillis(player.getCurrentAfkTime()).format());

				player.setStartAfkRoomDate(System.currentTimeMillis());

				String rMessage = getRandom(plugin.configuration.room.broadcast.messages);
				if (plugin.configuration.room.broadcast.use && !rMessage.equalsIgnoreCase("")){
					ChatHelper.broadcast(rMessage, replacements);
				}

				rMessage = getRandom(plugin.configuration.room.messages);
				if (!rMessage.equalsIgnoreCase("")) {
					ChatHelper.message(player.getPlayer(), rMessage, replacements);
				}
				player.setInsideAfkRoom(true);
				player.getPlayer().teleport(plugin.configuration.toLocation(plugin.configuration.room.location));
				plugin.commandManager.run(plugin.configuration.room.commands, player);

			}
			player.setTeleporting(false);
		});
	}

	public static void exit(AFKPlayer player){
		if (forceExit(player)){
			AFKRoomExitEvent event = new AFKRoomExitEvent(player);
			Bukkit.getScheduler().runTask(AFKMagic.getInstance(), () -> {
				Bukkit.getPluginManager().callEvent(event);
			});
		}
	}

	public static boolean forceExit(AFKPlayer player){
		AFKMagic plugin = AFKMagic.getInstance();
		if (plugin.configuration.room.use && player.isInsideAfkRoom() && !player.isTeleporting())
		{
			player.setTeleporting(true);
			Location location = player.getLastLocation();
			if (location == null)
				location = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
			player.getPlayer().teleport(location);
			player.setInsideAfkRoom(false);
			player.setLastLocation(null);
			player.setTeleporting(false);
			return true;
		}
		return false;
	}
	public RoomAction(AFKMagic plugin){
		super(plugin);
	}
}