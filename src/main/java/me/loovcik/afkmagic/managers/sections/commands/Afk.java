package me.loovcik.afkmagic.managers.sections.commands;

import java.util.ArrayList;
import java.util.List;

public class Afk
{
	public String label;
	public List<String> aliases = new ArrayList<>();
	public Permissions permissions = new Permissions();

	public static class Permissions {
		public String command;
		public String other;
	}
}