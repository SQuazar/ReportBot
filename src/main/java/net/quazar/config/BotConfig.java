package net.quazar.config;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class BotConfig {
    @NonNull
    private String token;
    @NonNull
    private Long guildId;
    @NonNull
    private Long reportChannel;
    @NonNull
    private Long moderatorRoleId;
}
