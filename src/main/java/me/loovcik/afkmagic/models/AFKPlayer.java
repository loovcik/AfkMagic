package me.loovcik.afkmagic.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.api.IAFKPlayer;
import me.loovcik.afkmagic.events.AFKEndEvent;
import me.loovcik.afkmagic.events.AFKStartEvent;
import me.loovcik.afkmagic.managers.actions.RoomAction;
import me.loovcik.afkmagic.utils.Others;
import me.loovcik.core.types.Time;
import me.loovcik.core.ChatHelper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.loovcik.core.managers.ConfigurationManager.getRandom;
import static me.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;
import static me.loovcik.afkmagic.utils.Extensions.isOnWorld;

public final class AFKPlayer implements IAFKPlayer
{
	private final AFKMagic plugin;
	private static final Map<UUID, AFKPlayer> cache = new ConcurrentHashMap<>();

	private final UUID uuid;
	@Getter private final String name;
	@Getter private final String ip;

	@Getter@Setter private Long gameTime = 0L;
	@Getter	private Long startAfkDate = 0L;
	@Getter private Long stopAfkDate = 0L;
	@Getter@Setter private Long currentAfkTime = 0L;
	@Getter@Setter private Long totalAfkTime = 0L;

	@Getter@Setter private Long startAfkRoomDate = -1L;
	@Getter@Setter private boolean isInsideAfkRoom = false;
	@Getter@Setter private Location lastLocation;
	@Getter@Setter private boolean isTeleporting;

	@Getter private Long calculateFrom = 0L;
	@Getter@Setter private boolean isWarned = false;
	@Getter@Setter private boolean isKicked = false;
	@Getter@Setter private boolean isInactive = false;

	@Getter@Setter private int kickCount;
	@Getter@Setter private Long altsDetectTime = 0L;
	@Getter@Setter private boolean altsDetected = false;
	@Getter@Setter private boolean altsWarned;
	@Getter@Setter private boolean altsKicked;
	@Getter@Setter private boolean altsBaned;
	@Getter@Setter private int altsKickedCount;
	@Getter@Setter private int altsDetectedCount;

	@Getter private boolean isAFK;
	@Getter private Long lastActivity;
	@Getter private boolean changed = false;

	public UUID getUniqueId() { return uuid; }

