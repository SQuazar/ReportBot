package net.quazar.handler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.quazar.registry.CommandRegistry;

public class SlashCommandHandler extends ListenerAdapter {
    private final CommandRegistry commandRegistry;

    public SlashCommandHandler(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        commandRegistry.findCommand(event.getName())
                .ifPresent(command -> command.execute(event));
    }
}
