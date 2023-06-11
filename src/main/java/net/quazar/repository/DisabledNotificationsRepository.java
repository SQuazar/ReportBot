package net.quazar.repository;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DisabledNotificationsRepository {
    private final Gson gson;
    private final File file;
    private final Set<Long> entities = new HashSet<>();

    public DisabledNotificationsRepository(Gson gson, File file) {
        this.gson = gson;
        this.file = file;
    }

    public boolean add(long entity) {
        return entities.add(entity);
    }

    public boolean remove(long entity) {
        return entities.remove(entity);
    }

    public boolean contains(long entity) {
        return entities.contains(entity);
    }

    public synchronized void save() {
        try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            gson.toJson(entities, new TypeToken<List<Long>>(){}.getType(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            entities.addAll(array.asList()
                    .stream()
                    .map(JsonElement::getAsLong)
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
