package me.loovcik.afkmagic.dependencies;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.afkmagic.AFKMagicListeners;
import me.loovcik.afkmagic.models.AFKPlayer;
import me.loovcik.core.ChatHelper;

@SuppressWarnings("UnstableApiUsage")
public class ProtocolLib {
	private AFKMagic plugin;

	@Getter
	private boolean available;

	public ProtocolLib() {
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
			available = true;
			ChatHelper.console("ProtocolLib support: <green>Yes</green> ("+Bukkit.getPluginManager().getPlugin("ProtocolLib").getPluginMeta().getVersion());
			ProtocolLibrary.getProtocolManager().addPacketListener(new AutoClickerDetector(AFKMagic.getInstance()));
			AFKMagicListeners.setAutoClickerFromProtocolLib(true);
		}
		else {
			ChatHelper.console("ProtocolLib support: <red>No</red>");
			AFKMagicListeners.setAutoClickerFromProtocolLib(false);
		}
	}

	public static class AutoClickerDetector extends PacketAdapter {
		private AFKMagic plugin;

		@Override
		public void onPacketReceiving(PacketEvent event) {
			plugin.listeners.onHandAnimation(event.getPlayer());
		}

		public AutoClickerDetector(AFKMagic plugin) {
			super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION);
			this.plugin = plugin;
			ChatHelper.console("ProtocolLib AutoClicker detector created");
		}
	}
}