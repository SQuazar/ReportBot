package net.quazar.command;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.quazar.ReportBot;
import net.quazar.repository.DisabledNotificationsRepository;

import java.util.Collections;

@AllArgsConstructor
public class ReportCommand implements Command {
    private final long reportChanelId;
    private final long guildId;
    private final DisabledNotificationsRepository repository;

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) return;
        if (event.getGuild().getIdLong() != guildId) return;

        event.getMessage().delete().queue();
        TextChannel reportChannel = event.getGuild().getTextChannelById(reportChanelId);
        if (reportChannel == null) {
            event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                            privateChannel.sendMessage("[!report] Канал для репортов не найден. Приносим свои извинения").queue())
                    .queue();
            return;
        }

        Message referencedMessage = event.getMessage().getReferencedMessage();
        if (referencedMessage == null) {
            event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                            privateChannel.sendMessage("[!report] Вы должны ссылаться на конкретное сообщение.").queue())
                    .queue();
            return;
        }

        if (referencedMessage.getAuthor().isBot()) return;

        String comment = null;
        if (args.length > 0)
            comment = String.join(" ", args);

        EmbedBuilder reportBuilder = new EmbedBuilder()
                .setTitle("Новая жалоба")
                .setAuthor(
                        event.getAuthor().getAsTag() + "|" + event.getAuthor().getIdLong(),
                        null,
                        event.getMember().getEffectiveAvatarUrl()
                )
                .addField("Нарушитель", referencedMessage.getAuthor().getAsMention(), false)
                .addField("Сообщение", referencedMessage.getContentDisplay(), false)
                .addField("Ссылка на сообщение", referencedMessage.getJumpUrl(), false)
                .setColor(ReportBot.Colors.REPORT_COLOR);
        if (comment != null)
            reportBuilder.addField("Комментарий", comment, false);
        MessageEmbed report = reportBuilder.build();

        reportChannel.sendMessageEmbeds(report)
                .setAllowedMentions(Collections.emptyList())
                .queue(message -> {
                    if (!repository.contains(event.getAuthor().getIdLong()))
                        event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                                        privateChannel.sendMessage("[!report] Жалоба отправлена. Спасибо за обращение!")
                                                .queue())
                                .queue();

                    RestAction.allOf(
                            message.addReaction(Emoji.fromCustom("accepted", 1115748659737403472L, false)),
                            message.addReaction(Emoji.fromCustom("denied", 1115748674480373800L, false))
                    ).queue();
                });
    }
}
