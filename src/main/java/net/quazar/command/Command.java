package net.quazar.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface Command {
    boolean execute(MessageReceivedEvent event, String[] args);
}
