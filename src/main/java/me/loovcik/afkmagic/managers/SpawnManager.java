package me.loovcik.afkmagic.managers;

import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.io.IOException;
import java.util.*;

/**
 * Menedżer zarządzający spawnowaniem mobów
 * w pobliżu graczy AFK
 */
public class SpawnManager
{

	private final AFKMagic plugin;

	/** Zasięg spawnowania mobów **/
	private int spawnRange;

	/**
	 * Sprawdza, czy moby powinny być spawnowane
	 * @param loc Lokalizacja, względem której należy sprawdzić możliwość
	 *            spawnowania się mobów
	 * @return True, jeśli moby powinny się spawnować
	 */
	public boolean shouldNaturalSpawn(Location loc){
		List<Player> playersInRange = new ArrayList<>();
		int chunkX = loc.getChunk().getX();
		int maxX = chunkX + spawnRange;
		int minX = chunkX - spawnRange;
		int chunkZ = loc.getChunk().getZ();
		int maxZ = chunkZ + spawnRange;
		int minZ = chunkZ - spawnRange;
		for (Player p : Bukkit.getOnlinePlayers()){
			if(p.getGameMode() == GameMode.SPECTATOR)
				continue;
			int playerChunkX = p.getLocation().getChunk().getX();
			if (playerChunkX < maxX && playerChunkX > minX){
				int playerChunkZ = p.getLocation().getChunk().getZ();
				if (playerChunkZ < maxZ && playerChunkZ > minZ)
					playersInRange.add(p);
			}
		}

		if(playersInRange.isEmpty())
			return true;

		for (Player p : playersInRange){
			if (!AFKPlayer.get(p.getUniqueId()).isAFK())
				return true;
		}
		return false;
	}

	/**
	 * Sprawdza, czy moby powinny być spawnowane ze spawnerów
	 * @param spawner Spawner, którego dotyczy sprawdzanie
	 */
	public boolean shouldSpawnerSpawn(CreatureSpawner spawner){
		double range = 16;
		List<Player> players = Objects.requireNonNull(spawner.getLocation().getWorld()).getPlayers();
		players.removeIf(p -> p.getGameMode().equals(GameMode.SPECTATOR));

		Location centerOfSpawner = spawner.getLocation().add(0.5, -0.5, 0.5);
		boolean playerInRange = false;
		boolean playerAFK = false;

		for (Player p : players){
			if(p.getLocation().distance(centerOfSpawner) > range)
				continue;
			playerInRange = true;
			if (AFKPlayer.get(p.getUniqueId()).isAFK())
				if (plugin.configuration.afk.protect.mob.spawn.delay.isNegative())
					playerAFK = true;
				else {
					playerAFK = AFKPlayer.get(p.getUniqueId()).getCurrentAfkTime() >= plugin.configuration.afk.protect.mob.spawn.delay.toMilliseconds();
				}
			else {
				playerAFK = false;
				break;
			}
		}
		if (playerInRange && !playerAFK)
			return true;
		return !playerInRange;
	}

	public SpawnManager(AFKMagic plugin){
		this.plugin = plugin;
		try
		{
			YamlConfiguration spigotConfig = new YamlConfiguration();
			spigotConfig.load("spigot.yml");
			spawnRange = Objects.requireNonNull(spigotConfig.getConfigurationSection("world-settings")).getConfigurationSection("default").getInt("mob-spawn-range", 8);
		}
		catch (IOException | InvalidConfigurationException e){
			spawnRange = 8;
		}
	}
}