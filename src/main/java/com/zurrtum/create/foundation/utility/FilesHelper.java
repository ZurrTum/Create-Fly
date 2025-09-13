package com.zurrtum.create.foundation.utility;

import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.zurrtum.create.Create;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FilesHelper {
    public static void createFolderIfMissing(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Path parentPath = path.getParent() == null ? path : path.getParent();
            Create.LOGGER.warn("Could not create Folder: {}", parentPath);
        }
    }

    public static String findFirstValidFilename(String name, Path folderPath, String extension) {
        int index = 0;
        String filename;
        Path filepath;
        do {
            filename = slug(name) + ((index == 0) ? "" : "_" + index) + "." + extension;
            index++;
            filepath = folderPath.resolve(filename);
        } while (Files.exists(filepath));
        return filename;
    }

    public static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("\\W+", "_");
    }

    private static JsonElement loadJson(InputStream inputStream) {
        try {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            reader.setStrictness(Strictness.LENIENT);
            JsonElement element = Streams.parse(reader);
            reader.close();
            inputStream.close();
            return element;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonElement loadJsonResource(String filepath) {
        return loadJson(ClassLoader.getSystemResourceAsStream(filepath));
    }

}
