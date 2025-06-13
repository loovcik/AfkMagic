package me.loovcik.afkmagic.managers.sections.messages;

public class Bypass
{
	public BypassSection alts = new BypassSection();
	public BypassSection kick = new BypassSection();
	public BypassSection warn = new BypassSection();
	public BypassSection afk = new BypassSection();
	public BypassSection room = new BypassSection();
	public BypassSection autoClicker = new BypassSection();
	public BypassSection kickAutoClicker = new BypassSection();

	public static class BypassSection {
		public String enabled;
		public String disabled;
	}
}