	/** Konwertuje klasę AFKPlayer na klasę Bukkit Player */
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}

	/** Pobiera tryb, w którym znajduje się gracz */
	public GameMode getGameMode() { return getPlayer().getGameMode(); }

	/** Sprawdza, czy gracz jest ukryty */
	public boolean isVanished(){
		if(!Bukkit.getOfflinePlayer(uuid).isOnline()) return false;
		Player player = getPlayer();
		for (MetadataValue meta : player.getMetadata("vanished"))
			if (meta.asBoolean()) return true;
		return false;
	}

	public Long getIdleTime() { return System.currentTimeMillis() - lastActivity; }

	/** Przełącza gracza w stan AFK */
	public void startAFK()
	{
		Bukkit.getScheduler().runTask(plugin, () -> {
			AFKStartEvent event = new AFKStartEvent(this);
			plugin.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				return;

			if (!plugin.configuration.afk.start.message.equalsIgnoreCase(""))
				ChatHelper.message(this.getPlayer(), plugin.dependencies.placeholderAPI.process(getPlayer(), plugin.configuration.afk.start.message));

			String bMessage = getRandom(plugin.configuration.afk.start.broadcast.messages);
			if (plugin.configuration.afk.start.broadcast.use && !bMessage.equalsIgnoreCase("") && !isVanished())
				if (!hasPlayerPermission(this.getPlayer(), plugin.configuration.afk.start.broadcast.bypass.permissions))
					ChatHelper.broadcast(plugin.dependencies.placeholderAPI.process(getPlayer(), bMessage.replaceAll("%player%", getName())));
			if (!plugin.configuration.afk.start.sound.equalsIgnoreCase(""))
				Others.playSound(plugin.configuration.afk.start.sound, this);
			forceStartAFK();

			if (plugin.configuration.global.logToConsole || plugin.configuration.global.debug)
				ChatHelper.console("changeAfkStatus{value=<green>true</green>, player="+getName()+"}");
			changed = true;
		});
	}

	public void stopAFK(StopReason reason)
	{
		if (System.currentTimeMillis() - stopAfkDate < Time.ofSeconds(2L).toMilliseconds()) return;

		stopAfkDate = System.currentTimeMillis();
		long time = System.currentTimeMillis() - startAfkDate;
		String afkTime = Time.ofMillis(time).format();

		if (isTeleporting || (isInsideAfkRoom && System.currentTimeMillis() - startAfkRoomDate < Time.ofSeconds(2L).toMilliseconds())) return;

		Bukkit.getScheduler().runTask(plugin, () -> {
			AFKEndEvent event = new AFKEndEvent(this, time);
			plugin.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				return;

			if (!plugin.configuration.afk.end.message.equalsIgnoreCase(""))
				ChatHelper.message(getPlayer(), plugin.dependencies.placeholderAPI.process(getPlayer(), plugin.configuration.afk.end.message));

			String rMessage = getRandom(plugin.configuration.afk.end.broadcast.messages);
			if (plugin.configuration.afk.end.broadcast.use && !rMessage.equalsIgnoreCase("") && !isVanished())
				if (!hasPlayerPermission(this.getPlayer(), plugin.configuration.afk.start.broadcast.bypass.permissions))
					ChatHelper.broadcast(plugin.dependencies.placeholderAPI.process(getPlayer(), rMessage.replaceAll("%player%", getName()).replaceAll("%time%", afkTime)));

			if (plugin.configuration.global.logToConsole || plugin.configuration.global.debug)
				ChatHelper.console("changeAfkStatus{value=<red>false</red>, player="+getName()+", reason="+reason+", duration="+afkTime+"}");
			RoomAction.exit(this);
			forceStopAFK();
		});
	}

	/** Włącza/wyłącza stan AFK gracza */
	public void toggleAFK() {
		if (isAFK()) stopAFK(StopReason.MANUAL);
		else startAFK();
	}

	/**
	 * Odświeża dane o ostatniej aktywności gracza.
	 * Gdy gracz staje się aktywny, wyłączany jest też AFK
	 * @param ignoreInactive Określa, czy ignorować flagę Inactive
	 *                       pochodzącą z wykrywania maszyn AFK
	 */
	public void interact(StopReason reason, boolean ignoreInactive){
		if (getPlayer() == null) return;
		if (!ignoreInactive && isInactive && !isInsideAfkRoom) {
			isInactive = false;
			return;
		}
		lastActivity = System.currentTimeMillis();
		isInactive = false;
		if (getPlayer().isOnline())
			if (isAFK()) stopAFK(reason);
	}

	private void forceStartAFK(){
		isAFK = true;
		getPlayer().setMetadata("isAFK", new FixedMetadataValue(plugin, true));
		calculateFrom = System.currentTimeMillis();

		startAfkDate = System.currentTimeMillis();
		plugin.dependencies.essentials.setAFK(getUniqueId(), isAFK());
		// Ignorowanie gracza AFK przy wyliczaniu osób śpiących
		if (plugin.configuration.afk.ignoreForSleep) getPlayer().setSleepingIgnored(true);
		plugin.commandManager.run(plugin.configuration.afk.start.commands, this);
		// Wyłączenie spawnerów, jeśli jest to włączone w konfiguracji
	}

	private void forceStopAFK(){
		RoomAction.forceExit(this);
		lastActivity = System.currentTimeMillis();
		changed = true;
		isAFK = false;
		if (getPlayer() != null)
			getPlayer().removeMetadata("isAFK", plugin);
		isWarned = false;
		isKicked = false;
		isTeleporting = false;
		isInactive = false;
		isInsideAfkRoom = false;
		currentAfkTime = 0L;
		startAfkDate = 0L;

		plugin.altsManager.getAll(this).forEach((x) -> {
			x.altsWarned = false;
			x.altsKicked = false;
			x.altsBaned = false;
		});

		if (getPlayer() != null && plugin.configuration.afk.ignoreForSleep) getPlayer().setSleepingIgnored(false);
		plugin.dependencies.essentials.setAFK(getUniqueId(), isAFK());
		plugin.commandManager.run(plugin.configuration.afk.end.commands, this);

		save();
	}

	/**
	 * Oblicza aktualne dane o czasie AFK
	 */
	public void calculateAFKTime(){
		Long currentAFKTime = getTotalAfkTime();
		Long timeAFK = System.currentTimeMillis() - calculateFrom;
		calculateFrom = System.currentTimeMillis();
		setTotalAfkTime(currentAFKTime + timeAFK);
	}

	public boolean check(){
		if (Bukkit.getPlayer(uuid) != null && !isVanished())
		{
			// Sprawdzanie, czy gracz nie ma uprawnień do pomijania AFK

			if (hasPlayerPermission(getPlayer(), plugin.configuration.afk.bypass.permissions))
				return false;

			// Sprawdzanie, czy gracz nie jest w regionie objętym pomijaniem AFK
			if (plugin.dependencies.worldGuard.isEnabled())
			{
				String[] regions = plugin.configuration.afk.bypass.regions.toArray(new String[0]);
				if (plugin.dependencies.worldGuard.isOnRegion(this, regions))
					return false;
			}

			// Sprawdzanie, czy gracz nie ma ustawionego trybu objętego pomijaniem AFK
			if (plugin.configuration.afk.bypass.gameMode.spectator && getGameMode().equals(GameMode.SPECTATOR)) return false;
			if (plugin.configuration.afk.bypass.gameMode.creative && getGameMode().equals(GameMode.CREATIVE)) return false;

			// Sprawdzanie, czy gracz nie jest w świecie, w którym AFK jest wyłączone
			if (isOnWorld(this, plugin.configuration.afk.bypass.worlds)) return false;

			Long timeToAFK = plugin.configuration.afk.start.time.toMilliseconds();
			return getIdleTime() >= timeToAFK;
		}
		return false;
	}

	public void load() {
		try
		{
			File f = new File(AFKMagic.getInstance().getDataFolder(), "statistics/"+getUniqueId()+".yml");
			if (!f.exists())
			{
				totalAfkTime = 0L;
				kickCount = 0;
				altsKickedCount = 0;
				altsDetectedCount = 0;
			}
			YamlConfiguration statistics = YamlConfiguration.loadConfiguration(f);
			totalAfkTime = statistics.getLong(getUniqueId() + ".totalAFKTime", 0L);
			kickCount = statistics.getInt(getUniqueId() + ".totalKicks", 0);
			altsKickedCount = statistics.getInt(getUniqueId() + ".altsKicks", 0);
			altsDetectedCount = statistics.getInt(getUniqueId() + ".altsTotalDetects", 0);

			changed = false;
			if (plugin.configuration.global.debug)
				ChatHelper.console(getName()+" data <green>loaded</green>");

		}
		catch (Exception e){
			ChatHelper.console("&cUnable to load data for player "+getName());
		}

		lastActivity = System.currentTimeMillis();
	}

	public void save() {
		if (!changed) {
			if (plugin.configuration.global.debug)
				ChatHelper.console("No changes detected on "+getName()+" data");
			return;
		}

		if (plugin.configuration.global.debug)
			ChatHelper.console("Saving "+getName()+" data...");
		if (totalAfkTime == 0 && kickCount == 0 && altsKickedCount == 0 && altsDetectedCount == 0)
			return;

		File f = new File(AFKMagic.getInstance().getDataFolder(), "statistics/"+getUniqueId()+".yml");
		if (!f.exists()){
			try{
				if (!f.createNewFile())
					ChatHelper.console("&cFailed to create file "+f.getName());
			}
			catch (IOException e){
				ChatHelper.console("&cFailed to create file "+f.getName());
			}
		}

		YamlConfiguration statistics = YamlConfiguration.loadConfiguration(f);
		statistics.set(getUniqueId() + ".totalAFKTime", totalAfkTime);
		statistics.set(getUniqueId() + ".totalKicks", kickCount);
		statistics.set(getUniqueId() + ".altsKicks", altsKickedCount);
		statistics.set(getUniqueId() + ".altsTotalDetects", altsDetectedCount);
		statistics.set(getUniqueId() + ".name", getName());

		try
		{
			statistics.save(f);
			changed = false;
			if (plugin.configuration.global.debug)
				ChatHelper.console(getName()+" data <green>saved</green>");
		}
		catch (IOException e){
			ChatHelper.console("&cUnable to save player data for "+getName());
		}
	}

	public void unload() {
		if (plugin.configuration.global.debug)
			ChatHelper.console("Unloading "+getName()+" data...");
		final String playerName = getName();
		if (isAFK) forceStopAFK();
		if (changed) save();
		if (cache.containsKey(uuid)){
			cache.remove(getUniqueId());
			if (plugin.configuration.global.debug)
				ChatHelper.console(playerName+" cache data <green>removed</green> from memory");
		}
		if (plugin.configuration.global.debug)
			ChatHelper.console(playerName+" data <green>unloaded</green>");
	}

	public static void unloadAll() {
		cache.values().forEach(AFKPlayer::unload);
	}

	/** Pobiera lub tworzy wrap gracza */
	public static AFKPlayer get(UUID uuid) {
		if(!cache.containsKey(uuid)) {
			AFKPlayer player = new AFKPlayer(uuid);
			player.load();
			cache.put(uuid, player);
		}
		return cache.get(uuid);
	}

	public static List<AFKPlayer> getPlayers() { return cache.values().stream().toList(); }

	public static List<AFKPlayer> getAFKPlayers() { return cache.values().stream().filter(AFKPlayer::isAFK).toList(); }

	public static boolean contains(UUID uuid) { return cache.containsKey(uuid); }

	private AFKPlayer(UUID uuid){
		plugin = AFKMagic.getInstance();
		this.uuid = uuid;
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		this.name = offlinePlayer.getName();
		if (getPlayer() != null) {
			InetSocketAddress socketAddress = getPlayer().getAddress();
			if (socketAddress != null)
				this.ip = socketAddress.getHostString().split(":")[0].replaceAll("/", "");
			else this.ip = null;
		}
		else this.ip = null;
	}

	public enum StopReason {
		NONE,
		CHAT,
		COMMAND,
		MANUAL,
		MOVE,
		TAKE_DAMAGE,
		INTERACT,
		INVENTORY_CLICK,
		BLOCK_PLACE,
		BLOCK_DESTROY,
		BLOCK_DAMAGE,
		CLICK,
		CONSUME,
		BED,
		CHANGE_WORLD,
		EDIT_BOOK,
		DROP_ITEM,
		PICKUP_ITEM,
		ITEM_BREAK,
		SHEAR,
		SPRINT,
		SNEAK,
		BUCKET_FILL,
		BUCKET_EMPTY,
		TELEPORT,
		MAKE_DAMAGE,
		HAND_MOVE
	}
}