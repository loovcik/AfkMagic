package me.loovcik.afkmagic;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;
import me.loovcik.afkmagic.events.AFKMachineDetectEvent;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.afkmagic.utils.MoveMonitor;
import me.loovcik.afkmagic.utils.MoveStorage;
import me.loovcik.core.ChatHelper;

import java.util.*;
import java.util.concurrent.*;

import static me.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;

public class AFKMagicListeners implements Listener
{
	private final AFKMagic plugin;
	private final MoveMonitor monitor;
	private final HashMap<UUID, Location> playerLocations = new HashMap<>();
	private final Map<UUID, Deque<Long>> clickTimes = new ConcurrentHashMap<>();
	private final Map<UUID, ScheduledFuture<?>> clickTimeouts = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> detected = new HashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final Map<UUID, ScheduledFuture<?>> detectionsStarted = new ConcurrentHashMap<>();
	private final Map<UUID, Long> lastPlayerAlert = new HashMap<>();
	private final Map<UUID, ScheduledFuture<?>> kickSchedule = new ConcurrentHashMap<>();
	private final Set<UUID> blockedInteractions = ConcurrentHashMap.newKeySet();
	private final Map<UUID, Location> autoClickerLastLocation = new ConcurrentHashMap<>();

	private final static Map<UUID, ScheduledFuture<?>> autoClickerBypass = new ConcurrentHashMap<>();
	private final int minDetectionsToProcess = 100;
	private final int minDetectionsToMessage = 20;

	@Setter	@Getter
	private static boolean autoClickerFromProtocolLib;

	/**
     *  Zadanie wykrywania mechanizmów
     */
    @Getter
    private BukkitTask AFKMachineDetectionTask;

	private void activate(Player player, AFKPlayer.StopReason reason){
		activate(player, reason, false);
	}

	/**
	 * Ustawia flagę aktywności gracza
	 * @param player Gracz
	 */
	private void activate(Player player, AFKPlayer.StopReason reason, boolean ignoreInactive){
		UUID uuid = player.getUniqueId();
		// Reset po ruchu
		clickTimes.remove(uuid);
		blockedInteractions.remove(uuid);

		ScheduledFuture<?> future = clickTimeouts.remove(uuid);
		if (future != null) future.cancel(false);
		AFKPlayer.get(player.getUniqueId()).interact(reason, ignoreInactive);
	}

	/**
	 * Dołączenie gracza na serwer
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		if (plugin.altsManager.hasAlts(player)) player.setAltsDetectTime(System.currentTimeMillis());
		plugin.altsManager.add(player);
		player.setInactive(false);
		activate(e.getPlayer(), AFKPlayer.StopReason.NONE);
	}

	/**
	 * Gracz opuszcza serwer
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		UUID uuid = e.getPlayer().getUniqueId();
		clickTimes.remove(uuid);
		ScheduledFuture<?> future = clickTimeouts.remove(uuid);
		if (future != null) future.cancel(false);
		player.setAltsDetectTime(0L);
		player.setAltsDetected(false);
		plugin.altsManager.remove(player);
		player.unload();
	}

	/**
	 * Gracz pisze na czacie
	 */
	@EventHandler
	public void onChat(AsyncChatEvent e){
		if (plugin.configuration.afk.end.detectors.chat.use || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			Bukkit.getScheduler().runTask(plugin, () -> activate(e.getPlayer(), AFKPlayer.StopReason.CHAT, true));
	}

	/**
	 * Gracz wpisuje komendę
	 */
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e){
		if (plugin.configuration.afk.end.detectors.chat.use || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK()){
			if (e.getMessage().startsWith("/"+plugin.configuration.commands.afk.label)) return;
			for (String command : plugin.configuration.afk.end.detectors.chat.blacklistCommands){
				if (e.getMessage().startsWith("/"+command))
					return;
			}
			activate(e.getPlayer(), AFKPlayer.StopReason.COMMAND, true);
		}
	}

	/**
	 * Gracz się porusza
	 */
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		if (player.isTeleporting()) return;
		MoveStorage movement = new MoveStorage(e);
		if (plugin.configuration.afk.end.detectors.move.use || !player.isAFK()){
			double moveRange = plugin.configuration.afk.end.detectors.move.distance;
			double rotateRange = plugin.configuration.afk.end.detectors.rotate.angle;
			monitor.logAndCheckMovement(e.getPlayer().getUniqueId(), movement, moveRange, rotateRange);
		}

