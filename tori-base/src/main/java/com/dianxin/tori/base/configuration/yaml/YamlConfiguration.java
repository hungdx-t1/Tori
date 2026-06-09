package com.dianxin.tori.base.configuration.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class YamlConfiguration implements FileConfiguration {

    private Map<String, Object> rootMap;
    private final Yaml yaml;

    public YamlConfiguration() {
        this.rootMap = new LinkedHashMap<>(); // Dùng LinkedHashMap để giữ thứ tự key

        // Cấu hình format YAML cho đẹp (giống Bukkit)
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Dạng block (xuống dòng) thay vì [a, b]
        options.setPrettyFlow(true);
        options.setIndent(2); // Thụt đầu dòng 2 spaces

        this.yaml = new Yaml(options);
    }

    @Override
    public void load(File file) throws IOException {
        if (!file.exists()) {
            this.rootMap = new LinkedHashMap<>();
            return;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            Map<String, Object> loaded = yaml.load(in);
            this.rootMap = (loaded != null) ? loaded : new LinkedHashMap<>();
        }
    }

    @Override
    public void save(File file) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            yaml.dump(this.rootMap, writer);
        }
    }

    // --- Core Logic: Xử lý Dot Notation (a.b.c) ---

    @Override
    public Object get(String path) {
        return get(path, null);
    }

    @Override
    public Object get(String path, Object def) {
        if (path == null || path.isEmpty()) return def;

        // Nếu không có dấu chấm, lấy trực tiếp
        if (!path.contains(".")) {
            return rootMap.getOrDefault(path, def);
        }

        // Có dấu chấm, cần đào sâu vào map
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = rootMap;

        for (int i = 0; i < keys.length - 1; i++) {
            Object obj = currentMap.get(keys[i]);
            if (obj instanceof Map) {
                //noinspection unchecked
                currentMap = (Map<String, Object>) obj;
            } else {
                return def; // Đường dẫn bị gãy (gặp key không phải Map)
            }
        }

        return currentMap.getOrDefault(keys[keys.length - 1], def);
    }

    @Override
    public void set(String path, Object value) {
        if (!path.contains(".")) {
            if (value == null) {
                rootMap.remove(path);
            } else {
                rootMap.put(path, value);
            }
            return;
        }

        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = rootMap;

        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            Object obj = currentMap.get(key);

            if (obj instanceof Map) {
                //noinspection unchecked
                currentMap = (Map<String, Object>) obj;
            } else {
                // Nếu chưa có Map hoặc node đó không phải Map, tạo mới
                Map<String, Object> newMap = new LinkedHashMap<>();
                currentMap.put(key, newMap);
                currentMap = newMap;
            }
        }

        String finalKey = keys[keys.length - 1];
        if (value == null) {
            currentMap.remove(finalKey);
        } else {
            currentMap.put(finalKey, value);
        }
    }

    @Override
    public boolean contains(String path) {
        return get(path) != null;
    }

    // --- Helpers implementation ---

    @Override
    public String getString(String path) {
        Object val = get(path);
        return (val != null) ? val.toString() : null;
    }

    @Override
    public String getString(String path, String def) {
        String val = getString(path);
        return (val != null) ? val : def;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return def; // Hoặc có thể try-catch Integer.parseInt nếu val là String
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    @Override
    public double getDouble(String path, double def) {
        Object val = get(path);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return def;
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object val = get(path);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return def;
    }

    @Override
    public List<?> getList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            return (List<?>) val;
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getStringList(String path) {
        List<?> list = getList(path);
        List<String> result = new ArrayList<>();
        for (Object o : list) {
            if (o != null) result.add(o.toString());
        }
        return result;
    }
}