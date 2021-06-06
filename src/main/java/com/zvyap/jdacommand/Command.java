package com.zvyap.jdacommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

	private final String name;
	protected String help = "A command handler by JDACommandUtilities";
	protected String arguments = null;
	protected CommandCategory category = new CommandCategory("DEFAULT");
	protected boolean handlerGuild = true;
	protected boolean handlerPM = false;
	protected boolean adminCommand = false;
	protected boolean isHidden = false;
	protected long cooldown = 0; // ms
	protected int minimumArgs, maximumArgs = -1; //-1 means disable
	protected final List<String> aliases = new ArrayList<String>();
	protected final List<Permission> requireUserPermission = new ArrayList<Permission>(); //Permission user need to execute this commamd
	protected final List<Permission> requireSelfPermission = new ArrayList<Permission>(); //Permission bot need to execute this command
	@Deprecated protected final List<Command> children = new ArrayList<Command>(); //Not working

	private final HashMap<Long, Long> cooldownCache = new HashMap<Long, Long>();
	
	protected abstract void execute(CommandCore core, MessageReceivedEvent evt, String[] args);
	
	protected Command(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getHelp() {
		return help;
	}
	
	@Nullable
	public String getArguments() {
		return arguments;
	}

	public boolean isHandlerGuild() {
		return handlerGuild;
	}

	public boolean isHandlerPM() {
		return handlerPM;
	}

	public boolean isAdminCommand() {
		return adminCommand;
	}

	public long getCooldown() {
		return cooldown;
	}

	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}

	public List<Permission> getRequireUserPermission() {
		return new ArrayList<Permission>(requireUserPermission);
	}

	public List<Permission> getRequireSelfPermission() {
		return new ArrayList<Permission>(requireSelfPermission);
	}

	public List<Command> getChildren() {
		return new ArrayList<Command>(children);
	}

	public CommandCategory getCategory() {
		return category;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public int getMinimumArgument() {
		return minimumArgs;
	}

	public int getMaximumArgument() {
		return maximumArgs;
	}

	protected HashMap<Long, Long> getCooldownCache() {
		return cooldownCache;
	}
}
