package com.dianxin.tori.base.configuration.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;

/**
 * Json5Configuration hỗ trợ đọc và lưu config JSON5,
 * đồng thời có thể reload lại config khi runtime.
 *
 * @param <T> Kiểu config cụ thể, phải kế thừa AbstractBotConfiguration
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class Json5GenericConfiguration<T extends AbstractBotConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(Json5GenericConfiguration.class);

    // Đối tượng Jackson tĩnh dùng chung để tối ưu hiệu suất
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)       // Cho phép // và /* */
            .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)       // Cho phép # comment
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES) // Cho phép key không có ""
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)       // Cho phép dùng nháy đơn ''
            // BẠN MUỐN BÁO LỖI HAY BỎ QUA DẤU PHẨY DƯ?
            // .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)   // Mở dòng này nếu muốn Jackson TỰ BỎ QUA dấu phẩy dư
            .enable(SerializationFeature.INDENT_OUTPUT)        // Lưu file sẽ tự xuống dòng, thụt lề cho đẹp
            .build();

    private T botConfig;
    private final File configFile;
    private final String defaultResource;
    private final Class<T> clazz;

    /**
     * Tạo config JSON5, nếu file chưa tồn tại sẽ copy từ resource mặc định.
     *
     * @param defaultResource Resource mặc định trong jar (vd: "config.json5")
     * @param filePath        Đường dẫn file config trên server
     * @param clazz           Class<T> của config cụ thể
     * @throws IOException nếu có lỗi đọc ghi
     */
    public Json5GenericConfiguration(String defaultResource, String filePath, Class<T> clazz) throws IOException {
        this.defaultResource = defaultResource;
        this.configFile = new File(filePath);
        this.clazz = clazz;

        ensureFileExists();
        reloadConfig(); // Load lần đầu
    }

    /** Đảm bảo file config tồn tại, copy từ resource nếu cần */
    private void ensureFileExists() throws IOException {
        if (configFile.exists()) return;

        // Copy từ resource
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(defaultResource)) {
            if (in == null) {
                throw new NoSuchFileException("Không tìm thấy default config: " + defaultResource);
            }

            // Nếu file nằm ở root project (không có parent), skip mkdirs
            File parent = configFile.getParentFile();
            if (parent != null) parent.mkdirs();

            Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("✅ File config mặc định đã được tạo: {}", configFile.getAbsolutePath());
        }
    }

    /** Reload config từ file JSON5 */
    public void reloadConfig() {
        try {
            this.botConfig = MAPPER.readValue(configFile, clazz);
            logger.info("✅ Config JSON5 đã được reload thành công từ '{}'", configFile.getAbsolutePath());
        } catch (JacksonException e) {
            // BẮT LỖI CÚ PHÁP TẠI ĐÂY (Nó sẽ in ra đúng dòng và cột bị lỗi)
            logger.error("❌ LỖI CÚ PHÁP JSON TRONG FILE '{}'\nChi tiết: {}", configFile.getName(), e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Lỗi hệ thống khi đọc file config '{}'", configFile.getAbsolutePath(), e);
        }
    }

    /** Lưu config hiện tại ra file JSON5 */
    public void saveConfig() {
        try {
            MAPPER.writeValue(configFile, botConfig);
            logger.info("✅ Config JSON5 đã được lưu thành công vào '{}'", configFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("❌ Lỗi khi lưu config JSON5 vào '{}'", configFile.getAbsolutePath(), e);
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public T getBotConfig() {
        return botConfig;
    }
}
