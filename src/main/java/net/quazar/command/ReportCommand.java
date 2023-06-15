package net.quazar.command;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.quazar.ReportBot;
import net.quazar.repository.DisabledNotificationsRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReportCommand implements Command, CooldownCommand {
    private final long reportChanelId;
    private final long guildId;
    private final DisabledNotificationsRepository repository;

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) return false;
        if (event.getGuild().getIdLong() != guildId) return false;

        event.getMessage().delete().queue();
        TextChannel reportChannel = event.getGuild().getTextChannelById(reportChanelId);
        if (reportChannel == null) {
            event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                            privateChannel.sendMessage("[!report] Канал для репортов не найден. Приносим свои извинения").queue())
                    .queue();
            return false;
        }

        Message referencedMessage = event.getMessage().getReferencedMessage();
        if (referencedMessage == null) {
            event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                            privateChannel.sendMessage("[!report] Вы должны ссылаться на конкретное сообщение.").queue())
                    .queue();
            return false;
        }

        if (referencedMessage.getAuthor().isBot()) return false;
//        if (referencedMessage.getAuthor().getIdLong() == event.getAuthor().getIdLong()) return;

        String comment = null;
        if (args.length > 0)
            comment = String.join(" ", args);

        EmbedBuilder reportBuilder = new EmbedBuilder()
                .setTitle("Новая жалоба")
                .setAuthor(
                        event.getAuthor().getAsTag() + " | " + event.getAuthor().getIdLong(),
                        null,
                        event.getMember().getEffectiveAvatarUrl()
                )
                .addField("Нарушитель", referencedMessage.getAuthor().getAsMention(), false)
                .setColor(ReportBot.Colors.REPORT_COLOR);
        if (!referencedMessage.getContentDisplay().isEmpty())
            reportBuilder.addField("Сообщение", referencedMessage.getContentDisplay(), false);
        reportBuilder.addField("Ссылка на сообщение", referencedMessage.getJumpUrl(), false);
        if (comment != null)
            reportBuilder.addField("Комментарий", comment, false);
        MessageEmbed report = reportBuilder.build();

        reportChannel.sendMessageEmbeds(report)
                .setContent(referencedMessage.getAttachments().size() > 0 ?
                        "Вложения:\n" + String.join("\n",
                                referencedMessage.getAttachments()
                                        .stream()
                                        .map(Message.Attachment::getUrl)
                                        .collect(Collectors.toSet()))
                        : null)
                .setAllowedMentions(Collections.emptyList())
                .queue(message -> {
                    if (!repository.contains(event.getAuthor().getIdLong()))
                        event.getAuthor().openPrivateChannel().onSuccess(privateChannel ->
                                        privateChannel.sendMessage("[!report] Жалоба отправлена. Спасибо за обращение!")
                                                .queue())
                                .queue();

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
        return Duration.ofSeconds(30);
    }
}
