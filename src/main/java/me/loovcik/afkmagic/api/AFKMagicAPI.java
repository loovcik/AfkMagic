package me.loovcik.afkmagic.api;

import org.bukkit.OfflinePlayer;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AFKMagicAPI
{
	private static AFKMagic plugin;

	public AFKMagicAPI(AFKMagic plugin){
		AFKMagicAPI.plugin = plugin;
	}

	public AFKMagicAPI() {

	}

	public List<IAFKPlayer> getAfkPlayers() {
        return new ArrayList<>(AFKPlayer.getAFKPlayers());
	}
	public IAFKPlayer getPlayer(OfflinePlayer op){
		if (AFKPlayer.contains(op.getUniqueId()))
			return AFKPlayer.get(op.getUniqueId());
		else return null;
	}

	public IAFKPlayer getPlayer(UUID uuid){
		if (AFKPlayer.contains(uuid))
			return AFKPlayer.get(uuid);
		return null;
	}
}