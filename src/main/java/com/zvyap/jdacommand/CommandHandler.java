package com.zvyap.jdacommand;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;

public class CommandHandler extends ListenerAdapter {

	private final CommandCore core;
	private BiConsumer<MessageReceivedEvent, Permission> userNoPermissionAction;
	private BiConsumer<MessageReceivedEvent, Permission> selfNoPermissionAction;
	private BiConsumer<MessageReceivedEvent, Long> inCooldownAction; //time cooldown remain
	private Consumer<MessageReceivedEvent> adminOnlyAction;
	private BiConsumer<MessageReceivedEvent, Command> invalidArgumentAction;
	
	CommandHandler(CommandCore core, boolean useDefualtAction, BiConsumer<MessageReceivedEvent, Permission> userNoPermissionAction,
			BiConsumer<MessageReceivedEvent, Permission> selfNoPermissionAction,
			BiConsumer<MessageReceivedEvent, Long> inCooldownAction, Consumer<MessageReceivedEvent> adminOnlyAction, 
			BiConsumer<MessageReceivedEvent, Command> invalidArgumentAction) {
		this.core = core;
		if(useDefualtAction) initDefaultAction();
		this.userNoPermissionAction = userNoPermissionAction;
		this.selfNoPermissionAction = selfNoPermissionAction;
		this.inCooldownAction = inCooldownAction;
		this.adminOnlyAction = adminOnlyAction;
		this.invalidArgumentAction = invalidArgumentAction;
	}
	
	void initDefaultAction() {
		userNoPermissionAction = (evt , perm) -> {
			evt.getChannel().sendMessage("No permission - " + perm.getName()).queue();
		};
		selfNoPermissionAction = (evt , perm) -> {
			evt.getChannel().sendMessage("Bot no permission - " + perm.getName()).queue();
		};
		inCooldownAction = (evt , end) -> {
			evt.getChannel().sendMessage("Command in cooldown - " + end).queue();
		};
		adminOnlyAction = (evt) -> {
			evt.getChannel().sendMessage("Command admin only").queue();
		};
		invalidArgumentAction = (evt, cmd) -> {
			evt.getChannel().sendMessage("Invalid argument" + (cmd.getArguments() != null ? " : " + evt.getMessage().getContentRaw().split("\\s+")[0] + " " + cmd.getArguments() : "")).queue();
		};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent evt) {
		if (core.isIgnoreBot() && (evt.getAuthor().isBot() || evt.getAuthor().isSystem() || evt.isWebhookMessage())) {
			return;
		}
		String raw = evt.getMessage().getContentRaw();
		String prefix = core.getCommandPrefix();
		String[] cmdRaw = raw.split("\\s+"); //Split first word
		if (!prefix.equals("") && !raw.startsWith(prefix)) { // Check prefix
			return;
		}
		for (Command cmd : core.getRegisteredCommand()) { // Finding command user execute
			if(isEquals(cmdRaw[0], prefix + cmd.getName())) {
				handleCommand(cmd, evt, cmd.getName());
			}
			for (String alias : cmd.getAliases()) {  //Find command with alias
				if(isEquals(cmdRaw[0], prefix + alias)) {
					handleCommand(cmd, evt, alias);
				}
			}
		}
	}
	
	private boolean isEquals(String raw, String str) {
		if(core.isCaseSensitive()) {
			if (raw.equals(str)) {
				return true;
			}
		}else {
			if (raw.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}

	private void handleCommand(Command cmd, MessageReceivedEvent evt, String label) {
		if (evt.isFromType(ChannelType.TEXT)) { // Check is the command need to handler channel type user sent
			if(!cmd.isHandlerGuild()) {
				return;
			}else if(!PermissionUtil.checkPermission(evt.getGuild().getSelfMember(), Permission.MESSAGE_WRITE)) {
				return;
			}
			
		}
		if (evt.isFromType(ChannelType.PRIVATE) && !cmd.isHandlerPM()) {
			return;
		}
		if (cmd.isAdminCommand() && !core.getBotAdmins().contains(evt.getAuthor().getIdLong())) { //Check is admin only and user is admin
			if(adminOnlyAction != null) adminOnlyAction.accept(evt);
			return;
		}

		for (Permission permission : cmd.getRequireSelfPermission()) { //Check self permission
			if(!evt.getGuild().getSelfMember().hasPermission(permission)) {
				if(selfNoPermissionAction != null) selfNoPermissionAction.accept(evt, permission);
				return;
			}
		}
		for (Permission permission : cmd.getRequireUserPermission()) { //Check user permission
			if(!evt.getMember().hasPermission(permission)) {
				if(userNoPermissionAction != null) userNoPermissionAction.accept(evt, permission);
				return;
			}
		}
		if(cmd.getCooldown() > 0) { //Command has a cooldown
			long userID = evt.getAuthor().getIdLong();
			if(cmd.getCooldownCache().containsKey(userID) && System.currentTimeMillis() < cmd.getCooldownCache().get(userID)) {
				if(inCooldownAction != null) inCooldownAction.accept(evt, cmd.getCooldownCache().get(userID) - System.currentTimeMillis());
				return;
			}else cmd.getCooldownCache().put(userID, System.currentTimeMillis() + cmd.getCooldown());
		}
		
		
		String message = evt.getMessage().getContentRaw().replaceFirst(core.getCommandPrefix() + label, "");
		String[] args = message.trim().split("\\s+");
		args = args[0].equals("") ? new String[0] : args;
		if(cmd.getMinimumArgument() > -1 && args.length < cmd.getMinimumArgument()) {
			if(invalidArgumentAction != null) invalidArgumentAction.accept(evt, cmd);
			return;
		}
		if(cmd.getMaximumArgument() > -1 && args.length > cmd.getMaximumArgument()) {
			if(invalidArgumentAction != null) invalidArgumentAction.accept(evt, cmd);
			return;
		}
		cmd.execute(core, evt, args);
	}

}
