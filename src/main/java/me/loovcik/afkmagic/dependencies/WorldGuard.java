package me.loovcik.afkmagic.dependencies;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class WorldGuard
{
	private com.sk89q.worldguard.WorldGuard hook;

	public boolean isEnabled() { return hook != null; }

	public com.sk89q.worldguard.WorldGuard get() { return hook; }

	public boolean isOnRegion(AFKPlayer player, String... regions){
		if (!isEnabled() || player == null || regions.length == 0) return false;
		List<ProtectedRegion> onRegions = getRegions(player);

		if (onRegions == null || onRegions.isEmpty()) return false;
		for (ProtectedRegion onRegion : onRegions){
			for (String region : regions){
				if (onRegion.getId().equalsIgnoreCase(region))
					return true;
			}
		}
		return false;
	}

	public List<ProtectedRegion> getRegions(AFKPlayer player){
		if (Bukkit.getOfflinePlayer(player.getUniqueId()).isOnline())
		{
			Location location = BukkitAdapter.adapt(player.getPlayer().getLocation());
			RegionContainer container = hook.getPlatform().getRegionContainer();
			RegionQuery query = container.createQuery();
			ApplicableRegionSet set = query.getApplicableRegions(location);
			if (set.size() != 0)
			{
				List<ProtectedRegion> regions = new ArrayList<>();
				for (ProtectedRegion region : set)
				{
					regions.add(region);
				}
				return regions;
			}
		}
		return null;
	}

	public WorldGuard(){
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().isPluginEnabled("WorldGuard")){
			hook = com.sk89q.worldguard.WorldGuard.getInstance();
			ChatHelper.console("WorldGuard support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("WorldGuard").getPluginMeta().getVersion() + ")");
		}
		else {
			ChatHelper.console("WorldGuard support: <red>No</red>");
		}
	}
}