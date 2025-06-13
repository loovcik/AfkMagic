package me.loovcik.afkmagic.events;

import lombok.Getter;
import me.loovcik.afkmagic.models.AFKPlayer;

/**
 * Zdarzenie wywoływane, gdy gracz
 * wychodzi ze stanu AFK
 */
@Getter
public class AFKEndEvent extends AFKEvent
{
	/**
	 *  Pobiera czas trwania AFK (w milisekundach)
	 */
	private final Long time;

	public AFKEndEvent(AFKPlayer player, Long time){
		super(player);
		this.time = time;
	}
}