		if ((movement.isLooking && (plugin.configuration.afk.end.detectors.rotate.use || !player.isAFK())) || (movement.isMoving && (plugin.configuration.afk.end.detectors.move.use || !player.isAFK()))) {
			activate(e.getPlayer(), AFKPlayer.StopReason.MOVE);
		}

	}

	/**
	 * Gracz zadaje obrażenia
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		if (e.getDamager() instanceof Player player) {
			if (!plugin.configuration.afk.end.detectors.interact.autoClicker.use)
				activate(player, AFKPlayer.StopReason.MAKE_DAMAGE);
			else if (blockedInteractions.contains(player.getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	/**
	 * Gracz otrzymuje obrażenia
	 */
	@EventHandler
	public void onPlayerGetsDamage(EntityDamageEvent e) {
		if (!(e instanceof EntityDamageByEntityEvent) && e.getEntity() instanceof Player) {
			if (e.getEntity() instanceof Player player) {
				activate(player, AFKPlayer.StopReason.TAKE_DAMAGE);
			}
		}
	}

	/**
	 * Gracz podejmuje interakcję
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (plugin.configuration.afk.end.detectors.interact.use || !AFKPlayer.get(event.getPlayer().getUniqueId()).isAFK() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (!plugin.configuration.afk.end.detectors.interact.autoClicker.use) {
				activate(event.getPlayer(), AFKPlayer.StopReason.INTERACT);
			}
			else if (blockedInteractions.contains(event.getPlayer().getUniqueId()))
				event.setCancelled(true);
		}
	}

	/**
	 * Gracz klika w ekwipunek
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player p)
		{
			if (plugin.configuration.afk.end.detectors.interact.use || !AFKPlayer.get(p.getUniqueId()).isAFK())
				activate(p, AFKPlayer.StopReason.INVENTORY_CLICK, true);
			else if (blockedInteractions.contains(p.getUniqueId()))
				e.setCancelled(true);
		}
	}

	/**
	 * Gracz stawia blok
	 */
	@EventHandler
	public void onPlayerBlockPlace(BlockPlaceEvent e){
		if (plugin.configuration.afk.end.detectors.block.place || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.BLOCK_PLACE);
	}

	/**
	 * Gracz niszczy blok
	 */
	@EventHandler
	public void onPlayerBlockBreak(BlockBreakEvent e){
		Player player = e.getPlayer();
		AFKPlayer afkPlayer = AFKPlayer.get(player.getUniqueId());

		if (plugin.configuration.afk.end.detectors.interact.autoClicker.use) {
			if (!autoClickerBypass.containsKey(e.getPlayer().getUniqueId()))
				if (blockedInteractions.contains(e.getPlayer().getUniqueId())) {
					e.setCancelled(true);
					ChatHelper.console("Action <gold>Block break</gold> cancelled due to autoClicker!");
					return;
				}
			setAutoClickerBypass(e.getPlayer());
		}

		if (!plugin.configuration.afk.end.detectors.block.destroy && !afkPlayer.isAFK())
			activate(player, AFKPlayer.StopReason.BLOCK_DESTROY);

		if (plugin.configuration.afk.end.detectors.block.destroy)
			activate(player, AFKPlayer.StopReason.BLOCK_DESTROY);
	}

	/**
	 * Gracz próbuje zniszczyć blok
	 */
	@EventHandler
	public void onPlayerBlockDamage(BlockDamageEvent e){
		Player player = e.getPlayer();
		AFKPlayer afkPlayer = AFKPlayer.get(player.getUniqueId());

		if (plugin.configuration.afk.end.detectors.interact.autoClicker.use) {
			if (!autoClickerBypass.containsKey(e.getPlayer().getUniqueId()))
				if (blockedInteractions.contains(e.getPlayer().getUniqueId())) {
					e.setCancelled(true);
					return;
				}
			setAutoClickerBypass(e.getPlayer());
		}

		if (!plugin.configuration.afk.end.detectors.block.destroy && !afkPlayer.isAFK())
			activate(player, AFKPlayer.StopReason.BLOCK_DAMAGE);

		if (plugin.configuration.afk.end.detectors.block.destroy)
			activate(player, AFKPlayer.StopReason.BLOCK_DAMAGE);
	}

	@EventHandler
	public void onSwing(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			if (plugin.configuration.afk.end.detectors.interact.autoClicker.use)
				if (!isAutoClickerFromProtocolLib())
					checkAutoClicker(event.getPlayer(), AFKPlayer.StopReason.HAND_MOVE);
		}
	}

	public void onHandAnimation(Player player) {
		if (plugin.configuration.afk.end.detectors.interact.autoClicker.use) {
			if (!autoClickerBypass.containsKey(player.getUniqueId()))
				checkAutoClicker(player, AFKPlayer.StopReason.CLICK);
		}
	}

	/**
	 * Gracz klika
	 */
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent e){
		if (plugin.configuration.afk.end.detectors.interact.use || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK()) {
			if (!plugin.configuration.afk.end.detectors.interact.autoClicker.use)
				activate(e.getPlayer(), AFKPlayer.StopReason.CLICK);
			else if (blockedInteractions.contains(e.getPlayer().getUniqueId()))
				e.setCancelled(true);
		}

	}

	public void checkAutoClicker(Player player, AFKPlayer.StopReason reason) {
		UUID uuid = player.getUniqueId();
		long now = System.currentTimeMillis();

		if (autoClickerBypass.containsKey(uuid)) return;

		// Zapisanie kliknięcia
		Deque<Long> times = clickTimes.computeIfAbsent(uuid, k -> new ArrayDeque<>());
		times.addLast(now);
		if (times.size() > 20) times.removeFirst();

		times.removeIf(i -> i < System.currentTimeMillis() - plugin.configuration.afk.end.detectors.interact.autoClicker.checkTime);

		// Anuluj poprzedni timeout jeśli istniał
		ScheduledFuture<?> oldTimeout = clickTimeouts.remove(uuid);
		ScheduledFuture<?> oldDetections = detectionsStarted.get(uuid);
		if (oldTimeout != null) oldTimeout.cancel(false);
		if (oldDetections != null) oldDetections.cancel(false);

		// Sprawdź podejrzane kliknięcia
		boolean suspicious = false;
		if (times.size() >= 6) {
			List<Long> intervals = new ArrayList<>();
			Iterator<Long> it = times.iterator();
			long prev = it.next();
			while (it.hasNext()) {
				long curr = it.next();
				intervals.add(curr - prev);
				prev = curr;
			}
			long avg = (long) intervals.stream().mapToLong(Long::longValue).average().orElse(0);
			long deviationSum = intervals.stream().mapToLong(i -> Math.abs(i - avg)).sum();
			long avgDeviation = deviationSum / intervals.size();
			long totalTime = times.getLast() - times.getFirst();
			int currentDetectionCount = getDetections(player);

			if (avg < 60) return;
			if (plugin.configuration.global.debug)
				ChatHelper.console("checkAutoClicker={reason=<gold>"+reason+"</gold>, clicks=<gold>"+currentDetectionCount+"</gold>, hits=<gold>"+times.size()+"</gold>, avg=<gold>"+avg+"</gold>, avgDeviation=<gold>"+avgDeviation+"</gold>/"+plugin.configuration.afk.end.detectors.interact.autoClicker.avgDeviation+", totalTime=<gold>"+totalTime+"</gold>/"+plugin.configuration.afk.end.detectors.interact.autoClicker.checkTime+"}");
			if (avgDeviation < plugin.configuration.afk.end.detectors.interact.autoClicker.avgDeviation && totalTime < plugin.configuration.afk.end.detectors.interact.autoClicker.checkTime) {
				suspicious = true;

				Location location = player.getLocation();
				Location loc = autoClickerLastLocation.getOrDefault(uuid, location);
				if (loc.getYaw() != location.getYaw() && loc.getPitch() != location.getPitch()) {
					resetAutoClicker(player);
					autoClickerLastLocation.put(uuid, player.getLocation());
					return;
				}

				autoClickerLastLocation.put(uuid, player.getLocation());


				// Wyświetlenie ostrzeżenia
				if (currentDetectionCount == minDetectionsToMessage) {
					showAutoClickerMessage(player, avg);
				}

				if (currentDetectionCount < minDetectionsToProcess)
					return;

				if (plugin.configuration.afk.end.detectors.interact.autoClicker.log.use) {
					if ((!plugin.configuration.afk.end.detectors.interact.autoClicker.log.onlyOnce || plugin.configuration.global.debug) || currentDetectionCount == minDetectionsToProcess) {
						String message = plugin.configuration.afk.end.detectors.interact.autoClicker.log.message;
						Map<String, String> replacements = new HashMap<>();
						replacements.put("%player%", player.getName());
						replacements.put("%deviation%", String.valueOf(avgDeviation));
						replacements.put("%intervals%", String.valueOf(intervals));
						replacements.put("%location%", player.getLocation().getBlockX() + "x" + player.getLocation().getBlockY() + "y" + player.getLocation().getBlockZ() + "z");
						replacements.put("%average%", String.valueOf(avg));
						replacements.put("%reason%", reason.name());
						replacements.put("%count%", String.valueOf(currentDetectionCount - minDetectionsToProcess));

						ChatHelper.console(message, replacements);
					}
				}

				if (plugin.configuration.afk.end.detectors.interact.autoClicker.cancelEvent) {
					if (plugin.configuration.global.debug && !blockedInteractions.contains(uuid))
						ChatHelper.console("<red>Disable interactions for player "+player.getName());
					blockedInteractions.add(uuid);
				}

				if (plugin.configuration.afk.end.detectors.interact.autoClicker.kick.use && currentDetectionCount > minDetectionsToProcess*2) {
					if (!hasPlayerPermission(player, plugin.configuration.afk.end.detectors.interact.autoClicker.kick.bypass.permissions)) {
						if (!kickSchedule.containsKey(uuid)) {
							if (plugin.configuration.global.debug)
								ChatHelper.console("<red>Schedule kick player "+player.getName());
							ScheduledFuture<?> kickScheduled = scheduler.schedule(() -> {
								resetAutoClicker(player);
								kickSchedule.remove(uuid);
								Bukkit.getScheduler().runTask(plugin, () -> player.kick(ChatHelper.minimessage(plugin.configuration.afk.end.detectors.interact.autoClicker.kick.reason), PlayerKickEvent.Cause.ILLEGAL_ACTION));
							}, plugin.configuration.afk.end.detectors.interact.autoClicker.kick.delay.toSeconds(), TimeUnit.SECONDS);

							kickSchedule.put(uuid, kickScheduled);
						}
					}
				}
			}
		}

		// Jeśli niepodejrzane - zaplanuj aktywację za 3 sekundy
		if (!suspicious) {
			ScheduledFuture<?> future = scheduler.schedule(() -> {
				resetAutoClicker(player);
				Bukkit.getScheduler().runTask(plugin, () -> {
					activate(player, reason, true);
				});
			}, 3, TimeUnit.SECONDS);
			clickTimeouts.put(uuid, future);
		}

		// Ogranicz wykrycia w czasie
		ScheduledFuture<?> future = scheduler.schedule(() -> {
			resetAutoClicker(player);
		}, 3, TimeUnit.SECONDS);
		detectionsStarted.put(uuid, future);
	}

	private int getDetections(Player player) {
		int detected = this.detected.getOrDefault(player.getUniqueId(), 0);
		this.detected.put(player.getUniqueId(), detected + 1);
		return detected + 1;
	}

	private void showAutoClickerMessage(Player player, long avg) {
		UUID uuid = player.getUniqueId();
		if (lastPlayerAlert.containsKey(uuid))
			if (lastPlayerAlert.get(uuid) < System.currentTimeMillis() - 30000L)
				lastPlayerAlert.remove(uuid);
		if (!lastPlayerAlert.containsKey(uuid) && plugin.configuration.afk.end.detectors.interact.autoClicker.alert && plugin.configuration.afk.end.detectors.interact.autoClicker.alertMessage != null && !plugin.configuration.afk.end.detectors.interact.autoClicker.alertMessage.isEmpty()) {
			lastPlayerAlert.put(uuid, System.currentTimeMillis());
			Map<String, String> replacements = new HashMap<>();
			replacements.put("%player%", player.getName());
			replacements.put("%average%", String.valueOf(avg));
			ChatHelper.message(player, plugin.configuration.afk.end.detectors.interact.autoClicker.alertMessage, replacements);
		}
	}

	private void resetAutoClicker(Player player) {
		UUID uuid = player.getUniqueId();
		clickTimes.remove(uuid);
		ScheduledFuture<?> oldTimeout = clickTimeouts.remove(uuid);
		ScheduledFuture<?> oldDetections = detectionsStarted.get(uuid);
		if (oldTimeout != null) oldTimeout.cancel(false);
		if (oldDetections != null) oldDetections.cancel(false);
		ScheduledFuture<?> kickScheduled = kickSchedule.remove(uuid);
		if (plugin.configuration.global.debug && blockedInteractions.contains(uuid))
			ChatHelper.console("<green>Enable interactions for player "+player.getName());
		blockedInteractions.remove(uuid);
		if (kickScheduled != null) kickScheduled.cancel(false);
		lastPlayerAlert.remove(uuid);


		detected.remove(uuid);
	}

	/**
	 * Gracz je
	 */
	@EventHandler
	public void onConsume(PlayerItemConsumeEvent e){
		if (plugin.configuration.afk.end.detectors.consume || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.CONSUME);
	}

	/**
	 * Gracz kładzie się do łóżka
	 */
	@EventHandler
	public void onBed(PlayerBedEnterEvent e){
		if (plugin.configuration.afk.end.detectors.interact.use || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.BED);
	}

	/**
	 * Gracz zmienia świat
	 */
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		if (player.isTeleporting()) return;
		if (plugin.configuration.afk.end.detectors.changeWorld || !player.isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.CHANGE_WORLD);
	}

	/**
	 * Gracz edytuje książkę
	 */
	@EventHandler
	public void onEditBook(PlayerEditBookEvent e){
		if (plugin.configuration.afk.end.detectors.book || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.EDIT_BOOK);
	}

	/**
	 * Gracz wyrzuca przedmiot
	 */
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e){
		if (plugin.configuration.afk.end.detectors.item.drop || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.DROP_ITEM);
	}

	/**
	 * Gracz podnosi przedmiot
	 */
	@EventHandler
	public void onPickupItem(PlayerPickupArrowEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		if (player.isTeleporting()) return;
		if (plugin.configuration.afk.end.detectors.item.pickup || !player.isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.PICKUP_ITEM);
	}

	/**
	 * Gracz niszczy przedmiot
	 */
	@EventHandler
	public void onItemBreak(PlayerItemBreakEvent e){
		if (plugin.configuration.afk.end.detectors.item.destroy || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.ITEM_BREAK);
	}

	/**
	 * Gracz strzyże owcę
	 */
	@EventHandler
	public void onShear(PlayerShearEntityEvent e){
		if (plugin.configuration.afk.end.detectors.shear || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.SHEAR);
	}

	/**
	 * Gracz biegnie
	 */
	@EventHandler
	public void onSprint(PlayerToggleSprintEvent e){
		if (plugin.configuration.afk.end.detectors.sprint || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.SPRINT);
	}

	/**
	 * Gracz kuca
	 */
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e){
		if (plugin.configuration.afk.end.detectors.sneak || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.SNEAK);
	}

	/**
	 * Gracz opróżnia wiaderko
	 */
	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e){
		if (plugin.configuration.afk.end.detectors.bucket.empty || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.BUCKET_EMPTY);
	}

	/**
	 * Gracz napełnia wiaderko
	 */
	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent e){
		if (plugin.configuration.afk.end.detectors.bucket.fill || !AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK())
			activate(e.getPlayer(), AFKPlayer.StopReason.BUCKET_FILL);
	}

	/**
	 * Gracz się teleportuje
	 */
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e){
		AFKPlayer player = AFKPlayer.get(e.getPlayer().getUniqueId());
		if (plugin.configuration.afk.end.detectors.teleport || !player.isAFK())
			if (!player.isTeleporting())
				activate(e.getPlayer(), AFKPlayer.StopReason.TELEPORT);
	}

	/*
	Ochrona gracza AFK
	 */

	/**
	 * Ochrona gracza AFK przed poruszaniem
	 * UWAGA: W niektórych przypadkach może powodować
	 * zamrożenie gracza, próbującego przesuwać
	 */
	@EventHandler
	public void onPlayerMoveProtect(PlayerMoveEvent e){
		if (!AFKPlayer.get(e.getPlayer().getUniqueId()).isAFK()) return;
		MoveStorage movement = new MoveStorage(e);
		boolean isBumpProtected = isMovementCausedByEntityBump(e.getPlayer());
		if (isBumpProtected && movement.isMoving && !movement.isLooking){
			if (movement.to.getY() <= movement.from.getY()) {
				e.setCancelled(true);
				if (plugin.configuration.global.debug)
					ChatHelper.console("<gold>Prevent to move player "+AFKPlayer.get(e.getPlayer().getUniqueId())+", because is AFK</gold>");
			}
		}
	}

	/**
	 * Ochrona gracza AFK przed otrzymywaniem obrażeń
	 */
	@EventHandler
	public void onEntityDamageProtection(EntityDamageByEntityEvent e){
		boolean damageCausedByPlayer = e.getDamager() instanceof Player;
		if(e.getDamager() instanceof Arrow)
			damageCausedByPlayer = ((Arrow) e.getDamager()).getShooter() instanceof Player;

		if (e.getEntity() instanceof Player player && AFKPlayer.get(player.getUniqueId()).isAFK()){
			if (plugin.configuration.afk.protect.hurt.players && damageCausedByPlayer) {
				e.setCancelled(true);
				if (plugin.configuration.global.debug)
					ChatHelper.console("<gold>Prevent damage "+player.getName()+" from "+damageCausedByPlayer+"</gold>");
			}
			if (plugin.configuration.afk.protect.hurt.others && !damageCausedByPlayer) {
				e.setCancelled(true);
				if (plugin.configuration.global.debug)
					ChatHelper.console("<gold>Prevent damage "+player.getName()+" from "+damageCausedByPlayer+"</gold>");
			}
		}
	}

	/**
	 * Gracz otrzymuje obrażenia
	 */
	@EventHandler
	public void onPlayerHurt(EntityDamageEvent e){
		if ((e instanceof EntityDamageByEntityEvent) || !(e.getEntity() instanceof Player))
			return;

		if (plugin.configuration.afk.protect.hurt.others && AFKPlayer.get(e.getEntity().getUniqueId()).isAFK()) {
			e.setCancelled(true);
			if (plugin.configuration.global.logToConsole || plugin.configuration.global.debug)
				ChatHelper.console("<gold>Prevent player "+e.getEntity().getName()+" hurt by "+e.getDamageSource().getCausingEntity().getName()+" ("+e.getDamageSource().getDamageType()+")</gold>");
		}
	}

	/**
	 * Ochrona przed spawnowaniem się naturalnie mobów
	 */
	@EventHandler
	public void onNaturalMonsterSpawn(CreatureSpawnEvent e){
		if (!plugin.configuration.afk.protect.mob.spawn.use) return;
		if (!(e.getEntity() instanceof Monster) || !e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))
			return;
		boolean shouldSpawn = plugin.spawnManager.shouldNaturalSpawn(e.getLocation());
		if(!shouldSpawn) e.setCancelled(true);
	}

	/**
	 * Ochrona przed spawnowaniem się mobów ze spawnera
	 */
	@EventHandler
	public void onSpawnerMonsterSpawn(SpawnerSpawnEvent e){
		if (!plugin.configuration.afk.protect.mob.spawn.use)
			return;
		if(!(e.getEntity() instanceof Monster) || e.getSpawner() == null)
			return;
		boolean shouldSpawn = plugin.spawnManager.shouldSpawnerSpawn(e.getSpawner());
		if(!shouldSpawn) e.setCancelled(true);
	}

	/**
	 * Ochrona przez targetowaniem się moba na graczu
	 */
	@EventHandler
	public void enEntityTarget(EntityTargetLivingEntityEvent e){
		if (!plugin.configuration.afk.protect.mob.target)
			return;
		if(!(e.getEntity() instanceof Monster))
			return;
		if (!(e.getTarget() instanceof Player player))
			return;
		if (!AFKPlayer.get(player.getUniqueId()).isAFK())
			return;
		e.setCancelled(true);
		if (plugin.configuration.global.debug)
			ChatHelper.console("<gold>Prevent mob "+e.getEntity().getName()+" targeting to "+e.getTarget().getName()+"</gold>");
	}

	/**
	 * Ochrona przed podnoszeniem przedmiotów podczas AFK
	 */
	@EventHandler
	public void onItemPickup(EntityPickupItemEvent e){
		if (e.getEntity() instanceof Player player)
			if (AFKPlayer.get(player.getUniqueId()).isAFK())
				if (plugin.configuration.afk.protect.pickup) {
					e.setCancelled(true);
					if (plugin.configuration.global.debug)
						ChatHelper.console("<gold>Prevent player "+player.getName()+" from pickup item "+e.getItem().getName()+"</gold>");
				}
	}

    private void startAFKMachineDetection(){
		AFKMachineDetectionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			if (plugin.configuration.afk.start.detectors.afkMachine.use)
			{
				playerLocations.clear();
				for (Player p : Bukkit.getOnlinePlayers())
					playerLocations.put(p.getUniqueId(), p.getLocation());
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
					for (UUID uuid : playerLocations.keySet())
					{
						if (Bukkit.getOfflinePlayer(uuid).isOnline())
						{
							Location savedLoc = playerLocations.get(uuid);
							Location loc = Objects.requireNonNull(Bukkit.getPlayer(uuid)).getLocation();
							boolean inactive = false;
							if (plugin.configuration.afk.start.detectors.afkMachine.use)
							{
								boolean isNotLooking = checkRotation(savedLoc, loc);
								boolean isNotMoving = checkTransform(savedLoc, loc);
								if (isNotLooking && isNotMoving)
									inactive = true;
							}
							if (inactive)
							{
								Bukkit.getScheduler().runTask(plugin, () -> {
									AFKMachineDetectEvent event = new AFKMachineDetectEvent(AFKPlayer.get(uuid));
									Bukkit.getPluginManager().callEvent(event);
								});
							}
							AFKPlayer.get(uuid).setInactive(inactive);
						}
					}
				}, 20 * 2);
			}
		}, 20 * 5, 20 * 5);
	}

	private boolean isMovementCausedByEntityBump(Player p){
		double requiredDistance = 0.5;
		if(!(plugin.configuration.afk.protect.move || plugin.configuration.afk.protect.hurt.others))
			return false;

		boolean playerAttacked = false;
		EntityDamageEvent event = p.getLastDamageCause();
		Entity damager = null;
		if (event instanceof EntityDamageByEntityEvent)
			damager = ((EntityDamageByEntityEvent) event).getDamager();

		boolean isEntityClose = false;
		for (Entity e : p.getNearbyEntities(requiredDistance, requiredDistance, requiredDistance)){
			if (e instanceof Monster || e instanceof Player){
				if (e.equals(damager))
					playerAttacked = true;
				isEntityClose = true;
				break;
			}
		}

		if (plugin.configuration.afk.protect.move && isEntityClose)
			return true;
		return plugin.configuration.afk.protect.hurt.others && (playerAttacked || isEntityClose);
	}

	private void setAutoClickerBypass(Player player) {
		ScheduledFuture<?> scheduledFuture = autoClickerBypass.remove(player.getUniqueId());
		if (scheduledFuture != null) scheduledFuture.cancel(false);
		ScheduledFuture<?> future = scheduler.schedule(() -> {
			autoClickerBypass.remove(player.getUniqueId());
			if (plugin.configuration.global.debug)
				ChatHelper.console("<red>Removed player <gold>"+player.getName()+"</gold> from bypass autoClickerDetector");
		}, 3, TimeUnit.SECONDS);
		autoClickerBypass.put(player.getUniqueId(), future);
		if (plugin.configuration.global.debug)
			ChatHelper.console("<green>Add player <gold>"+player.getName()+"</gold> to bypass autoClickerDetector");
	}

	private boolean checkRotation(Location oldLoc, Location newLoc){
		boolean yaw = oldLoc.getYaw() == newLoc.getYaw();
		boolean pitch = oldLoc.getPitch() == newLoc.getPitch();
		return yaw && pitch;
	}

	private boolean checkTransform(Location oldLoc, Location newLoc){
		double epsilon = 1;
		boolean x = Math.abs(oldLoc.getX() - newLoc.getX()) < epsilon;
		boolean y = Math.abs(oldLoc.getY() - newLoc.getY()) < epsilon;
		boolean z = Math.abs(oldLoc.getZ() - newLoc.getZ()) < epsilon;
		return x && y && z;
	}

	public AFKMagicListeners(AFKMagic plugin){
		this.plugin = plugin;
		monitor = new MoveMonitor();
		startAFKMachineDetection();
		Bukkit.getPluginManager().registerEvents(this, plugin);

	}
}