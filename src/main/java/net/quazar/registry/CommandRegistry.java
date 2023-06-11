package net.quazar.registry;

import net.quazar.command.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }

    public Optional<Command> findCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

}
