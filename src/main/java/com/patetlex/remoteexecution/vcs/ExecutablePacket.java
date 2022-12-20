package com.patetlex.remoteexecution.vcs;

import com.google.gson.*;
import com.patetlex.displayphoenix.file.Data;
import com.patetlex.displayphoenix.util.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExecutablePacket {

    private static final Gson gson = new GsonBuilder().create();

    private File exe;

    public ExecutablePacket(File exe) {
        this.exe = exe;
    }

    public File getExecutable() {
        return exe;
    }

    public String getJson() {
        try {
            byte[] file = FileHelper.readAllBytesFromStream(new FileInputStream(this.exe));
            JsonObject obj = new JsonObject();
            obj.addProperty("identifier", this.exe.getName());
            obj.addProperty("file", gson.toJson(file));
            return gson.toJson(obj);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ExecutablePacket read(String json) {
        JsonElement element = gson.fromJson(json, JsonElement.class);
        if (element.isJsonObject()) {
            JsonObject obj = (JsonObject) element;
            if (!obj.has("identifier"))
                return null;
            String identifier = obj.get("identifier").getAsString();
            if (!obj.has("file"))
                return null;
            byte[] file = gson.fromJson(obj.get("file").getAsString(), byte[].class);
            Data.cache(null, "\\exe\\");
            return new ExecutablePacket(Data.cache(file, "\\exe\\" + identifier));
        }
        return null;
    }
}
