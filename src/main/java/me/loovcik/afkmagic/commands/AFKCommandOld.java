/*
package org.loovcik.afkmagic.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.loovcik.afkmagic.AFKMagic;
import org.loovcik.afkmagic.managers.PlayersManager;
import org.loovcik.afkmagic.models.AFKPlayer;
import org.loovcik.afkmagic.utils.Time;
import me.loovcik.core.ChatHelper;

import java.util.*;

import static org.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;

public class AFKCommandOld extends AFKCommandBase
{

	public AFKCommandOld()
	{
		super("afk", "Zarządza stanem AFK", new ArrayList<>());
	}

	@Override
	protected void onCommand(CommandSender sender, String[] args)
	{
		assert sender != null;
		assert args != null;
		if (args.length == 0)
		{
			if (!(sender instanceof Player))
			{
				sender.message(AFKMagic.getInstance().configuration.messages.playerOnlyCommand);
				return;
			}
			AFKPlayer player = AFKMagic.getInstance().playersManager.getPlayer((Player) sender);
			player.toggleAFK();
		}
		else
		{
			*/
/* Dostęp do komend administracyjnych *//*

			if (args[0].equalsIgnoreCase("admin"))
			{
				if (args[1].equalsIgnoreCase("bypass"))
				{
					if (hasPlayerPermission(sender, plugin.permissions.bypass, plugin.permissions.admin, plugin.permissions.all))
					{
						if (!PlayersManager.isExists(args[2]))
							sender.message(plugin.configuration.messages.noPlayer);
						else bypass(sender, args[2]);
					}
					else noPermission(sender);
				}
				else if (args[1].equalsIgnoreCase("reload"))
				{
					if (hasPlayerPermission(sender, plugin.permissions.reload, plugin.permissions.admin, plugin.permissions.all))
						reloadConfig(sender);
					else noPermission(sender);
				}
				else if (args[1].equalsIgnoreCase("room"))
				{
					if (args[2].equalsIgnoreCase("set"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.roomSet, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
							setAFKRoom(sender);
						else noPermission(sender);
					}
					else if (args[2].equalsIgnoreCase("remove"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.roomRemove, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
							removeAFKRoom(sender);
						else noPermission(sender);
					}
					else if (args[2].equalsIgnoreCase("teleport"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.roomTeleport, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
							tpToAFKRoom(sender);
						else noPermission(sender);
					}
					else if (args[2].equalsIgnoreCase("list"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.roomList, plugin.permissions.room, plugin.permissions.admin, plugin.permissions.all))
							listAFKRoom(sender);
						else noPermission(sender);
					}
				}
				else if (args[1].equalsIgnoreCase("time"))
				{
					if (args[2].equalsIgnoreCase("afk"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.timeAfk, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
							setTime(sender, TimeMode.fromString(args[2]), args[3], args[4]);
						else noPermission(sender);
					}
					else if (args[2].equalsIgnoreCase("game"))
					{
						if (hasPlayerPermission(sender, plugin.permissions.timeGame, plugin.permissions.time, plugin.permissions.admin, plugin.permissions.all))
							setTime(sender, TimeMode.fromString(args[2]), args[3], args[4]);
						else noPermission(sender);
					}
				}
				else if (args[1].equalsIgnoreCase("status"))
				{
					if (hasPlayerPermission(sender, plugin.permissions.status, plugin.permissions.admin, plugin.permissions.all))
						showPlayerStatus(sender, args[2]);
					else noPermission(sender);
				}
				else
					ChatHelper.message(sender, plugin.configuration.messages.unknownCommand);
			}
			else
			{
				if (args.length == 1)
				{
					if (hasPlayerPermission(sender, plugin.permissions.others, plugin.permissions.all))
						changeAFK(sender, args[0]);
					else noPermission(sender);
				}
			}
		}
	}

	*/
/**
	 * Wykonuje przeładowanie konfiguracji pluginu
	 * @param sender Wywołujący komendę
	 *//*

	private void reloadConfig(CommandSender sender){
		plugin.getLogger().info("Reloading config...");
		plugin.configuration.loadConfig();
		plugin.getLogger().info("Config reloaded.");
		ChatHelper.message(sender, plugin.configuration.messages.pluginReloaded);
	}

	*/
