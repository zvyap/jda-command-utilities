package com.zvyap.jdacommand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class CommandCore {

	private String commandPrefix;
	private List<Command> commands;
	private List<Long> botAdmins;
	private boolean isCaseSensitive;
	private boolean ignoreBot;
	
	private CommandHandler handler;
	private List<JDA> registeredJDA;

	private CommandCore() {}
	
	@Nullable
	public Command getCommand(String name) {
		for(Command command : commands) {
			if(!isCaseSensitive) {
				if(command.getName().equalsIgnoreCase(name)) {
					return command;
				}
			}else {
				if(command.getName().equals(name)) {
					return command;
				}
			}
		}
		return null;
	}
	
	@Nullable
	public Command getCommandByAlias(String alias) {
		for(Command command : commands) {
			if(!isCaseSensitive) {
				for(String a : command.getAliases()) {
					if(a.equalsIgnoreCase(alias)) return command;
				}
			}else {
				if(command.getAliases().contains(alias)) {
					return command;
				}
			}
		}
		return null;
	}

	public String getCommandPrefix() {
		return commandPrefix;
	}

	public boolean isIgnoreBot() {
		return ignoreBot;
	}
	
	public List<Command> getRegisteredCommand(){
		return new ArrayList<Command>(commands);
	}

	public List<Long> getBotAdmins() {
		return new ArrayList<Long>(botAdmins);
	}
	
	public CommandHandler getCommandHandler() {
		return handler;
	}

	public List<JDA> getRegisteredJDA() {
		return new ArrayList<JDA>(registeredJDA);
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	
	public JDA getJDA() {
		if(registeredJDA.size() > 0 && registeredJDA.size() < 2) {
			throw new IllegalArgumentException("Registered JDA is more than 1, use getRegisteredJDA() instead of getJDA()");
		}
		return registeredJDA.get(0);
	}

	public boolean isCoreAdmin(long idLong) {
		return botAdmins.contains(idLong);
	}
	
	public static class CommandCoreBuilder {
		
		private String commandPrefix = "";
		private final List<Command> commands = new ArrayList<Command>();
		private final List<Long> botAdmins = new ArrayList<Long>();
		private boolean isCaseSensitive = false;
		private boolean ignoreBot = true;
		private boolean useDefualtAction = true;

		private BiConsumer<MessageReceivedEvent, Permission> userNoPermissionAction;
		private BiConsumer<MessageReceivedEvent, Permission> selfNoPermissionAction;
		private BiConsumer<MessageReceivedEvent, Long> inCooldownAction;
		private Consumer<MessageReceivedEvent> adminOnlyAction;
		private BiConsumer<MessageReceivedEvent, Command> invalidArgumentAction;
		
		private List<JDA> registeredJDA = new ArrayList<JDA>();

		public CommandCoreBuilder registeredJDA(JDA jda) {
			if(registeredJDA.contains(jda)) throw new IllegalArgumentException("JDA is already registed");
			registeredJDA.add(jda);
			return this;
		}
		
		public CommandCoreBuilder registerCommand(Command command) {
			if (getCommand(command.getName()) != null) throw new IllegalArgumentException(String.format("Command \"%s\" is already registed", command.getName()));
			for (String alias : command.getAliases()) { // Check alias is registered
				if (getCommandByAlias(alias) != null) throw new IllegalArgumentException(String.format("Command alias \"%s\" is already registed", alias));
			}
			commands.add(command);
			return this;
		}
		
		@Nullable
		public Command getCommand(String name) {
			for(Command command : commands) {
				if(!isCaseSensitive) {
					if(command.getName().equalsIgnoreCase(name)) {
						return command;
					}
				}else {
					if(command.getName().equals(name)) {
						return command;
					}
				}
			}
			return null;
		}
		
		@Nullable
		public Command getCommandByAlias(String alias) {
			for(Command command : commands) {
				if(!isCaseSensitive) {
					for(String a : command.getAliases()) {
						if(a.equalsIgnoreCase(alias)) return command;
					}
				}else {
					if(command.getAliases().contains(alias)) {
						return command;
					}
				}
			}
			return null;
		}

		public CommandCoreBuilder addBotAdmin(long userID) {
			botAdmins.add(userID);
			return this;
		}
		
		public CommandCoreBuilder addBotAdmin(String userID) {
			try {
				botAdmins.add(Long.parseLong(userID));
			}catch (NumberFormatException e) {
				throw new IllegalArgumentException("User id must be long");
			}
			return this;
		}

		public CommandCoreBuilder setCommandPrefix(String commandPrefix) {
			this.commandPrefix = commandPrefix;
			return this;
		}

		public CommandCoreBuilder setCaseSensitive(boolean isCaseSensitive) {
			this.isCaseSensitive = isCaseSensitive;
			return this;
		}

		public CommandCoreBuilder setIgnoreBot(boolean ignoreBot) {
			this.ignoreBot = ignoreBot;
			return this;
		}

		public CommandCoreBuilder setUserNoPermissionAction(BiConsumer<MessageReceivedEvent, Permission> userNoPermissionAction) {
			this.userNoPermissionAction = userNoPermissionAction;
			return this;
		}

		public CommandCoreBuilder setSelfNoPermissionAction(BiConsumer<MessageReceivedEvent, Permission> selfNoPermissionAction) {
			this.selfNoPermissionAction = selfNoPermissionAction;
			return this;
		}

		public CommandCoreBuilder setInCooldownAction(BiConsumer<MessageReceivedEvent, Long> inCooldownAction) {
			this.inCooldownAction = inCooldownAction;
			return this;
		}

		public CommandCoreBuilder setAdminOnlyAction(Consumer<MessageReceivedEvent> adminOnlyAction) {
			this.adminOnlyAction = adminOnlyAction;
			return this;
		}

		public void setInvalidArgumentAction(BiConsumer<MessageReceivedEvent, Command> invalidArgumentAction) {
			this.invalidArgumentAction = invalidArgumentAction;
		}

		public void setUseDefualtAction(boolean useDefualtAction) {
			this.useDefualtAction = useDefualtAction;
		}

		public CommandCore build() {
			CommandCore core = new CommandCore();
			core.botAdmins = this.botAdmins;
			core.commandPrefix = this.commandPrefix;
			core.commands = this.commands;
			core.ignoreBot = this.ignoreBot;
			core.isCaseSensitive = this.isCaseSensitive;

			core.handler = new CommandHandler(core, this.useDefualtAction, userNoPermissionAction, selfNoPermissionAction, inCooldownAction, adminOnlyAction, invalidArgumentAction);
			core.registeredJDA = this.registeredJDA;
			for (JDA jda : registeredJDA) {
				jda.addEventListener(core.handler);
			}
			return core;
		}
	}
}
