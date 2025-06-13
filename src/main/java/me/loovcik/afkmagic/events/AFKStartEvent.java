package me.loovcik.afkmagic.events;

import me.loovcik.afkmagic.models.AFKPlayer;

/**
 * Zdarzenie wywoływane, gdy gracz
 * przejdzie w stan AFK
 */
public class AFKStartEvent extends AFKEvent
{
	public AFKStartEvent(AFKPlayer player){
		super(player);
	}
}