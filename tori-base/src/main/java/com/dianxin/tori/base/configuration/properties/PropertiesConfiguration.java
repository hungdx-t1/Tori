package com.dianxin.tori.base.configuration.properties;

import org.jetbrains.annotations.ApiStatus;

import java.io.File;

/**
 * Example code:
 * <p>
 * server.port=8080
 * <p>
 * bot.name=AjaxBot
 * <p>
 * # Dùng dấu | làm splitter
 * <p>
 * bot.owners=123456|987654|112233
 * <p>
 * debug.mode=true
 *
 * <pre><code>
 * public void testProperties() {
 *     File file = new File("app.properties");
 *
 *     // Khởi tạo với splitter là "|"
 *     EmbeddedPropertiesConfiguration config = PropertiesConfiguration.of(file, "|");
 *
 *     // 1. Lấy String/Int/Boolean
 *     String name = config.getString("bot.name"); // "AjaxBot"
 *     int port = config.getInt("server.port");    // 8080
 *     boolean debug = config.getBoolean("debug.mode"); // true
 *
 *     // 2. Lấy List (Splitter hoạt động ở đây)
 *     List<String> owners = config.getStringList("bot.owners");
 *     // Kết quả: List chứa ["123456", "987654", "112233"]
 *
 *     // 3. Set List mới và lưu lại
 *     List<String> newAdmins = List.of("Mino", "Dianxin");
 *     config.set("bot.admins", newAdmins); // Sẽ lưu thành: bot.admins=Mino|Dianxin
 *
 *     try {
 *         config.save(file);
 *     } catch (Exception e) {
 *         e.printStackTrace();
 *     }
 * }
 * </code></pre>
 */
// todo
@ApiStatus.Experimental
@SuppressWarnings("unused")
public class PropertiesConfiguration {

    /**
     * Tạo một cấu hình Properties từ file.
     * @param file File .properties cần load
     * @param splitter Ký tự phân cách dùng cho List (ví dụ "," hoặc "|"). Mặc định là ",".
     */
    public static EmbeddedPropertiesConfiguration of(File file, String splitter) {
        return new EmbeddedPropertiesConfigurationImpl(file, splitter);
    }

    // Overload tiện ích: Dùng mặc định dấu phẩy ","
    public static EmbeddedPropertiesConfiguration of(File file) {
        return new EmbeddedPropertiesConfigurationImpl(file, ",");
    }
}
