package net.quazar.handler;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.quazar.command.*;

public class ContextCommandHandler extends ListenerAdapter {
    private final ReportContextCommand reportCommand;
    private final ReportAvatarContextCommand reportAvatarCommand;
    private final long guildId;

    public ContextCommandHandler(long reportChannel, long guildId) {
        this.reportCommand = new ReportContextCommand(reportChannel);
        this.reportAvatarCommand = new ReportAvatarContextCommand(reportChannel);
        this.guildId = guildId;
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getCommandString().equals("Пожаловаться")) {
            if (event.getGuild().getIdLong() != guildId) return;
            if (reportCommand.hasCooldown(event.getUser())) {
                event.reply("Вы отправляете жалобы слишком часто!")
                        .setEphemeral(true).queue();
                return;
            }
            if (reportCommand.execute(event))
                reportCommand.cooldown(event.getUser());
        }
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        if (event.getCommandString().equals("Пожаловаться на аватар")) {
            if (event.getGuild().getIdLong() != guildId) return;
            if (reportAvatarCommand.hasCooldown(event.getUser())) {
                event.reply("Вы отправляете жалобы слишком часто!")
                        .setEphemeral(true).queue();
                return;
            }
            if (reportAvatarCommand.execute(event))
                reportAvatarCommand.cooldown(event.getUser());
        }
    }
}
