package net.quazar.command;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.quazar.ReportBot;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReportContextCommand implements ContextCommand<Message>, CooldownCommand {
    private final long reportChannel;

    @Override
    public boolean execute(GenericContextInteractionEvent<Message> event) {
        event.deferReply(true).queue();

        TextChannel reportsChannel = event.getGuild().getTextChannelById(reportChannel);
        if (reportsChannel == null) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Канал для репортов не найден. Приносим свои извинения").queue();
            return false;
        }

        Message target = event.getTarget();

        if (target.getAuthor().isBot()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете пожаловаться на бота").queue();
            return false;
        }
        if (target.getAuthor().getIdLong() == event.getUser().getIdLong()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете отправить жалобу на самого себя").queue();
            return false;
        }

        EmbedBuilder reportBuilder = new EmbedBuilder()
                .setTitle("Новая жалоба")
                .setAuthor(
                        event.getUser().getName() + " | " + event.getUser().getIdLong(),
                        null,
                        event.getMember().getEffectiveAvatarUrl()
                )
                .addField("Нарушитель", target.getAuthor().getAsMention(), false)
                .setColor(ReportBot.Colors.REPORT_COLOR);
        if (!target.getContentDisplay().isEmpty())
            reportBuilder.addField(
                    "Сообщение",
                    target.getContentDisplay().length() > 1024 ? target.getContentDisplay().substring(0, 1024) : target.getContentDisplay(),
                    false
            );
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
        return true;
    }

    private final Map<Long, Long> cooldownUsers = new HashMap<>();

    @Override
    public boolean hasCooldown(User user) {
        cooldownUsers.values().removeIf(cooldown -> cooldown < System.currentTimeMillis());
        return cooldownUsers.containsKey(user.getIdLong());
    }

    @Override
    public void cooldown(User user) {
        cooldownUsers.put(user.getIdLong(), System.currentTimeMillis() + getCooldown().toMillis());
    }

    @Override
    public Duration getCooldown() {
        return Duration.ofMinutes(3);
    }
}
