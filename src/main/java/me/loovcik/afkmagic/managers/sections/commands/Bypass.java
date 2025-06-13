package me.loovcik.afkmagic.managers.sections.commands;

import java.util.ArrayList;
import java.util.List;

public class Bypass
{
	public String label;
	public List<String> aliases = new ArrayList<>();
	public String permission;
	public Types afk = new Types();
	public Types alts = new Types();
	public Types kick = new Types();
	public Types room = new Types();
	public Types autoClicker = new Types();
	public Types kickAutoClicker = new Types();

	public static class Types {
		public String label;
		public String permission;
	}
}