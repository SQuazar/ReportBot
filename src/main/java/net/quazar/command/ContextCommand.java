package net.quazar.command;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public interface ContextCommand {
    boolean execute(MessageContextInteractionEvent event);
}
