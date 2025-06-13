package me.loovcik.afkmagic.events;

import lombok.Getter;
import me.loovcik.afkmagic.models.AFKPlayer;

import java.time.Instant;
import java.util.Date;

/**
 * Zdarzenie wywoływane, gdy gracz zostanie przeniesiony
 * do bezpiecznej strefy
 */
@Getter
public class AFKRoomEnterEvent extends AFKEvent
{
	/**
	 *  Pobiera dokładną datę przeniesienia
	 *  graczy do bezpiecznej strefy
	 */
	private final Date date;

	public AFKRoomEnterEvent(AFKPlayer player){
		super(player);
		date = Date.from(Instant.now());
	}
}