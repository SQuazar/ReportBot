package net.quazar.handler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.quazar.command.Command;
import net.quazar.registry.CommandRegistry;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler extends ListenerAdapter {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^!(\\w+)");

    private final CommandRegistry commandRegistry;

    public CommandHandler(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getChannelType() != ChannelType.TEXT && event.getChannelType() != ChannelType.PRIVATE) return;
        String message = event.getMessage().getContentRaw().trim();
        if (!message.startsWith("!")) {
            processCommandNotFound(event);
            return;
        }

        Matcher matcher;
        String command = (matcher = COMMAND_PATTERN.matcher(message)).find() ?
                matcher.group(1) : null;
        if (command == null) {
            processCommandNotFound(event);
            return;
        }

        Optional<Command> cmd = commandRegistry.findCommand(command);
        if (cmd.isPresent()) {
            String[] args = new String[0];
            String subs;
            if ((subs = message.substring(command.length() + 1).trim()).length() > 0)
                args = subs.split(" ");
            cmd.get().execute(event, args);
            return;
        }

        processCommandNotFound(event);
    }

    private void processCommandNotFound(MessageReceivedEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            String[] msg = new String[] {
                    "Чтобы отправить жалобу на игрока, нажмите ПКМ по сообщению и выберите ответить, в самом сообщении напишите: \n!report",
                    "",
                    "Чтобы отключить/включить уведомления о поданной жалобе, напишите мне сообщение:\n!notify"
            };
            event.getChannel().sendMessage(String.join("\n", msg)).queue();
        }
    }
}
