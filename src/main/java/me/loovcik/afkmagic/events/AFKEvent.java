package me.loovcik.afkmagic.events;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import me.loovcik.afkmagic.api.IAFKPlayer;
import me.loovcik.afkmagic.models.AFKPlayer;

/**
 * Bazowa klasa eventów AFKMagic
 */
public class AFKEvent extends Event implements Cancellable
{
	/**
	 * Czas, którego dotyczy zdarzenie
	 */
	private final AFKPlayer player;

	/**
	 * Przechowuje powód przerwania zdarzenia
	 */
	@Getter
	private String reason;

	/**
	 * Przechowuje informację, czy zdarzenie zostało anulowane
	 */
	private boolean cancelled = false;

	/**
	 * Lista obiektów podpiętych do zdarzenia
	 */
	public static HandlerList handlers = new HandlerList();

	/**
	 * Nie używane — wymagane przez interfejs
	 */
	public static HandlerList getHandlerList(){
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers(){
		return handlers;
	}

	@Override
	public boolean isCancelled(){
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel){
		cancelled = cancel;
	}

	public void setCancelled(boolean cancel, String reason){
		cancelled = cancel;
		this.reason = reason;
	}

	public IAFKPlayer getPlayer(){
		return player;
	}

	public AFKEvent(AFKPlayer player){
		this.player = player;
	}
}