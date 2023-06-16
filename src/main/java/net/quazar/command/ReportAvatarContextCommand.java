package net.quazar.command;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.quazar.ReportBot;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ReportAvatarContextCommand implements ContextCommand<User>, CooldownCommand {
    private final long reportChannel;

    @Override
    public boolean execute(GenericContextInteractionEvent<User> event) {
        event.deferReply(true).queue();

        TextChannel reportsChannel = event.getGuild().getTextChannelById(reportChannel);
        if (reportsChannel == null) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Канал для репортов не найден. Приносим свои извинения").queue();
            return false;
        }

        Member target = ((UserContextInteractionEvent) event).getTargetMember();

        if (target == null) {
            event.getHook().sendMessage("Пользователь " + event.getUser().getAsMention() + " не является участником данного сервера")
                    .queue();
            return false;
        }

        if (target.getUser().isBot()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете пожаловаться на бота").queue();
            return false;
        }
        if (target.getIdLong() == event.getUser().getIdLong()) {
            event.getHook().setEphemeral(true)
                    .sendMessage("Вы не можете отправить жалобу на самого себя").queue();
            return false;
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Новая жалоба на аватар")
                .setAuthor(
                        event.getUser().getName() + " | " + event.getUser().getIdLong(),
                        null,
                        event.getUser().getEffectiveAvatarUrl()
                )
                .addField("Нарушитель", target.getAsMention(), false)
                .setImage(target.getEffectiveAvatarUrl())
                .setColor(ReportBot.Colors.REPORT_COLOR)
                .build();
        reportsChannel.sendMessageEmbeds(embed).queue(success -> {
                    event.getHook().sendMessage("Жалоба отправлена. Спасибо за обращение!")
                            .setEphemeral(true).queue();

                    RestAction.allOf(
                            success.addReaction(Emoji.fromCustom("accepted", ReportBot.Emojis.ACCEPTED, false)),
                            success.addReaction(Emoji.fromCustom("denied", ReportBot.Emojis.DENIED, false)),
                            success.addReaction(Emoji.fromCustom("gori_gori", ReportBot.Emojis.DELETE, false))
                    ).queue();
                }
        );
        return true;
    }

    private final Map<Long, Long> cooldowns = new HashMap<>();

    @Override
    public boolean hasCooldown(User user) {
        cooldowns.values().removeIf(cooldown -> cooldown < System.currentTimeMillis());
        return cooldowns.containsKey(user.getIdLong());
    }

    @Override
    public void cooldown(User user) {
        cooldowns.put(user.getIdLong(), System.currentTimeMillis() + getCooldown().toMillis());
    }

    @Override
    public Duration getCooldown() {
        return Duration.ofMinutes(3);
    }
}
