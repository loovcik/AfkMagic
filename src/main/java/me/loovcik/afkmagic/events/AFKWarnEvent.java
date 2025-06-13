package me.loovcik.afkmagic.events;

import me.loovcik.afkmagic.models.AFKPlayer;

/**
 * Zdarzenie wywoływane, gdy gracz zostanie
 * ostrzeżony, zgodnie z ustawieniami w pliku config
 */
public class AFKWarnEvent extends AFKEvent
{
	public AFKWarnEvent(AFKPlayer player){
		super(player);
	}
}