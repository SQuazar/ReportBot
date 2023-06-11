package net.quazar.command;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.quazar.repository.DisabledNotificationsRepository;

public class NotifyCommand implements Command {
    private final DisabledNotificationsRepository repository;

    public NotifyCommand(DisabledNotificationsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (event.isFromGuild()) return;
        long authorId = event.getAuthor().getIdLong();
        if (!repository.contains(authorId)) {
            repository.add(authorId);
            event.getChannel().sendMessage("Уведомления отключены").queue();
        } else {
            repository.add(authorId);
            repository.remove(authorId);
            event.getChannel().sendMessage("Уведомления включены").queue();
        }
    }
}
