package net.quazar.handler;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.quazar.ReportBot;

@AllArgsConstructor
public class MessageReactionHandler extends ListenerAdapter {
    private static final String ACCEPTED = "accepted:" + ReportBot.Emojis.ACCEPTED;
    public static final String DENIED = "denied:" + ReportBot.Emojis.DENIED;
    public static final String DELETE = "gori_gori:" + ReportBot.Emojis.DELETE;

    private final long guildId;
    private final long moderatorRoleId;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getGuild().getIdLong() != guildId) return;
        if (event.getUser().isBot()) return;
        if (event.getMember().getRoles().stream().noneMatch(r -> r.getIdLong() == moderatorRoleId)) return;
        event.getChannel().retrieveMessageById(event.getReaction().getMessageIdLong()).queue(message -> {
            if (message == null) return;
            if (message.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong()) return;
            if (message.getEmbeds().size() == 0) return;

            MessageEmbed embed = message.getEmbeds()
                    .stream()
                    .findFirst()
                    .get();
            if (!embed.getColor().equals(ReportBot.Colors.REPORT_COLOR)) return;

            MessageEmbed edited = null;
            if (event.getEmoji().getAsReactionCode().equals(ACCEPTED))
                edited = new EmbedBuilder(embed)
                        .setColor(ReportBot.Colors.REPORT_ACCEPTED_COLOR)
                        .addField("Модератор", event.getUser().getAsMention(), false)
                        .build();
            if (event.getEmoji().getAsReactionCode().equals(DENIED))
                edited = new EmbedBuilder(embed)
                        .setColor(ReportBot.Colors.REPOST_DENIED_COLOR)
                        .addField("Модератор", event.getUser().getAsMention(), false)
                        .build();
            if (event.getEmoji().getAsReactionCode().equals(DELETE)) {
                message.delete().queue();
                return;
            }

            if (edited != null) {
                message.clearReactions().queue();
                message.editMessageEmbeds(edited).queue();
            }
        });
    }
}
