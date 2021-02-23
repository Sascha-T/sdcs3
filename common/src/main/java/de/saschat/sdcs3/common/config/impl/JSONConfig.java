package de.saschat.sdcs3.common.config.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.saschat.sdcs3.common.config.Config;
import de.saschat.sdcs3.common.config.ConfigAdaptorArguments;

import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JSONConfig extends Config {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public String get_raw(String key) {
        return MEMORY.get(key);
    }

    @Override
    public void set_raw(String key, String value) {
        MEMORY.put(key, value);
    }

    @Override
    public void save(ConfigAdaptorArguments a) {
        File config = new File(a.minecraft_root, "sdcs3.json");
        FileWriter out = null;
        try {
            out = new FileWriter(config);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HashMap<String, HashMap<String, String>> output = new HashMap<>();
        for (Map.Entry<String, String> ent : MEMORY.entrySet()) {
            String[] p = ent.getKey().split("\\.");
            if (!output.containsKey(p[0]))
                output.put(p[0], new HashMap<>());
            output.get(p[0]).put(p[1], ent.getValue());
        }
        try {
            String json = gson.toJson(output);
            out.write(json);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void load(ConfigAdaptorArguments a, boolean loadDefault) {
        File config = new File(a.minecraft_root, "sdcs3.json");
        if (!config.exists())
            if (loadDefault) {
                load_memory(DEFAULT_CONFIG);
                save(a);
            }
        try {
            MEMORY = new HashMap<>();
            HashMap<String, LinkedTreeMap<String, String>> hashMap = gson.fromJson(new FileReader(config), HashMap.class);
            for (Map.Entry<String, LinkedTreeMap<String, String>> ent : hashMap.entrySet()) {
                for (Map.Entry<String, String> entry : ent.getValue().entrySet()) {
                    MEMORY.put(ent.getKey() + "." + entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load_memory(HashMap<String, String> config) {
        MEMORY = config;
    }
}
