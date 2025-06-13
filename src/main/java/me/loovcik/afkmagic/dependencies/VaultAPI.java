package me.loovcik.afkmagic.dependencies;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.loovcik.core.ChatHelper;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("UnstableApiUsage")
public class VaultAPI
{
	private static Permission permission;

	public static Permission getPermission() { return permission; }

	public void initialize() {
		if (getServer().getPluginManager().isPluginEnabled("Vault"))
		{
			setupPermission();
			ChatHelper.console("Vault support: <green>Yes</green> (" + getServer().getPluginManager().getPlugin("Vault").getPluginMeta().getVersion() + ")");
		}
		else {
			ChatHelper.console("Vault support: <red>No</red>");
		}
	}

	private void setupPermission(){
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		permission = rsp != null ? rsp.getProvider() : null;
	}
}