/**
	 * Wykonanie komendy ustawiającej punkt spawnowania
	 * w bezpiecznym miejscu
	 * @param sender Wywołujący komendę
	 *//*

	private void setAFKRoom(CommandSender sender){
		if (sender instanceof Player p)
		{
			Location location = p.getLocation();
			plugin.configuration.room.location.x = Math.floor(location.getX());
			plugin.configuration.room.location.y = Math.floor(location.getY());
			plugin.configuration.room.location.z = Math.floor(location.getZ());
			plugin.configuration.room.location.world = location.getWorld().getName();
			plugin.configuration.getConfig().set("room.location.x", plugin.configuration.room.location.x);
			plugin.configuration.getConfig().set("room.location.y", plugin.configuration.room.location.y);
			plugin.configuration.getConfig().set("room.location.z", plugin.configuration.room.location.z);
			plugin.configuration.getConfig().set("room.location.world", plugin.configuration.room.location.world);
			plugin.saveConfig();
			Map<String, String> replacements = new HashMap<>();
			replacements.put("%location%", "(X="+plugin.configuration.room.location.x+", Y="+ plugin.configuration.room.location.y + ", Z="+ plugin.configuration.room.location.z+", world="+location.getWorld().getName()+")");
			ChatHelper.message(p, plugin.configuration.messages.afkRoomLocationSet, replacements);
		}
		else
			ChatHelper.message(sender, plugin.configuration.messages.playerOnlyCommand);
	}

	*/
/**
	 * Wykonanie komendy ustawiającej czas dla gracza
	 * @param sender Wywołujący komendę
	 * @param timeMode Rodzaj czasu do ustawienia
	 * @param user Gracz, któremu należy ustawić czas
	 * @param time Czas w sekundach lub w formacie skróconym
	 *//*

	private void setTime(CommandSender sender, TimeMode timeMode, String user, String time){
		OfflinePlayer op = PlayersManager.getOfflinePlayer(user);
		if (op == null) {
			ChatHelper.message(sender, plugin.configuration.messages.noPlayer);
			return;
		}

		Time newTime = Time.of(time);
		AFKPlayer player = plugin.playersManager.getPlayer(op);
		if (newTime.toMilliseconds() < 0) newTime = Time.zero();
		Map<String, String> relacements = new HashMap<>();
		relacements.put("%player%", player.getName());
		relacements.put("%time%", newTime.format());
		if (timeMode == TimeMode.TIME_AFK){
			plugin.timeManager.setTotalAFKTime(player, newTime);
			ChatHelper.message(sender, ChatHelper.parse(plugin.configuration.messages.afkTimeChanged, relacements));
		}
		else if (timeMode == TimeMode.TIME_GAME){
			plugin.timeManager.setGameTime(player, newTime);
			ChatHelper.message(sender, ChatHelper.parse(plugin.configuration.messages.gameTimeChanged, relacements));
		}
	}

	*/
/**
	 * Wykonanie komendy usuwającej punkt spawnowania
	 * w bezpiecznym miejscu
	 * @param sender Wywołujący komendę
	 *//*

	private void removeAFKRoom(CommandSender sender){
		plugin.configuration.room.location.x = 0;
		plugin.configuration.room.location.y = 0;
		plugin.configuration.room.location.z = 0;
		plugin.configuration.room.location.world = "world_none";
		plugin.configuration.getConfig().set("room.location.x", plugin.configuration.room.location.x);
		plugin.configuration.getConfig().set("room.location.y", plugin.configuration.room.location.y);
		plugin.configuration.getConfig().set("room.location.z", plugin.configuration.room.location.z);
		plugin.configuration.getConfig().set("room.location.world", plugin.configuration.room.location.world);
		plugin.saveConfig();
		ChatHelper.message(sender, plugin.configuration.messages.afkRoomLocationRemove);
	}

	*/
