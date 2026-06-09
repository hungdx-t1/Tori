//  spublic interface FileConfiguration {
//    void saveDefaultConfig();
//
//    void save(File file) throws IOException;
//
//    void save(String file) throws IOException;
//
//    void load(File file) throws IOException;
//
//    void load(String file) throws IOException;
//
//    void saveConfig();
//
//    void reloadConfig();
//
//    // Core getters
//    String getString(String path);
//    String getString(String path, String def);
//
//    int getInt(String path);
//    int getInt(String path, int def);
//
//    boolean getBoolean(String path);
//    boolean getBoolean(String path, boolean def);
//
//    double getDouble(String path);
//    double getDouble(String path, double def);
//
//    long getLong(String path);
//    long getLong(String path, long def);
//
//    java.util.List<String> getStringList(String path);
//    java.util.List<?> getList(String path);
//
//    MemorySection getSection(String path);
//
//    boolean contains(String path);
//    boolean contains(String path, boolean ignoreDefault);
//
//    boolean isSet(String path);
//
//    Set<String> getKeys(boolean deep);
//    Map<String, Object> getValues(boolean deep);
//}
package com.dianxin.tori.base.configuration.yaml;

import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Example usage:
 * <pre><code>
 *     public void loadConfigExample() {
 *     try {
 *         // 1. Tạo instance
 *         FileConfiguration config = new YamlConfiguration();
 *
 *         // 2. Xác định file
 *         File file = new File("config.yml");
 *
 *         // 3. Load (Nếu file chưa có thì tạo file mặc định - logic này bạn tự code thêm)
 *         if (!file.exists()) {
 *             file.createNewFile();
 *             // Set default values
 *             config.set("server.port", 8080);
 *             config.set("bot.token", "default_token");
 *             config.set("bot.owners", List.of("123", "456"));
 *             config.save(file);
 *         }
 *
 *         config.load(file);
 *
 *         // 4. Lấy dữ liệu (Dot notation)
 *         String token = config.getString("bot.token");
 *         int port = config.getInt("server.port");
 *         List<String> owners = config.getStringList("bot.owners");
 *
 *         System.out.println("Token: " + token);
 *
 *     } catch (IOException e) {
 *         e.printStackTrace();
 *     }
 * }
 * </code></pre>
 */
@SuppressWarnings("unused")
public interface FileConfiguration {

    /**
     * Tải dữ liệu từ file vào bộ nhớ.
     */
    void load(File file) throws IOException;

    /**
     * Lưu dữ liệu từ bộ nhớ xuống file.
     */
    void save(File file) throws IOException;

    /**
     * Lấy giá trị Object thô.
     */
    Object get(String path);
    Object get(String path, Object def);

    /**
     * Set giá trị vào path.
     */
    void set(String path, Object value);

    /**
     * Kiểm tra path có tồn tại không.
     */
    boolean contains(String path);

    // --- Các hàm tiện ích (Getters) ---

    String getString(String path);
    String getString(String path, String def);

    int getInt(String path);
    int getInt(String path, int def);

    double getDouble(String path);
    double getDouble(String path, double def);

    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);

    List<?> getList(String path);
    List<String> getStringList(String path);

    // TODO add reload config function
}