package me.loovcik.afkmagic.managers;

import me.loovcik.core.types.Time;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.managers.sections.Commands;
import me.loovcik.afkmagic.managers.sections.Messages;

import java.util.*;

/**
 * Menedżer zarządzający konfiguracją
 */
public class ConfigurationManager extends me.loovcik.core.managers.ConfigurationManager
{
	private final AFKMagic plugin;
	private FileConfiguration config;

	/**
	 * Konfiguracja czatu
	 */
	public cGlobal global = new cGlobal();

	/**
	 * Konfiguracja komunikatów
	 */
	public Messages messages = new Messages();

	/**
	 * Konfiguracja komend pluginu
	 */
	public Commands commands = new Commands();

	/**
	 * Konfiguracja dotycząca AFK
	 */
	public cAFK afk = new cAFK();

	/**
	 * Konfiguracja dotycząca wyświetlania ostrzeżenia
	 * dla graczy będących AFK przez określony czas
	 */
	public cWarn warn = new cWarn();

	/**
	 * Konfiguracja dotycząca wyrzucania graczy z serwera
	 * po określonym czasie AFK
	 */
	public cKick kick = new cKick();

	/**
	 * Konfiguracja dotycząca przenoszenia graczy AFK
	 * do wyznaczonego miejsca
	 */
	public cAFKRoom room = new cAFKRoom();

	/**
	 * Konfiguracja dotycząca wykrywania graczy
	 * używających multikont do AFK
	 */
	public cAlts alts = new cAlts();

	/**
	 * Konfiguracja dotycząca wyrzucania graczy będących
	 * najdłużej AFK, jeśli ilość osób na serwerze osiągnie
	 * wskazaną wartość.
	 */
	public cLimits limits = new cLimits();

	/**
	 * Konfiguracja synchronizacji stanu AFK z pluginem Essentials
	 */
	public cEssentials essentials = new cEssentials();

	/**
	 * Wczytanie konfiguracji z pliku yml
	 */
	public void loadConfig()
	{
		plugin.reloadConfig();
		this.config = plugin.getConfig();
		config.options().copyDefaults(true);
		load();
		plugin.saveConfig();
	}

	/**
	 * Konwertuje zapis lokalizacji z pliku konfiguracyjnego
	 * do formatu Minecraft
	 * @param loc Lokalizacja z pliku yaml
	 * @return Lokalizacja w formacie Minecraft
	 */
	public Location toLocation(vLocation loc){
		return new Location(Bukkit.getWorld(loc.world), loc.x, loc.y, loc.z);
	}

