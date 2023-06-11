package net.quazar;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonWriter;
import io.github.cdimascio.dotenv.Dotenv;
import net.quazar.config.BotConfig;
import net.quazar.repository.DisabledNotificationsRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        File disabledNotifyFile = new File("disable_notify.json");
        createDisableNotifyFile(disabledNotifyFile);
        Gson gson = new Gson();
        DisabledNotificationsRepository disabledNotificationsRepository = new DisabledNotificationsRepository(gson, disabledNotifyFile);
        disabledNotificationsRepository.load();
        startSaveTimerForNotificationsRepository(disabledNotificationsRepository);

        Dotenv env = Dotenv.load();
        BotConfig config = BotConfig.builder()
                .token(Objects.requireNonNull(env.get("BOT_TOKEN"), "BOT_TOKEN cannot be null"))
                .guildId(Long.parseLong(Objects.requireNonNull(env.get("GUILD_ID"), "GUILD_ID cannot be null")))
                .reportChannel(Long.parseLong(Objects.requireNonNull(env.get("REPORT_CHANNEL"), "REPORT_CHANNEL cannot be null")))
                .moderatorRoleId(Long.parseLong(Objects.requireNonNull(env.get("MODERATOR_ROLE_ID"), "MODERATOR_ROLE_ID cannot be null")))
                .build();
        new ReportBot(config, disabledNotificationsRepository).start();
    }

    private static void createDisableNotifyFile(File file) {
        if (file.exists()) return;
        JsonArray array = new JsonArray();
        try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            new Gson().toJson(array, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startSaveTimerForNotificationsRepository(DisabledNotificationsRepository repository) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                repository.save();
            }
        }, 1000 * 60, 1000 * 60);
    }
}
