package me.loovcik.afkmagic.managers;

import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.dependencies.*;

public class DependenciesManager
{
	private final AFKMagic plugin;
	public Essentials essentials;
	public PlaceholderAPI placeholderAPI;
	public WorldGuard worldGuard;
	public VaultAPI vault;
	public ProtocolLib protocolLib;

	private void create(){
		essentials = new Essentials();
		placeholderAPI = new PlaceholderAPI(plugin);
		worldGuard = new WorldGuard();
		vault = new VaultAPI();
		vault.initialize();
		protocolLib = new ProtocolLib();
	}

	public DependenciesManager(AFKMagic plugin){
		this.plugin = plugin;
		create();
	}
}