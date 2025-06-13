package me.loovcik.afkmagic.events;

import me.loovcik.afkmagic.models.AFKPlayer;

public class AFKMachineDetectEvent extends AFKEvent
{
	public AFKMachineDetectEvent(AFKPlayer player){
		super(player);
	}
}