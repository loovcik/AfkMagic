package me.loovcik.afkmagic.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.util.*;

import static me.loovcik.afkmagic.utils.Extensions.hasPlayerPermission;

/**
 * Manager zarządzający wykrywaniem multikont graczy AFK
 */
public class AltsManager implements Listener
{
	private final AFKMagic plugin;
	private final Map<String, List<UUID>> cache = new HashMap<>();

	/**
	 * Pobiera listę wszystkich aktywnych multikont
	 * @param player Gracz, którego multikonta mają zostać pobrane
	 * @return Lista powiązanych kont
	 */
	public List<AFKPlayer> getAll(AFKPlayer player){
		return getAll(player.getIp());
	}

	/**
	 * Pobiera listę wszystkich aktywnych kont, które łączą się z danego adresu IP
	 * @param ip Adres IP do określenia kont
	 * @return Lista aktywnych kont połączonych z tego adresu IP
	 */
	private List<AFKPlayer> getAll(String ip){
		try
		{
			List<AFKPlayer> result = new ArrayList<>(0);
			if (cache.isEmpty() || !cache.containsKey(ip)) return result;
			for (UUID uuid : cache.get(ip))
				result.add(AFKPlayer.get(uuid));
			return result;
		}
		catch (Exception e){
			return List.of();
		}
	}

	/**
	 * Sprawdza, czy gracz posiada aktywne multikonta
	 * @param player Gracz, który ma zostać sprawdzony
	 * @return True, jeśli istnieje chociaż jedno multikonto gracza online
	 */
	public boolean hasAlts(AFKPlayer player){
		List<AFKPlayer> all = getAll(player);
		for (AFKPlayer p : all){
			if (!p.getUniqueId().equals(player.getUniqueId()))
			{
				Player bPlayer = p.getPlayer();
				if (bPlayer != null){
					if (bPlayer.isOnline())
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sprawdza, czy gracz zalogował się jako pierwszy ze wszystkich multikont
	 * @param player Gracz, którego należy sprawdzić
	 * @return True, jeśli gracz jest pierwszym multikontem, które zalogowało się na serwerze w bieżącej sesji
	 */
	public boolean isFirst(AFKPlayer player){
		List<AFKPlayer> all = getAll(player);
		return all.indexOf(player) == 0;
	}

	/**
	 * Określa, czy gracz posiada uprawnienia, aby zostać pominiętym przy sprawdzaniu
	 * pod kątem wykorzystywania multikont
	 * @param player Gracz, którego należy sprawdzić
	 * @return True, jeśli gracz nie powinien być brany pod uwagę przy sprawdzaniu multikont
	 */
	public boolean isBypassed(AFKPlayer player){
		try
		{
			return getAll(player.getIp()).stream().anyMatch(x -> hasPlayerPermission(x.getPlayer(), plugin.configuration.alts.bypass.permissions));
		}
		catch (Exception e){
			return false;
		}
	}

	/**
	 * Dodaje gracza do grupy kont, powiązanych z jego adresem IP
	 * @param player Gracz, którego należy dołączyć
	 */
	public void add(AFKPlayer player){
		if (!cache.containsKey(player.getIp()))
			cache.put(player.getIp(), new ArrayList<>());
		if (!cache.get(player.getIp()).contains(player.getUniqueId()))
			cache.get(player.getIp()).add(player.getUniqueId());
	}

	/**
	 * Usuwa gracz z grupy kont, powiązanych z jego adresem IP
	 * @param player Gracz, którego należy usunąć
	 */
	public void remove(AFKPlayer player){
		if (!cache.containsKey(player.getIp()))
			return;
		if (!cache.get(player.getIp()).contains(player.getUniqueId()))
			return;
		cache.get(player.getIp()).remove(player.getUniqueId());
		if (cache.get(player.getIp()).isEmpty())
			cache.remove(player.getIp());
	}

	/**
	 * Przebudowanie pamięci numerów IP i powiązanych z nim aktywnych kont.
	 * Wymagane, jeśli plugin zostanie przeładowany podczas działania serwera,
	 * aby wszyscy aktywni gracze zostali dopisani do odpowiednich grup.
	 */
	private void buildCache(){
		cache.clear();
		for (Player player : Bukkit.getOnlinePlayers())
			add(AFKPlayer.get(player.getUniqueId()));
	}

	public AltsManager(AFKMagic plugin){
		this.plugin = plugin;
		buildCache();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
}