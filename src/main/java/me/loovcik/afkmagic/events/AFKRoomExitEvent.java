package me.loovcik.afkmagic.events;

import lombok.Getter;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.time.Instant;
import java.util.Date;

/**
 * Zdarzenie wywoływane, gdy gracz wyjdzie
 * z bezpiecznej strefy
 */
@Getter
public class AFKRoomExitEvent extends AFKEvent
{
	/**
	 *  Pobiera dokładną datę zdarzenia
	 */
	private final Date date;

	public AFKRoomExitEvent(AFKPlayer player){
		super(player);
		date = Date.from(Instant.now());
	}
}