package net.quazar;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.quazar.command.NotifyCommand;
import net.quazar.command.ReportCommand;
import net.quazar.config.BotConfig;
import net.quazar.handler.CommandHandler;
import net.quazar.handler.ContextCommandHandler;
import net.quazar.handler.MessageReactionHandler;
import net.quazar.registry.CommandRegistry;
import net.quazar.repository.DisabledNotificationsRepository;

import java.awt.*;

public class ReportBot {
    private final BotConfig config;
    private final DisabledNotificationsRepository disabledNotificationsRepository;

    public ReportBot(BotConfig config, DisabledNotificationsRepository disabledNotificationsRepository) {
        this.config = config;
        this.disabledNotificationsRepository = disabledNotificationsRepository;
    }

    public void start() {
        JDABuilder builder = JDABuilder.createLight(config.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setStatus(OnlineStatus.OFFLINE);
        JDA jda = builder.build();

        CommandRegistry commandRegistry = new CommandRegistry();
        // 1082385530576175235L
        commandRegistry.register("report", new ReportCommand(config.getReportChannel(), config.getGuildId(),
                disabledNotificationsRepository));
        commandRegistry.register("notify", new NotifyCommand(disabledNotificationsRepository));

        jda.updateCommands().addCommands(
                Commands.context(Command.Type.MESSAGE, "Пожаловаться")
                        .setGuildOnly(true)
        ).queue();

        jda.addEventListener(new CommandHandler(commandRegistry));
        jda.addEventListener(new ContextCommandHandler(config.getReportChannel(), config.getGuildId()));
        jda.addEventListener(new MessageReactionHandler(config.getGuildId(), config.getModeratorRoleId()));
    }

    public static class Colors {
        public static final Color REPORT_COLOR = new Color(19, 75, 195);
        public static final Color REPORT_ACCEPTED_COLOR = new Color(11, 231, 99);
        public static final Color REPOST_DENIED_COLOR = new Color(245, 53, 35);
    }

    public static class Emojis {
        public static final long ACCEPTED = 480443108509876224L;
        public static final long DENIED = 480443108803477517L;
    }
}
