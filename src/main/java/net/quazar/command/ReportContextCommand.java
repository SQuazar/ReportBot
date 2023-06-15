package net.quazar.command;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.quazar.ReportBot;

import java.util.Collections;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReportContextCommand implements ContextCommand {
    private final long reportChannel;

    @Override
    public void execute(MessageContextInteractionEvent event) {
        event.deferReply(true).queue();

        TextChannel reportsChannel = event.getGuild().getTextChannelById(reportChannel);
        if (reportsChannel == null) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Канал для репортов не найден. Приносим свои извинения").queue();
            return;
        }

        Message target = event.getTarget();

        if (target.getAuthor().isBot()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете пожаловаться на бота").queue();
            return;
        }
        if (target.getAuthor().getIdLong() == event.getUser().getIdLong()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете отправить жалобу на самого себя").queue();
            return;
        }

        EmbedBuilder reportBuilder = new EmbedBuilder()
                .setTitle("Новая жалоба")
                .setAuthor(
                        event.getUser().getAsTag() + " | " + event.getUser().getIdLong(),
                        null,
                        event.getMember().getEffectiveAvatarUrl()
                )
                .addField("Нарушитель", target.getAuthor().getAsMention(), false)
                .setColor(ReportBot.Colors.REPORT_COLOR);
        if (!target.getContentDisplay().isEmpty())
            reportBuilder.addField("Сообщение", target.getContentDisplay(), false);
        reportBuilder.addField("Ссылка на сообщение", target.getJumpUrl(), false);

        reportsChannel.sendMessageEmbeds(reportBuilder.build())
                .setContent(target.getAttachments().size() > 0 ?
                        "Вложения:\n" + String.join("\n",
                                target.getAttachments()
                                        .stream()
                                        .map(Message.Attachment::getUrl)
                                        .collect(Collectors.toSet()))
                        : null)
                .setAllowedMentions(Collections.emptyList())
                .queue(message -> {
                    event.getHook().setEphemeral(true)
                                    .sendMessage("Жалоба отправлена. Спасибо за обращение!").queue();

                    RestAction.allOf(
                            message.addReaction(Emoji.fromCustom("accepted", ReportBot.Emojis.ACCEPTED, false)),
                            message.addReaction(Emoji.fromCustom("denied", ReportBot.Emojis.DENIED, false)),
                            message.addReaction(Emoji.fromCustom("gori_gori", ReportBot.Emojis.DELETE, false))
                    ).queue();
                });
    }
}