/**
	 * Wykonanie komendy teleportującej do bezpiecznego miejsca
	 * @param sender Wywołujący komendę
	 *//*

	private void tpToAFKRoom(CommandSender sender){
		if (sender instanceof Player p){
			if(plugin.configuration.room.location.isSet()){
				p.teleport(plugin.configuration.toLocation(plugin.configuration.room.location));
				plugin.afkManager.interact(plugin.playersManager.getPlayer(p), true);
			}
			else {
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%player%", p.getName());
				ChatHelper.message(p, ChatHelper.parse(plugin.configuration.messages.noRoomSpecified, replacements));
			}
		}
		else ChatHelper.message(sender, plugin.configuration.messages.playerOnlyCommand);
	}

	*/
/**
	 * Wykonanie komendy ustawiającej bypassa na sprawdzanie
	 * pod kątem multikont
	 * @param sender Wywołujący komendę
	 * @param user Gracz, który ma być pomijany
	 *//*

	private void bypass(CommandSender sender, String user){
		OfflinePlayer op = PlayersManager.getOfflinePlayer(user);
		if (op == null)
			ChatHelper.message(sender, plugin.configuration.messages.noPlayer);
		else {
			AFKPlayer player = plugin.playersManager.getPlayer(op);
			Map<String, String> replacements = new HashMap<>();
			replacements.put("%player%", op.getName());
			replacements.put("%account%", op.getName());
			if (plugin.altsManager.isExcluded(player)){
				if (plugin.altsManager.isExcludedExactly(player)) {
					plugin.altsManager.unExclude(player);
					ChatHelper.message(player.getPlayer(), ChatHelper.parse(plugin.configuration.messages.playerNotExcluded, replacements));
				}
				else {
					ChatHelper.message(sender, ChatHelper.parse(plugin.configuration.messages.otherAccountExcluded, replacements));
				}
			}
			else {
				plugin.altsManager.exclude(player);
				ChatHelper.message(sender, ChatHelper.parse(plugin.configuration.messages.playerExcluded, replacements));
			}
		}

	}

	*/
/**
	 * Wykonanie komendy wyświetlającej listę osób
	 * przebywających w bezpiecznym miejscu
	 * @param sender Wywołujący komendę
	 *//*

	private void listAFKRoom(CommandSender sender){
		if (plugin.afkManager.count() == 0)
			ChatHelper.message(sender, plugin.configuration.messages.afkRoomEmpty);
		else {
			ChatHelper.message(sender, plugin.configuration.messages.afkRoomList);
			int i = 1;
			for (AFKPlayer p : plugin.playersManager.players.values().stream().filter(AFKPlayer::isAFK).toList())
				ChatHelper.message(sender, i+". "+p.getName());
		}
	}


	*/
/**
	 * Wykonanie komendy zmieniającej stan AFK
	 * innego gracza
	 * @param sender Wywołujący komendę
	 * @param name Gracz, któremu należy zmienić stan AFK
	 *//*

	private void changeAFK(CommandSender sender, String name){
		if (name == null) {
			ChatHelper.message(sender, plugin.configuration.messages.noPlayerSpecified);
			return;
		}

		Player p = Bukkit.getPlayer(name);
		if (p == null)
			ChatHelper.message(sender, plugin.configuration.messages.noPlayer);
		else
		{
			AFKPlayer player = plugin.playersManager.getPlayer(p);
			plugin.afkManager.toggleAFK(player);
		}
	}

	*/
/**
	 * Wyświetlenie informacji o braku uprawnień
	 * @param sender Wywołujący komendę
	 *//*

	private void noPermission(CommandSender sender){
		ChatHelper.message(sender, plugin.configuration.messages.noPermission);
	}

	*/
/**
	 * Rodzaj czasu
	 *//*

	public enum TimeMode {
		TIME_AFK, TIME_GAME, TIME_UNKNOWN;

		public static TimeMode fromString(String value){
			if (value.equalsIgnoreCase("afk")) return TIME_AFK;
			if (value.equalsIgnoreCase("game")) return TIME_GAME;
			return TIME_UNKNOWN;
		}
	}
}*/