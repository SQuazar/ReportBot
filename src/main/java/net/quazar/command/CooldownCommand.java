package net.quazar.command;

import net.dv8tion.jda.api.entities.User;

import java.time.Duration;

public interface CooldownCommand {
    boolean hasCooldown(User user);

    void cooldown(User user);

    Duration getCooldown();
}