	/**
	 * Pobiera i przypisuje wartości z pliku konfiguracyjnego
	 */
	public void load()
	{
		global.prefix = getString("global.prefix", "&8[&lAFK&8]&r", "Prefix used in chat by the plugin");
		global.debug = getBoolean("global.debug", false, "Show additional debug information on console");
		global.logToConsole = getBoolean("global.logToConsole", true, "Log afk change status to console");

		messages.misc.unknownCommand = getString("messages.misc.unknownCommand", "&cUnknown command");
		messages.misc.noPermission = getString("messages.misc.noPermission", "&cYou don't have permission to do this");
		messages.misc.noPlayer = getString("messages.misc.noPlayer", "&cPlayer not found");
		messages.misc.noPlayerSpecified = getString("messages.misc.noPlayerSpecified", "&cYou need to specify the player");
		messages.misc.playerOnlyCommand = getString("messages.misc.playerOnlyCommand", "&cThis command is player only");
		messages.misc.reloading = getString("messages.misc.reloading", "&7Reloading plugin configuration...");
		messages.misc.reloaded = getString("messages.misc.reloaded", "&aPlugin configuration reloaded");

		messages.status.noInformation = getString("messages.status.noInformation", "&cNo player information");
		messages.status.players.clear();
		messages.status.players.addAll(getList("messages.status.players", List.of("&e%player%'s information", "&7- Is AFK: &6%status%", "&7- Total AFK time: &6%totalTime%")));
		messages.status.afk.clear();
		messages.status.afk.addAll(getList("messages.status.afk", List.of("&7- AFK start date: &6%startTime%", "&7- Current AFK time: &6%currentTime%")));
		messages.status.advanced.clear();
		messages.status.advanced.addAll(getList("messages.status.advanced", List.of("&7- Total kicks: &6%kick%", "&7- Total alts kicks: &6%altsKicks%", "&7- Total alts detections: &6%altsDetections%", "&7- Is on AFK Room?: &6%room%")));
		messages.status.activity.clear();
		messages.status.activity.addAll(getList("messages.status.activity", List.of("&7- Is active: &6%active%", "&7- Last activity: &6%activity%")));
		messages.status.admins.clear();
		messages.status.admins.addAll(getList("messages.status.admins", List.of("&7- Last alts detection: &6%altsDetectionDate%")));

		messages.list.empty = getString("messages.list.empty", "&7There are no players AFK");
		messages.list.header = getString("messages.list.header", "&eList of AFK players &7(%count%)&e:");
		messages.list.element = getString("messages.list.element", "&a%player% &7(%since%)");

		messages.bypass.afk.enabled = getString("messages.bypass.afk.enabled", "&7Player &6%player% &7is no longer switch to AFK");
		messages.bypass.afk.disabled = getString("messages.bypass.afk.disabled", "&7Player &6%player% &7is now switching to AFK");
		messages.bypass.alts.enabled = getString("messages.bypass.alts.enabled", "&7Player &6%player% &7alts checking is now disabled");
		messages.bypass.alts.disabled = getString("messages.bypass.alts.disabled", "&7Player &6%player% &7alts checking is now enabled");
		messages.bypass.kick.enabled = getString("messages.bypass.kick.enabled", "&7Player &6%player% &7is no longer kicking");
		messages.bypass.kick.disabled = getString("messages.bypass.kick.disabled", "&7Player &6%player% &7is now kicking");
		messages.bypass.warn.enabled = getString("messages.bypass.warn.enabled", "&7Player &6%player% &7is no longer warnings");
		messages.bypass.warn.disabled = getString("messages.bypass.warn.disabled", "&7Player &6%player% &7is now warning");
		messages.bypass.room.enabled = getString("messages.bypass.room.enabled", "&7Player &6%player% &7is no longer teleport to AFK room");
		messages.bypass.room.disabled = getString("messages.bypass.room.disabled", "&7Player &6%player% &7teleport to AFK room enabled");
		messages.bypass.autoClicker.enabled = getString("messages.bypass.autoClicker.enabled", "&7Player &6%player% &7is no longer checking for autoClicker");
		messages.bypass.autoClicker.disabled = getString("messages.bypass.autoClicker.disabled", "&7Player &6%player% &7is now checking for autoClicker");
		messages.bypass.kickAutoClicker.enabled = getString("messages.bypass.kickAutoClicker.enabled", "&7Player &6%player% &7is no longer kicking");
		messages.bypass.kickAutoClicker.disabled = getString("messages.bypass.kickAutoClicker.disabled", "&7Player &6%player% &7is now kicking enabled");

		messages.room.empty = getString("messages.room.empty", "&cThere is no player in AFK Room");
		messages.room.list = getString("messages.room.list", "&eList of all players in AFK Room");
		messages.room.noRoom = getString("messages.room.noRoom", "&cPlayer cannot be teleported to AFK Room. No location set in config");
		messages.room.set = getString("messages.room.set", "&eList of all players in AFK Room");

		messages.time.afk = getString("messages.time.afk", "&Total AFK time for player &6%player%&7 is now set to &6%time%");
		messages.time.game = getString("messages.time.game", "&7Total game time for player &6%player%&7 is now set to &6%time%");

		messages.yesWord = getString("messages.yesWord", "Yes");
		messages.noWord = getString("messages.noWord", "No");

		commands.afk.label = getString("commands.afk.label", "afk");
		commands.afk.aliases.addAll(getList("commands.afk.aliases", List.of("afkMagic")));
		commands.afk.permissions.command = getString("commands.afk.permissions.command", "afkmagic.command.afk");
		commands.afk.permissions.other = getString("commands.afk.permissions.other", "afkmagic.command.afk.other");

		commands.list.label = getString("commands.list.label", "list");
		commands.list.aliases.addAll(getList("commands.list.aliases", List.of()));
		commands.list.permission = getString("commands.list.permission", "afkmagic.command.list");

		commands.status.label = getString("commands.status.label", "status");
		commands.status.aliases.addAll(getList("commands.status.aliases", List.of()));
		commands.status.permissions.command = getString("commands.status.permissions.command", "afkmagic.command.status");
		commands.status.permissions.other = getString("commands.status.permissions.other", "afkmagic.command.status.other");
		commands.status.permissions.advanced = getString("commands.status.permissions.advanced", "afkmagic.status.advanced");
		commands.status.permissions.activity = getString("commands.status.permissions.activity", "afkmagic.status.activity");
		commands.status.permissions.admin = getString("commands.status.permissions.admin", "afkmagic.status.admin");

		commands.bypass.label = getString("commands.bypass.label", "bypass");
		commands.bypass.aliases.addAll(getList("commands.bypass.aliases", List.of()));
		commands.bypass.permission = getString("commands.bypass.permission", "afkmagic.command.bypass");
		commands.bypass.afk.label = getString("commands.bypass.types.afk.label", "afk");
		commands.bypass.afk.permission = getString("commands.bypass.types.afk.permission", "afkmagic.command.bypass.afk");
		commands.bypass.alts.label = getString("commands.bypass.types.alts.label", "alts");
		commands.bypass.alts.permission = getString("commands.bypass.types.alts.permission", "afkmagic.command.bypass.alts");
		commands.bypass.kick.label = getString("commands.bypass.types.kick.label", "kick");
		commands.bypass.kick.permission = getString("commands.bypass.types.kick.permission", "afkmagic.command.bypass.kick");
		commands.bypass.room.label = getString("commands.bypass.types.room.label", "room");
		commands.bypass.room.permission = getString("commands.bypass.types.room.permission", "afkmagic.command.bypass.room");
		commands.bypass.autoClicker.label = getString("commands.bypass.types.autoClicker.label", "autoClicker");
		commands.bypass.autoClicker.permission = getString("commands.bypass.types.autoClicker.permission", "afkmagic.command.bypass.autoclicker");
		commands.bypass.kickAutoClicker.label = getString("commands.bypass.types.kickAutoClicker.label", "kickAutoClicker");
		commands.bypass.kickAutoClicker.permission = getString("commands.bypass.types.kickAutoClicker.permission", "afkmagic.command.bypass.autoclicker.kick");

		commands.reload.label = getString("commands.reload.label", "reload");
		commands.reload.aliases.addAll(getList("commands.reload.aliases", List.of()));
		commands.reload.permission = getString("commands.reload.permission", "afkmagic.command.reload");


		afk.interval = getTime("afk.interval", "1s", "Specifies how often the plugin should check players");
		afk.ignoreForSleep = getBoolean("afk.ignoreForSleep", true, "Specifies whether players who are AFK are ignored\nwhen calculating the required number of players who need to\nskip the night.");

		afk.start.use = getBoolean("afk.start.use", true, "Specifies whether AFK function is used");
		afk.start.time = getTime("afk.start.time", "5m", "Specifies the amount of time (in seconds) the player remains idle after\nit will go into AFK state");
		afk.start.commands.clear();
		afk.start.commands.addAll(getList("afk.start.commands", List.of(), """
				List of commands that will be executed when
				the player enters the AFK state.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				"""));
		afk.start.message = getString("afk.start.message", "&7You're now AFK", """
				The message that will be displayed to the player,
				which goes into AFK state.
				Leave blank if you don't want the message displayed
				""");
		afk.start.sound = getString("afk.start.sound", "BLOCK_ANVIL_HIT", "Specifies the sound that will be played to the passing player\nin AFK state.");
		afk.start.broadcast.use = getBoolean("afk.start.broadcast.use", true, "Specifies whether information about the player entering the\nAFK state should be announced to other players");
		afk.start.broadcast.messages.clear();
		afk.start.broadcast.messages.addAll(getList("afk.start.broadcast.messages", List.of("&3%player%&7 is now AFK"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				"""));
		afk.start.broadcast.bypass.permissions.clear();
		afk.start.broadcast.bypass.permissions.addAll(getList("afk.start.broadcast.bypass", List.of(), """
				Players with the following permissions will not be announced to other players
				Similarly, if the player has these permissions, it will also not be displayed
				information about exiting the AFK state"""));
		afk.start.detectors.afkMachine.use = getBoolean("afk.start.detectors.afkMachines.use", true, "Settings for detecting mechanisms that prevent entering the AFK state.\nStanding on water bubbles, riding in a minecart, autoclicker are detected");

		addComment("afk.start", "Settings for enabling AFK status");
		addComment("afk.start.broadcast", "AFK announcement other players settings");
		addComment("afk.start.detectors", "Settings of detectors that detecting attempts to bypass the entering\nin AFK state");

		afk.end.commands.clear();
		afk.end.commands.addAll(getList("afk.end.commands", List.of(), """
				List of commands that will be executed when
				player exits AFK state.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				%time% - time (in seconds) for which the player was AFK
				"""));
		afk.end.message = getString("afk.end.message", "&7You aren't AFK now", """
				The message displayed to the player who exits
				AFK status.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				%time% - time during the player was AFK
				""");
		afk.end.broadcast.use = getBoolean("afk.end.broadcast.use", true, "Specifies whether information about the player exiting from\nAFK state should be announced to other players");
		afk.end.broadcast.messages.clear();
		afk.end.broadcast.messages.addAll(getList("afk.end.broadcast.messages", List.of("&3%player%&7 isn't AFK now"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				%time% - time during the player was AFK
				"""));
		afk.end.detectors.move.use = getBoolean("afk.end.detectors.move.use", true, "Determines whether player movement causes exit from AFK state");
		afk.end.detectors.move.distance = getDouble("afk.end.detectors.move.distance", 1.5d, "Specifies the distance the player must move to exit from\nAFK state");
		afk.end.detectors.rotate.use = getBoolean("afk.end.detectors.rotate.use", true, "Determines whether turning around the player will cause\nexit from AFK state");
		afk.end.detectors.rotate.angle = getDouble("afk.end.detectors.rotate.angle", 1.4d, "Specifies the range of rotation that the player must make\nto exit from AFK state");
		afk.end.detectors.interact.use = getBoolean("afk.end.detectors.interact.use", true, "Determines whether player interact with environment\ncauses exit from AFK state");
		afk.end.detectors.interact.autoClicker.use = getBoolean("afk.end.detectors.interact.autoclicker.use", true, "Determines whether autoClicker detection is enable");
		afk.end.detectors.interact.autoClicker.avgDeviation = getInt("afk.end.detectors.interact.autoclicker.deviation", 25, "Deviation in milliseconds between successive clicks");
		afk.end.detectors.interact.autoClicker.checkTime = getInt("afk.end.detectors.interact.autoclicker.checkTime", 3000, "Time in milliseconds over which the regularity of clicks is to be checked");
		afk.end.detectors.interact.autoClicker.alert = getBoolean("afk.end.detectors.interact.autoclicker.alert.use", true, "If enabled, the player will receive a notification when an autoClicker is detected");
		afk.end.detectors.interact.autoClicker.alertMessage = getString("afk.end.detectors.interact.autoclicker.alert.message", "&cNie używaj autoClickera!", "A message shown to the player in the chat\ninforming about suspiciously regular clicks");
		afk.end.detectors.interact.autoClicker.kick.use = getBoolean("afk.end.detectors.interact.autoclicker.kick.use", false, "Specifies whether the function should be enabled");
		afk.end.detectors.interact.autoClicker.kick.delay = getTime("afk.end.detectors.interact.autoclicker.kick.delay", "2m", """
				Player Kick Delay.
				Kicks are scheduled when autoClicker usage is confirmed
				(consistently clicks detected more than 100 times).
				Once the time below has passed since the schedule,
				the player will be kicked if they have not disabled
				autoClicker in the meantime.
				""");
		afk.end.detectors.interact.autoClicker.kick.bypass.permissions.clear();
		afk.end.detectors.interact.autoClicker.kick.bypass.permissions.addAll(getList("afk.end.detectors.interact.autoclicker.kick.bypass.permissions", List.of("afkmagic.bypass.autoclicker.kick"), "Players with the following permissions will not be kicked for using autoClicker"));
		afk.end.detectors.interact.autoClicker.kick.reason = getString("afk.end.detectors.interact.autoclicker.kick.reason", "&cUżywanie Auto-Clickera", "Reason for kick in native player kick message");
		afk.end.detectors.interact.autoClicker.bypass.permissions.clear();
		afk.end.detectors.interact.autoClicker.bypass.permissions.addAll(getList("afk.end.detectors.interact.autoclicker.bypass.permissions", List.of("afkmagic.bypass.autoclicker"), "Players with the following permissions will not be detected by autoClicker detector"));

		addComment("afk.end.detectors.interact.autoclicker.kick.bypass", "Options to exclude players from being kicked\nfrom the server for using autoClicker");
		addComment("afk.end.detectors.interact.autoclicker.bypass", "Options to exclude players from autoClicker detection");

		afk.end.detectors.interact.autoClicker.cancelEvent = getBoolean("afk.end.detectors.interact.autoclicker.cancel", true, """
				If enabled, the current action will be canceled if autoClicker
				usage is detected. Actions that can be canceled include
				hitting mobs, mining blocks, etc.
				""");
		afk.end.detectors.interact.autoClicker.log.use = getBoolean("afk.end.detectors.interact.autoclicker.log.use", true, "Defines whether autoClicker detection should be logged in the console");
		afk.end.detectors.interact.autoClicker.log.message = getString("afk.end.detectors.interact.autoclicker.log.message", "AutoClicker detected [%count%] | %player% | Reason: %reason% | Avg: %average% | Dev: %deviation%", """
				The message shown in the console when an autoClicker is detected
				Available placeholders:
				%player% - player name
				%deviation% - Deviation of the value from the regularity of clicks
				%intervals% - List of click interval values
				%location% - AutoClicker detection location
				%count% - Number of positive autoClicker detections
				""");
		afk.end.detectors.interact.autoClicker.log.onlyOnce = getBoolean("afk.end.detectors.interact.autoclicker.log.onlyOnce", true, "If true, the autoClicker detection notification\nwill be sent only once per detection cycle");

		addComment("afk.end.broadcast", "Settings to announce a player's exit\nfrom AFK status to all players");
		addComment("afk.end.detectors", "Settings of detectors that detect the moment of exit\nfrom AFK state");
		addComment("afk.end.detectors.move", "Player move detection settings");
		addComment("afk.end.detectors.rotate", "Player rotation detection settings");
		addComment("afk.end.detectors.interact", "Determines whether interaction with the environment\ncauses exit from AFK state");
		addComment("afk.end.detectors.interact.autoclicker", "AutoClicker detection settings");
		addComment("afk.end.detectors.interact.alert", "Options for notifying players when autoClicker is detected");
		addComment("afk.end.detectors.interact.kick", "Options for kicking players who use autoClicker");
		addComment("afk.end.detectors.interact.log", "Detection log options to console");

		afk.end.detectors.chat.use = getBoolean("afk.end.detectors.chat.use", true, "Determines whether typing in chat causes exit from AFK state");
		afk.end.detectors.chat.blacklistCommands.clear();
		afk.end.detectors.chat.blacklistCommands.addAll(getList("afk.end.detectors.chat.blacklist-commands", List.of("helpop", "r"), "List of commands which, when used, will not exit the AFK state"));
		addComment("afk.end.detectors.chat", "Chat writing detection settings");

		afk.end.detectors.consume = getBoolean("afk.end.detectors.consume", true, "Determines whether eating something causes exit from AFK state");
		afk.end.detectors.block.place = getBoolean("afk.end.detectors.block.place", true, "Determines whether block place causes from AFK state");
		afk.end.detectors.block.destroy = getBoolean("afk.end.detectors.block.destroy", true, "Determines whether block destroy causes from AFK state");
		addComment("afk.end.detectors.block", "Block interaction detection settings");

		afk.end.detectors.item.drop = getBoolean("afk.end.detectors.item.drop", true, "Determines whether drop an item causes from AFK state");
		afk.end.detectors.item.pickup = getBoolean("afk.end.detectors.item.pickup", true, "Determines whether pickup an item causes from AFK state");
		afk.end.detectors.item.destroy = getBoolean("afk.end.detectors.item.destroy", true, "Determines whether break an item causes from AFK state");
		addComment("afk.end.detectors.item", "Items interaction detection settings");

		afk.end.detectors.bucket.fill = getBoolean("afk.end.detectors.bucket.fill", true, "Determines whether filling the bucket causes exit AFK state");
		afk.end.detectors.bucket.empty = getBoolean("afk.end.detectors.bucket.empty", true, "Determines whether emptying the bucket causes exit");
		addComment("afk.end.detectors.bucket", "Bucket interaction detection settings");

		afk.end.detectors.changeWorld = getBoolean("afk.end.detectors.changeWorld", true, "Determines whether the player changes the world causes exit from AFK state");
		afk.end.detectors.shear = getBoolean("afk.end.detectors.shear", true, "Determines whether shear causes exit from AFK state");
		afk.end.detectors.sneak = getBoolean("afk.end.detectors.sneak", true, "Determines whether using sneak causes exit from AFK state");
		afk.end.detectors.sprint = getBoolean("afk.end.detectors.sprint", true, "Determines whether using sprint causes exit from AFK state");
		afk.end.detectors.book = getBoolean("afk.end.detectors.book", true, "Determines whether interaction with the book causes exit from AFK state");
		afk.end.detectors.teleport = getBoolean("afk.end.detectors.teleport", true, "Determines whether teleportation causes exit from AFK state");

		afk.protect.move = getBoolean("afk.protect.move", false, """
				Specifies whether to prevent moving an AFK player
				ATTENTION! In some situations, it can cause a player trying
				to move an AFK player to bug and prevent them from moving
				""");
		afk.protect.hurt.players = getBoolean("afk.protect.hurt.players", false, "Determines whether to prevent other players from dealing damage");
		afk.protect.hurt.others = getBoolean("afk.protect.hurt.others", false, "Determines whether to prevent taking damage from mobs");
		afk.protect.mob.spawn.use = getBoolean("afk.protect.mob.spawn.use", false, "Specifies whether to disable mob spawning near an AFK player");
		afk.protect.mob.spawn.delay = getTime("afk.protect.mob.spawn.delay", -1, """
				Specifies the time in seconds before mob spawning is disabled around
				the AFK player.
				Set to -1 if spawning should be disabled immediately.
				The delay only applies to spawning mobs from spawners.
				Naturally spawned mobs will be disabled immediately
				if there is no other non-AFK player in range.
				""");
		afk.protect.mob.target = getBoolean("afk.protect.mob.target", false, "Determines whether to prevent mobs from targeting the AFK player");
		afk.protect.pickup = getBoolean("afk.protect.pickup", false, "Determines whether an AFK player can pick up items thrown to him");
		addComment("afk.protect", "AFK player protection configuration");
		addComment("afk.protect.hurt", "Damage protection settings");
		addComment("afk.protect.mob", "Mob protection settings");
		addComment("afk.protect.mob.spawn", "Specifies the rules for spawning mobs near the player\nif there are no other players around him");

		afk.bypass.permissions.clear();
		afk.bypass.permissions.addAll(getList("afk.bypass.permissions", List.of("afkmagic.bypass.afk"), "Players with the following permissions will not enter AFK"));
		afk.bypass.regions.clear();
		afk.bypass.regions.addAll(getList("afk.bypass.regions", List.of(), "Players located in the following regions (if WorldGuard available)\nwill not enter AFK"));
		afk.bypass.gameMode.creative = getBoolean("afk.bypass.gamemode.creative", true, "Players in spectactor mode will not enter AFK");
		afk.bypass.gameMode.spectator = getBoolean("afk.bypass.gamemode.spectator", true, "Players in creative mode will not enter AFK");
		afk.bypass.worlds.clear();
		afk.bypass.worlds.addAll(getList("afk.bypass.worlds", List.of(), "Players in the following worlds will not enter AFK"));
		addComment("afk.bypass", "Settings for bypassing AFK");
		addComment("afk.bypass.gamemode", "A player with a following game mode will not enter AFK");

		warn.use = getBoolean("warn.use", false, "Specifies whether the warning feature is to be used");
		warn.time = getTime("warn.time", "4m", "Specifies the time after AFK a warning is displayed");
		warn.messages.clear();
		warn.messages.addAll(getList("warn.messages", List.of("&7You will be kicked if you continue to be AFK"), """
				A list of messages that will be randomly displayed to the player.
				All placeholders from PlaceholderAPI (if available)
				and the following internal ones are supported:
				%player% - player name
				
				Set this field to [] if you do not want the message to be displayed.
				Useful if you just want to execute a specific command.
				"""));
		warn.commands.clear();
		warn.commands.addAll(getList("warn.commands", List.of(), """
				List of commands that will be executed when the player is warned.
				Any placeholders from PlaceholderAPI (if available)
				and the following internal ones are supported:
				%player% - player name
				If you do not want to use any commands, just leave []
				"""));
		warn.bypass.permissions.clear();
		warn.bypass.permissions.addAll(getList("warn.bypass.permissions", List.of("afkmagic.bypass.warn"), "Players with the following permissions will not be warned"));
		warn.bypass.regions.clear();
		warn.bypass.regions.addAll(getList("warn.bypass.regions", List.of(), "Players located in the following regions (if WorldGuard available)\nwill not be warned"));
		warn.bypass.gameMode.creative = getBoolean("warn.bypass.gamemode.creative", true, "Players in spectactor mode will not be warned");
		warn.bypass.gameMode.spectator = getBoolean("warn.bypass.gamemode.spectator", true, "Players in creative mode will not be warned");
		warn.bypass.worlds.clear();
		warn.bypass.worlds.addAll(getList("warn.bypass.worlds", List.of(), "Players in the following worlds will not be warned"));
		addComment("warn.bypass", "Configuration to bypass player warnings");
		addComment("warn.bypass.gamemode", "Players with a following game mode will not be warned");

		kick.use = getBoolean("kick.use", false, "Specifies whether the kick function is to be used");
		kick.useDefault = getBoolean("kick.useDefault", true, "Specifies whether the plugin should use an internal\nmechanism to kick a player");
		kick.time = getTime("kick.time", "5m", "Specifies the AFK time (in seconds) after which\nAFK players will be kicked");
		kick.minPlayersToActivate = getInt("kick.minPlayersToActivate", 68, "Specifies minimal players on server, to activate kick\nfunction. Use -1, to set always active");
		kick.reason = getString("kick.reason", "&7You've been AFK too long", """
				Specifies the reason for kicking a player from the server
				Any placeholders from PlaceholderAPI (if available)
				and the following internal ones are supported:
				%player% - player name
				%time% - time during the player was AFK
				""");
		kick.broadcast.use = getBoolean("kick.broadcast.use", false, "Specifies whether information about the player is kicked from\nserver should be announced to other players");
		kick.broadcast.messages.clear();
		kick.broadcast.messages.addAll(getList("kick.broadcast.messages", List.of("&7Player %player% was kicked"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				%time% - time during the player was AFK
				"""));
		kick.commands.clear();
		kick.commands.addAll(getList("kick.commands", List.of(), """
				List of commands that will be executed when the player is kicked.
				Any placeholders from PlaceholderAPI (if available) and the
				following internal ones are supported:
				%player% - player name
				%reason% - reason for kicking
				%time% - AFK time
				If you don't want to use any commands, just leave []
				"""));
		kick.bypass.permissions.clear();
		kick.bypass.permissions.addAll(getList("kick.bypass.permissions", List.of("afkmagic.bypass.kick"), "Players with the following permissions will not be kicked"));
		kick.bypass.regions.clear();
		kick.bypass.regions.addAll(getList("kick.bypass.regions", List.of(), "Players located in the following regions (if WorldGuard available)\nwill not be kicked"));
		kick.bypass.gameMode.creative = getBoolean("kick.bypass.gamemode.creative", true, "Players in spectactor mode will not be kicked");
		kick.bypass.gameMode.spectator = getBoolean("kick.bypass.gamemode.spectator", true, "Players in creative mode will not be kicked");
		kick.bypass.worlds.clear();
		kick.bypass.worlds.addAll(getList("kick.bypass.worlds", List.of(), "Players in the following worlds will not be kicked"));
		addComment("kick.broadcast", "Settings for announce all players when a player is\nkicked from the server");
		addComment("kick.bypass", "Configuration to bypass player kicks");
		addComment("kick.bypass.gamemode", "Players with a following game mode will not be kicked");

		room.use = getBoolean("room.use", false, "Determines whether the AFK players to a designated safe\nlocation feature is used");
		room.time = getTime("room.time", "5m", "Specifies the AFK time (in seconds) after witch the player\nshould be teleported to safe location");
		room.commands.clear();
		room.commands.addAll(getList("room.commands", List.of(), """
				List of commands that will be executed when the player is teleporting
				to safe location.
				Any placeholders from PlaceholderAPI (if available) and the
				following internal ones are supported:
				%player% - player name
				If you don't want to use any commands, type []
				"""));
		room.messages.clear();
		room.messages.addAll(getList("room.messages", List.of("&7You've been moved to the AFK room"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				"""));
		room.broadcast.use = getBoolean("room.broadcast.use", false, "Specifies whether information about the player is teleported\nshould be announced to other players");
		room.broadcast.messages.clear();
		room.broadcast.messages.addAll(getList("room.broadcast.messages", List.of("Player %player% has been moved to the AFK room"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				"""));
		room.bypass.permissions.clear();
		room.bypass.permissions.addAll(getList("room.bypass.permissions", List.of("afkmagic.bypass.room"), "Players with the following permissions will not be teleported"));
		room.bypass.regions.clear();
		room.bypass.regions.addAll(getList("room.bypass.regions", List.of(), "Players located in the following regions (if WorldGuard available)\nwill not be teleported"));
		room.bypass.gameMode.creative = getBoolean("room.bypass.gamemode.creative", true, "Players in spectactor mode will not be teleported");
		room.bypass.gameMode.spectator = getBoolean("room.bypass.gamemode.spectator", true, "Players in creative mode will not be teleported");
		room.bypass.worlds.clear();
		room.bypass.worlds.addAll(getList("room.bypass.worlds", List.of(), "Players in the following worlds will not be teleported"));
		room.location.x = getDouble("room.location.x", 0);
		room.location.y = getDouble("room.location.y", 400);
		room.location.z = getDouble("room.location.z", 0);
		room.location.world = getString("room.location.world", "world_none");
		addComment("room.broadcast", "Settings for informing all players when a given player\nhas been moved to a safe location");
		addComment("room.bypass", "Settings for players who will not be teleported");
		addComment("room.gamemode", "Players with a following game mode will not be teleported");
		addComment("room.location", "Location of a safe place");

		alts.use = getBoolean("alts.use", false, "Specifies whether to use multi-account detection");
		alts.warning.time = getTime("alts.warning.time", "5s", "Specifies the time (in seconds) after detection, after which a warning\nabout restrictions on using multiple accounts for AFK will be sent");
		alts.warning.self.use = getBoolean("alts.warning.self.use", true, "Specifies whether to warn the player about multi-account restrictions");
		alts.warning.self.messages.clear();
		alts.warning.self.messages.addAll(getList("alts.warning.self.messages", List.of("Don't use alts accounts to AFK!"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - player name
				%alts% - other account associated with this player
				"""));
		alts.warning.alts.use = getBoolean("alts.warning.alts.use", true, "Specifies whether to warn other player associated with the player\nabout multi-account restrictions");
		alts.warning.alts.messages.clear();
		alts.warning.alts.messages.addAll(getList("alts.warning.alts.messages", List.of("&cUsing multiple accounts for AFK is not allowed!"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - main account name
				%alts% - other account associated with this player
				"""));
		alts.warning.admins.use = getBoolean("alts.warning.admins.use", true, "Specifies whether to warn server administrators\nabout player's use multi-account to AFK");
		alts.warning.admins.messages.clear();
		alts.warning.admins.messages.addAll(getList("alts.warning.admins.messages", List.of("%player% uses multiple account (%alt%) to AFK!"), """
				A list of messages that will be sent to everyone.
				Messages from the list are randomly selected.
				Any placeholders from PlaceholderAPI are supported
				(if available) and the following internals:
				%player% - main account name
				%alts% - other account associated with this player
				"""));
		alts.warning.admins.permissions.clear();
		alts.warning.admins.permissions.addAll(getList("alts.warning.admins.permissions", List.of(), "Specifies the permissions that players must have to\nreceive information about players using multi-accounts"));
		addComment("alts.warning", "Settings for warning players about restrictions on using multi-account to AFK");
		addComment("alts.warning.self", "Player self warning settings");
		addComment("alts.warning.alts", "Warning settings for other accounts associated with the player");
		addComment("alts.warning.admins", "Settings for displaying information about the player's use\nof multi-accounts for server administration");

		alts.kick.use = getBoolean("alts.kick.use", true, "Specifies whether the multi-accounts kick function is to be used");
		alts.kick.useDefault = getBoolean("alts.kick.useDefault", true, "Specifies whether the plugin should use an internal\nmechanism to kick a player");
		alts.kick.time = getTime("alts.kick.time", "1m", "Specifies the AFK time after which\nmulti-account players will be kicked");
		alts.kick.limit = getInt("alts.kick.limit", 3, """
				Specifies a limit of kicks from the server before ban
				settings are used.
				To use this mechanism, you must enable the ban function
				and set a time greater than the warning time
				(if the limit is exceeded, the time from the ban settings will be omitted)
				Please type -1 to disable this feature.
				Minimal value is 2
				""");
		alts.kick.reason = getString("alts.kick.reason", "Using multiple accounts to AFK", "Specifies the reason for kicking a player from the server");
		alts.kick.commands.clear();
		alts.kick.commands.addAll(getList("alts.kick.commands", List.of(), """
				List of commands that will be executed when the player is kicked.
				Any placeholders from PlaceholderAPI (if available) and the
				following internal ones are supported:
				%player% - player name
				%reason% - reason for kicking
				If you don't want to use any commands, type []
				"""));
		addComment("alts.kick", "Settings for kicking players who use multi-account AFK");

		alts.ban.use = getBoolean("alts.ban.use", false, "Specifies whether the multi-accounts ban function is to be used");
		alts.ban.useDefault = getBoolean("alts.ban.useDefault", true, "Specifies whether the plugin should use an internal\nmechanism to ban a player");
		alts.ban.time = getTime("alts.ban.time", "20s", "Specifies the AFK time after which\nmulti-account players will be banned");
		alts.ban.reason = getString("alts.ban.reason", "Using multiple accounts to AFK", "Specifies the reason for banned a player from the server");
		alts.ban.commands.clear();
		alts.ban.commands.addAll(getList("alts.ban.commands", List.of(), """
				List of commands that will be executed when the player is banned.
				Any placeholders from PlaceholderAPI (if available) and the
				following internal ones are supported:
				%player% - player name
				%reason% - reason for banning
				If you don't want to use any commands, type []
				"""));
		alts.bypass.permissions.clear();
		alts.bypass.permissions.addAll(getList("alts.bypass.permissions", List.of("afkmagic.bypass.alts"), """
				Players with the following permissions will not be
				checked for using multi-accounts. First permission on list
				below is a default permission used when you bypass player
				by command
				"""));
		addComment("alts.ban", "Settings for banning players who use multi-account AFK");
		addComment("alts.ban.bypass", "Settings for bypassing players when detecting multi-accounts.");

//		limits.use = getBoolean("limits.use", false);
//		limits.limit = getInt("limits.limit", -1);
//		limits.whitelist.use = getBoolean("limits.whitelist.use", false);
//		limits.whitelist.regions.clear();
//		limits.whitelist.regions.addAll(getList("limits.whitelist.regions", List.of()));
//		limits.blacklist.use = getBoolean("limits.blacklist.use", false);
//		limits.blacklist.permissions.clear();
//		limits.blacklist.permissions.addAll(getList("limits.blacklist.permissions", List.of()));
//		limits.blacklist.regions.clear();
//		limits.blacklist.regions.addAll(getList("limits.blacklist.regions", List.of()));


		essentials.updateAFKStatus = getBoolean("essentials.updateAfkStatus", true, "Specifies whether AFK status should be synchronized");

		plugin.saveConfig();
	}

	private void structureConfig() {
		createSection("global", "-----------------------------------------------\nGeneral settings\n-----------------------------------------------");
		createSection("afk", "-----------------------------------------------\nAFK detection mechanism settings\n-----------------------------------------------");
		createSection("warn", "-----------------------------------------------\nConfiguration to display a warning for AFK players\nafter a certain period of time\n-----------------------------------------------");
		createSection("kick", "-----------------------------------------------\nSettings for kicking players who are AFK\n-----------------------------------------------");
		createSection("room", "-----------------------------------------------\nConfiguration for moving AFK players to a designated safe location\n-----------------------------------------------");
		createSection("alts", "-----------------------------------------------\nConfiguration for detecting multi-account AFK players\n-----------------------------------------------");
		createSection("essentials", "-----------------------------------------------\nConfiguration for AFK state synchronization with the Essentials plugin\n-----------------------------------------------");
		createSection("commands", "-----------------------------------------------\nConfiguration for plugin commands\n-----------------------------------------------");
		createSection("messages", "-----------------------------------------------\nLanguage specific messages\n-----------------------------------------------");
	}

	@Override
	public String getHeader() {
		List<String> title = new ArrayList<>();
		title.add("""
				
				      /$$$$$$  /$$$$$$$$ /$$   /$$       /$$      /$$  /$$$$$$   /$$$$$$  /$$$$$$  /$$$$$$\s
				     /$$__  $$| $$_____/| $$  /$$/      | $$$    /$$$ /$$__  $$ /$$__  $$|_  $$_/ /$$__  $$
				    | $$  \\ $$| $$      | $$ /$$/       | $$$$  /$$$$| $$  \\ $$| $$  \\__/  | $$  | $$  \\__/
				    | $$$$$$$$| $$$$$   | $$$$$/        | $$ $$/$$ $$| $$$$$$$$| $$ /$$$$  | $$  | $$     \s
				    | $$__  $$| $$__/   | $$  $$        | $$  $$$| $$| $$__  $$| $$|_  $$  | $$  | $$     \s
				    | $$  | $$| $$      | $$\\  $$       | $$\\  $ | $$| $$  | $$| $$  \\ $$  | $$  | $$    $$
				    | $$  | $$| $$      | $$ \\  $$      | $$ \\/  | $$| $$  | $$|  $$$$$$/ /$$$$$$|  $$$$$$/
				    |__/  |__/|__/      |__/  \\__/      |__/     |__/|__/  |__/ \\______/ |______/ \\______/\s
				
				                                      (C) 2025 Loovcik
				
				 =================================================================================================
				
				 Information:
				   1. Color Codes are supported with the "&" character.
				   2. HEX Codes are supported with the <#000000> and &x&r&r&g&g&b&b format.
				   3. MiniMessage is fully supported
				   4. Note that #rrggbb and &#rrggbb formats are no longer supported!
				   5. All times are supported in shortened format (1h, 20m, 10s, etc.) or can be given in seconds
				   6. Sound List
				      - https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html
				
				 =================================================================================================
				""");
		return String.join("\n", title);
	}

	/**
	 * Default constructor
	 * @param plugin Main plugin
	 */
	public ConfigurationManager(AFKMagic plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = plugin.getConfig();
		config.options().copyDefaults(true);
		structureConfig();
	}

	/*
			Klasy przechowujące konfigurację
	 */

	/**
	 * Konfiguracja ogólna czatu
	 */
	public static class cGlobal {
		/**
		 * Prefix używany na czacie przez plugin
		 */
		public String prefix;
		public boolean debug;
		public boolean logToConsole;
	}

	/**
	 * Konfiguracja dotycząca stanu AFK
	 */
	public static class cAFK {
		public Time interval;
		/**
		 * Określa, czy gracze, którzy są AFK, mają być ignorowani
		 * podczas obliczania wymaganej ilości graczy, którzy muszą
		 * spać, aby pominąć noc.
		 */
		public boolean ignoreForSleep;

		/**
		 * Ustawienia dotyczące włączania stanu AFK
		 */
		public cAFKStart start = new cAFKStart();

		/**
		 * Ustawienia dotyczące wyłączania stanu AFK
		 */
		public cAFKStop end = new cAFKStop();

		/**
		 * Ustawienia ochrony graczy AFK
		 */
		public vAfkProtect protect = new vAfkProtect();

		/**
		 * Ustawienia dotyczące pomijania przechodzenia w stan AFK
		 */
		public prBypass bypass = new prBypass();
	}

	/**
	 * Konfiguracja dotycząca wyświetlania ostrzeżenia dla graczy AFK
	 * po określonym czasie
	 */
	public static class cWarn {
		/**
		 * Określa, czy funkcja ostrzegania ma być używana
		 */
		public boolean use;

		/**
		 * Określa, po jakim czasie od przejścia w stan AFK ma zostać
		 * wyświetlone ostrzeżenie. Podany czas w sekundach.
		 */
		public Time time;

		/**
		 * Lista komunikatów, które zostaną losowo wyświetlone graczowi.
		 * Obsługiwane są wszystkie placeholdery z PlaceholderAPI
		 * (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 * - %player% - nazwa gracza
		 * <p>
		 * Ustaw to pole na [], jeśli nie chcesz, aby
		 * komunikat był wyświetlany. Przydatne, jeśli chcesz
		 * tylko wykonać określoną komendę.
		 * </p>
		 */
		public List<String> messages = new ArrayList<>();

		/**
		 * Lista komend, które zostaną wykonane w momencie
		 * ostrzeżenia gracza.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza
		 * </p><br>
		 * <p>
		 *     Jeśli nie chcesz używać żadnych komend, wpisz []
		 * </p>
		 */
		public List<String> commands = new ArrayList<>();

		/**
		 * Konfiguracja pomijania ostrzegania graczy
		 */
		public prBypass bypass = new prBypass();
	}

	/**
	 * Konfiguracja dotycząca wyrzucania graczy AFK po określonym
	 * czasie
	 */
	public static class cKick {
		/**
		 * Określa, czy funkcja wyrzucania z serwera graczy
		 * po określonym czasie ma być używana
		 */
		public boolean use;

		/**
		 * Określa, czy przy wyrzucaniu plugin ma zastosować
		 * domyślną komendę
		 */
		public boolean useDefault;

		/**
		 * Określa, po jakim czasie (w sekundach) od przejścia w stan AFK
		 * gracz ma zostać wyrzucony.
		 */
		public Time time;

		/**
		 * Określa, przy jakiej ilości graczy funkcja wyrzucania ma
		 * zostać aktywowana
		 */
		public int minPlayersToActivate;

		/**
		 * Określa powód wyrzucenia używany przy wywołaniu
		 * domyślnej, jak i własnych komend
		 */
		public String reason;

		/**
		 * Ustawienia dotyczące informowania wszystkich graczy
		 * o wyrzuceniu danego gracza z serwera
		 */
		public umBroadcast broadcast = new umBroadcast();

		/**
		 * Lista komend, które zostaną wykonane w momencie
		 * wyrzucenia gracza.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza<br>
		 *     <code>%reason%</code> - powód wyrzucenia<br>
		 *     <code>%time%</code> - czas AFK
		 * </p><br>
		 * <p>
		 *     Jeśli nie chcesz używać żadnych komend, wpisz []
		 * </p>
		 */
		public List<String> commands = new ArrayList<>();

		/**
		 * Konfiguracja pomijania wyrzucania graczy
		 */
		public prBypass bypass = new prBypass();
	}

	/**
	 * Konfiguracja dotycząca przenoszenia graczy AFK w wyznaczone, bezpieczne miejsce
	 */
	public static class cAFKRoom {
		/**
		 * Określa, czy funkcja przenoszenia graczy AFK w wyznaczone, bezpieczne miejsce
		 * ma być używana
		 */
		public boolean use;

		/**
		 * Określa, po jakim czasie (w sekundach) od momentu przejścia w stan AFK
		 * gracz ma zostać przeniesiony
		 */
		public Time time;

		/**
		 * Lista komend, które zostaną wykonane w momencie
		 * przeniesienia gracza do bezpiecznego miejsca.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza
		 * </p><br>
		 * <p>
		 *     Jeśli nie chcesz używać żadnych komend, wpisz []
		 * </p>
		 */
		public List<String> commands = new ArrayList<>();

		/**
		 * Lista wiadomości, które zostaną losowo wyświetlone graczowi,
		 * który jest przenoszony do bezpiecznego miejsca.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli jest dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza
		 * </p>
		 * <p>
		 *     Pozostaw to pole puste, jeśli nie chcesz wyświetlać
		 *     żadnego komunikatu
		 * </p>
		 */
		public List<String> messages = new ArrayList<>();

		/**
		 * Ustawienia dotyczące informowania wszystkich graczy
		 * o przeniesieniu danego gracza w bezpieczne miejsce
		 */
		public umBroadcast broadcast = new umBroadcast();

		/**
		 * Ustawienia dotyczące graczy, którzy nie będą przenoszeni
		 */
		public prBypass bypass = new prBypass();

		/**
		 * Lokalizacja bezpiecznego miejsca
		 */
		public vLocation location = new vLocation();
	}

	/**
	 * Konfiguracja dotycząca wykrywania multikont graczy AFK
	 */
	public static class cAlts {
		/**
		 * Określa, czy funkcja wykrywania multikont ma
		 * być używana
		 */
		public boolean use;

		/**
		 * Ustawienia dotyczące ostrzegania graczy o restrykcjach
		 * w używaniu multikont do AFK
		 */
		public vAltsWarning warning = new vAltsWarning();

		/**
		 * Ustawienia dotyczące wyrzucania graczy, którzy używają
		 * multikont do AFK
		 */
		public vAltsKick kick = new vAltsKick();

		/**
		 * Ustawienia dotyczące banowania graczy, którzy używają
		 * multikont do AFK
		 */
		public vAltsKick ban = new vAltsKick();

		/**
		 * Ustawienia dotyczące pomijania graczy przy wykrywaniu
		 * multikont.
		 */
		public prBypass bypass = new prBypass();
	}

	/**
	 * Konfiguracja dotycząca wyrzucania graczy, którzy najdłużej są AFK,
	 * w przypadku osiągnięcia określonej ilości graczy na serwerze
	 */
	public static class cLimits {
		/**
		 * Określa, czy funkcja limitowania graczy AFK na serwerze
		 * ma być używana
		 */
		public boolean use;

		/**
		 * Określa, ilu maksymalnie graczy może znajdować się na
		 * serwerze. Wpisz <code>-1</code>, aby użyć ustawień serwera
		 */
		public int limit;

		/**
		 * Ustawienia określające, że tylko gracze spełniający
		 * dane kryteria mogą zostać wyrzuceni.
		 */
		public vWhitelist whitelist = new vWhitelist();

		/**
		 * Ustawienia określające, którzy gracze nie zostają
		 * nigdy wyrzuceni
		 */
		public vBlacklist blacklist = new vBlacklist();
	}

	/**
	 * Konfiguracja dotycząca synchronizacji stanu AFK
	 * z pluginem Essentials
	 */
	public static class cEssentials {
		/**
		 * Określa, czy stan AFK ma być synchronizowany
		 */
		public boolean updateAFKStatus;
	}

	/**
	 * Ustawienia dotyczące momentu włączenia stanu AFK
	 */
	public static class cAFKStart {
		/**
		 * Określa, czy funkcja AFK ma być używana
		 */
		public boolean use;

		/**
		 * Określa czas (w sekundach) bezczynności gracza, po którym
		 * przejdzie on w stan AFK
		 */
		public Time time;

		/**
		 * Lista komend, która zostanie wykonana w momencie
		 * przejścia gracza w stan AFK.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza<br>
		 * </p>
		 */
		public List<String> commands = new ArrayList<>();

		/**
		 * Treść wiadomości, która zostanie wyświetlona graczowi,
		 * który przechodzi w stan AFK.<br>
		 * Pozostaw puste, jeśli nie chcesz, aby wiadomość była
		 * wyświetlana
		 */
		public String message;

		/**
		 * Określa dźwięk, który będzie odtwarzany graczowi przechodzącemu
		 * w stan AFK.
		 */
		public String sound;

		/**
		 * Ustawienia dotyczące ogłaszania przejścia w stan AFK
		 * innym graczom
		 */
		public umbBroadcast broadcast = new umbBroadcast();

		/**
		 * Ustawienia detektorów wykrywających próby omijania przejścia
		 * w stan AFK
		 */
		public vStartDetectors detectors = new vStartDetectors();
	}

	/**
	 * Konfiguracja dotycząca wychodzenia ze stanu AFK
	 */
	public static class cAFKStop {
		/**
		 * Lista komend, która zostanie wykonana w momencie
		 * wyjścia gracza ze stanu AFK.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza<br>
		 *     <code>%time%</code> - czas (w sekundach), przez który gracz był AFK
		 * </p><br>
		 */
		public List<String> commands = new ArrayList<>();

		/**
		 * Treść wiadomości wyświetlana graczowi, który wychodzi ze
		 * stanu AFK.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza<br>
		 *     <code>%time%</code> - czas, przez który gracz był AFK
		 * </p><br>
		 * <p>
		 *     Pozostaw puste, jeśli nie chcesz wyświetlać wiadomości graczowi
		 * </p>
		 */
		public String message;

		/**
		 * Ustawienia ogłaszania wszystkim graczom o wyjściu gracza
		 * ze stanu AFK
		 */
		public umBroadcast broadcast = new umBroadcast();

		/**
		 * Ustawienia detektorów, wykrywających moment wyjścia
		 * ze stanu AFK
		 */
		public vEndDetectors detectors = new vEndDetectors();
	}

	/**
	 * Konfiguracja pomijania
	 */
	public static class prBypass {
		/**
		 * Gracze posiadający poniższe uprawnienia nie będą brani pod uwagę
		 */
		public List<String> permissions = new ArrayList<>();

		/**
		 * Gracze znajdujący się w poniższych regionach (jeśli dostępny WorldGuard)
		 * nie będą brani pod uwagę
		 */
		public List<String> regions = new ArrayList<>();

		/**
		 * Gracz z ustawionym trybem gry nie będą brani pod uwagę
		 */
		public gameModeBypass gameMode = new gameModeBypass();

		/**
		 * Gracze znajdujący się w określonych światach nie będą
		 * brani pod uwagę
		 */
		public List<String> worlds = new ArrayList<>();
	}

	/**
	 * Ustawienia dotyczące pomijania na podstawie trybu gry
	 */
	public static class gameModeBypass
	{
		/**
		 * Gracze w trybie obserwatora nie będą brani pod uwagę
		 */
		public boolean spectator;

		/**
		 * Gracze w trybie kreatywnym nie będą brani pod uwagę
		 */
		public boolean creative;
	}

	/**
	 * Konfiguracja dotycząca ogłaszania wszystkim graczom
	 */
	public static class umBroadcast {
		/**
		 * Określa, czy funkcja ogłaszania ma być używana
		 */
		public boolean use;

		/**
		 * Lista wiadomości, która zostanie wysłana do wszystkich
		 * graczy. Wiadomości z listy są losowane.
		 */
		public List<String> messages = new ArrayList<>();
	}

	/**
	 * Rozszerzenie konfiguracji ogłaszania
	 */
	public static class umpBroadcast extends umBroadcast {
		/**
		 * Określa uprawnienia, które muszą mieć gracze, aby otrzymać
		 * ogłoszenie
		 */
		public List<String> permissions = new ArrayList<>();
	}

	/**
	 * Rozszerzenie konfiguracji ogłoszenia
	 */
	public static class umbBroadcast extends umBroadcast {
		/**
		 * Ustawienia pomijania wyświetlania ogłoszenia
		 */
		public vPermission bypass = new vPermission();
	}

	public static class vPermission {
		/**
		 * Gracze posiadający określone uprawnienia, nie spowodują
		 * wysłania ogłoszenia do wszystkich graczy
		 */
		public List<String> permissions = new ArrayList<>();
	}

	/**
	 * Konfiguracja ostrzeżeń w przypadku wykrycia
	 * używania multikont do AFK
	 */
	public static class vAltsWarning {
		/**
		 * Określa czas (w sekundach) od wykrycia, po którym
		 * zostanie wysłana wiadomość ostrzegająca o restrykcjach
		 * dotyczących używania multikont do AFK
		 */
		public Time time;

		/**
		 * Ustawienia ostrzegania gracza
		 */
		public umBroadcast self = new umBroadcast();

		/**
		 * Ustawienia ostrzegania pozostałych kont powiązanych z graczem
		 */
		public umBroadcast alts = new umBroadcast();

		/**
		 * Ustawienia dotyczące wyświetlania informacji o używaniu
		 * przez gracza multikont do administracji serwera
		 */
		public umpBroadcast admins = new umpBroadcast();
	}

	/**
	 * Konfiguracja wyrzucania graczy korzystających z multikont do AFK
	 */
	public static class vAltsKick {
		/**
		 * Określa, czy funkcja wyrzucania ma być używana
		 */
		public boolean use;

		/**
		 * Określa, czy plugin ma używać wewnętrznego mechanizmu
		 * do wyrzucenia gracza
		 */
		public boolean useDefault;

		/**
		 * Określa czas AFK, po którym gracze używający
		 * multikont zostaną wyrzuceni
		 */
		public Time time;

		/**
		 * Określa limit wyrzuceń z serwera, zanim zostaną użyte ustawienia banowania.
		 * Aby korzystać z tego mechanizmu, należy włączyć funkcję banowania i ustawić
		 * w niej czas większy od czasu ostrzegania (w przypadku przekroczenia limitu,
		 * czas z ustawień bana zostanie pominięty)
		 */
		public int limit;

		/**
		 * Określa powód wyrzucenia gracza z serwera
		 */
		public String reason;

		/**
		 * Lista komend, które zostaną wykonane w momencie wyrzucenia graczy
		 * używających multikont do AFK.<br><br>
		 * <p>
		 *     Obsługiwane są dowolne placeholdery z PlaceholderAPI
		 *     (jeśli dostępny) oraz następujące wewnętrzne:<br>
		 *     <code>%player%</code> - nazwa gracza
		 *     <code>%reason%</code> - powód wyrzucenia
		 * </p>
		 */
		public List<String> commands = new ArrayList<>();
	}

	/**
	 * Konfiguracja określająca, którzy gracze magą zostać wyrzuceni
	 * w przypadku osiągnięcia limitu
	 */
	public static class vWhitelist {
		/**
		 * Określa, czy funkcja ma być używana
		 */
		public boolean use;

		/**
		 * Tylko gracze znajdujący się w podanych regionach (wymagany WorldGuard)
		 * mogą zostać wyrzuceni w przypadku osiągnięcia limitu osób na serwerze
		 */
		public List<String> regions = new ArrayList<>();
	}

	/**
	 * Konfiguracja określająca, którzy gracze nie mogą zostać wyrzuceni
	 * pomimo osiągnięcia limitu
	 */
	public static class vBlacklist extends vWhitelist {
		/**
		 * Gracze posiadający wybrane uprawnienia nigdy nie zostaną
		 * wyrzuceni pomimo osiągnięcia limitu graczy na serwerze
		 */
		public List<String> permissions = new ArrayList<>();
	}

	/**
	 * Ustawienia detektorów wykrywających mechanizmy uniemożliwiające przejście w stan AFK
	 */
	public static class vStartDetectors {
		/**
		 * Ustawienia detekcji mechanizmów uniemożliwiających przejście w stan AFK.<br><br>
		 * Wykrywane są między innymi stanie na wodzie z bąbelkami, jeżdżenie w wagoniku, autoclicker
		 */
		public vStartDetectorAfkMachine afkMachine = new vStartDetectorAfkMachine();
	}

	public static class vStartDetectorAfkMachine{
		/**
		 * Określa, czy funkcja detekcji mechanizmów uniemożliwiających przejście
		 * w stan AFK ma być używana
		 */
		public boolean use;
	}

	/**
	 * Konfiguracja detektorów wykrywających moment wyjścia ze stanu AFK
	 */
	public static class vEndDetectors {
		/**
		 * Ustawienia wykrywania ruchu gracza
		 */
		public dMove move = new dMove();

		/**
		 * Ustawienia wykrywania obrotu gracza
		 */
		public dRotate rotate = new dRotate();

		/**
		 * Określa, czy interakcja z otoczeniem powoduje wyjście
		 * ze stanu AFK
		 */
		public dInteract interact = new dInteract();

		/**
		 * Określa, czy zjedzenie czegoś powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean consume;

		/**
		 * Ustawienia detekcji pisania na czacie
		 */
		public dChat chat = new dChat();

		/**
		 * Ustawienia detekcji interakcji z przedmiotami
		 */
		public dItem item = new dItem();

		/**
		 * Ustawienia detekcji interakcji z blokami
		 */
		public dBlock block = new dBlock();

		/**
		 * Określa, czy strzyżenie powoduje wyjście ze
		 * stanu AFK
		 */
		public boolean shear;

		/**
		 * Określa, czy użycie sprintu powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean sprint;

		/**
		 * Określa, czy użycie kucania powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean sneak;

		/**
		 * Ustawienia detekcji interakcji z wiadrem
		 */
		public dBucket bucket = new dBucket();

		/**
		 * Określa, czy zmiana świata przez gracza powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean changeWorld;

		/**
		 * Określa, czy interakcja z książką powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean book;

		/**
		 * Określa, czy teleportacja powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean teleport;
	}

	public static class dBucket {
		/**
		 * Określa, czy napełnienie wiadra powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean fill;

		/**
		 * Określa, czy opróżnienie wiadra powoduje wyjście
		 * ze stanu AFK
		 */
		public boolean empty;
	}
	public static class dItem {
		/**
		 * Określa, czy wyrzucenie przedmiotu powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean drop;

		/**
		 * Określa, czy podniesienie przedmiotu powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean pickup;

		/**
		 * Określa, czy zniszczenie przedmiotu powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean destroy;
	}

	public static class dInteract {
		public boolean use;
		public dAutoClicker autoClicker = new dAutoClicker();

	}

	public static class dAutoClicker {
		public boolean use;
		public int avgDeviation;
		public int checkTime;
		public dAutoClickerLog log = new dAutoClickerLog();
		public boolean cancelEvent;
		public dKick kick = new dKick();
		public boolean alert;
		public String alertMessage;
		public vPermission bypass = new vPermission();
	}

	public static class dKick {
		public boolean use;
		public Time delay;
		public String reason;
		public vPermission bypass = new vPermission();
	}

	public static class dAutoClickerLog {
		public boolean use;
		public String message;
		public boolean onlyOnce;
	}

	public static class dBlock {
		/**
		 * Określa, czy postawienia bloku powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean place;

		/**
		 * Określa, czy zniszczenie bloku powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean destroy;
	}

	public static class dMove {
		/**
		 * Określa, czy poruszenie się gracza powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean use;

		/**
		 * Określa dystans, na który musi poruszyć się gracz,
		 * aby wyjść ze stanu AFK
		 */
		public double distance;
	}

	public static class dRotate {
		/**
		 * Określa, czy obrócenie się gracza spowoduje
		 * wyjście ze stanu AFK
		 */
		public boolean use;

		/**
		 * Określa zakres obrotu, który musi wykonać gracz,
		 * aby wyjść ze stanu AFK
		 */
		public double angle;
	}

	public static class dChat {
		/**
		 * Określa, czy pisanie na czacie powoduje
		 * wyjście ze stanu AFK
		 */
		public boolean use;

		/**
		 * Lista komend, których użycie przez gracza nie spowoduje
		 * wyjścia ze stanu AFK
		 */
		public List<String> blacklistCommands = new ArrayList<>();
	}

	public static class vLocation {
		public double x;
		public double y;
		public double z;
		public String world;

		/**
		 * Sprawdza, czy bezpieczne miejsce zostało skonfigurowane
		 * @return True, jeśli wartości są inne niż domyślne
		 */
		public boolean isSet(){
			return (!(x == 0 && y == 0 && z == 0 && world.equalsIgnoreCase("world_none")));
		}
	}

	/**
	 * Konfiguracja ochrony gracza będącego AFK
	 */
	public static class vAfkProtect {
		/**
		 * Określa, czy uniemożliwić przesuwanie gracza będącego AFK<br><br>
		 * <p>
		 *     UWAGA! W niektórych sytuacjach może powodować zbudowanie się gracza
		 *     próbującego przesunąć gracza AFK i uniemożliwić mu ruch
		 * </p>
		 */
		public boolean move;

		/**
		 * Ustawienia ochrony przed obrażeniami
		 */
		public vProtectHurt hurt = new vProtectHurt();

		/**
		 * Ustawienia ochrony przed mobami
		 */
		public vProtectMob mob = new vProtectMob();

		/**
		 * Określa, czy gracz AFK może podnosić rzucone mu przedmioty
		 */
		public boolean pickup;
	}

	/**
	 * Ustawienia ochrony przed obrażeniami
	 */
	public static class vProtectHurt {
		/**
		 * Określa, czy inni gracze mogą zadać obrażenia
		 * graczowi będącemu AFK
		 */
		public boolean players;

		/**
		 * Określa, czy pozostałe interakcje mogą zadawać obrażenia
		 * graczowi będącemu AFK. Dotyczy to zarówno obrażeń od
		 * mobów, jak i lawy, topienia, spadających bloków itp.
		 */
		public boolean others;
	}

	/**
	 * Ustawienia ochrony przed mobami
	 */
	public static class vProtectMob {
		/**
		 * Określa zasady spawnowania się mobów w pobliżu
		 * gracza AFK, jeśli dookoła niego nie znajdują się
		 * gracze niebędący AFK
		 */
		public vProtectSpawn spawn = new vProtectSpawn();

		/**
		 * Określa, czy zapobiegać targetowaniu się mobów na
		 * graczu AFK
		 */
		public boolean target;
	}

	public static class vProtectSpawn {
		/**
		 * Określa, czy wyłączać spawnowanie się mobów
		 * w pobliżu gracza AFK
		 */
		public boolean use;

		/**
		 * Określa czas, po którym wyłączone zostanie
		 * spawnowanie się mobów ze spawnerów
		 */
		public Time delay;
	}
}