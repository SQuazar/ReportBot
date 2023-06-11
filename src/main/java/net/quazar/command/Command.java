package net.quazar.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SlashCommand {
    void execute(SlashCommandInteractionEvent event);
}
