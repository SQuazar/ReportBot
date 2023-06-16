package net.quazar.command;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;

public interface ContextCommand<C> {
    boolean execute(GenericContextInteractionEvent<C> event);
}
