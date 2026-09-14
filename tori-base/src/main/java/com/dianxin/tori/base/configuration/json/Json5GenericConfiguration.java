package com.dianxin.tori.base.configuration.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * A generic configuration manager that supports reading, saving,
 * and hot-reloading JSON5 configuration files at runtime.
 *
 * @param <T> The concrete configuration type, which must extend {@link AbstractJsonConfiguration}
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class Json5GenericConfiguration<T extends AbstractJsonConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(Json5GenericConfiguration.class);

    // Shared static ObjectMapper instance for performance optimization
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)           // Allows standard Java comments (// and /* */)
            .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)           // Allows YAML-style comments (#)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES) // Allows unquoted property keys
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)           // Allows single quotes ('') for strings
            // Note: Uncomment the line below if you want Jackson to automatically tolerate trailing commas
            // .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(SerializationFeature.INDENT_OUTPUT)            // Pretty-prints the output with indentation when saving
            .build();

    private T botConfig;
    private final File configFile;
    private final String defaultResource;
    private final Class<T> clazz;

    /**
     * Initializes the JSON5 configuration manager.
     * If the target configuration file does not exist, it will be copied from the specified default resource.
     *
     * @param defaultResource The fallback resource path inside the JAR (e.g., "config.json5")
     * @param filePath        The target file path on the server filesystem
     * @param clazz           The {@code Class<T>} token of the concrete configuration
     * @throws IOException    If an I/O error occurs during reading or writing
     */
    public Json5GenericConfiguration(String defaultResource, String filePath, Class<T> clazz) throws IOException {
        this.defaultResource = defaultResource;
        this.configFile = new File(filePath);
        this.clazz = clazz;

        ensureFileExists();
        reloadConfig();// Initial load
    }

    /** * Ensures that the configuration file exists on disk,
     * extracting it from the application resources if missing.
     */
    private void ensureFileExists() throws IOException {
        if (configFile.exists()) return;

        // Extract from resources
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(defaultResource)) {
            if (in == null) {
                throw new NoSuchFileException("Default configuration resource not found: " + defaultResource);
            }

            // If the file is located in the root project directory (has no parent), skip directory creation
            File parent = configFile.getParentFile();
            if (parent != null) parent.mkdirs();

            Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("✅ Default configuration file successfully created: {}", configFile.getAbsolutePath());
        }
    }

    /**
     * Reloads the configuration instance directly from the JSON5 file.
     */
    public void reloadConfig() {
        try {
            // Automatically check and add missing fields from defaultResource
            boolean updated = syncMissingKeysWithDefault();

            // Directly map from file (or JSON tree) to POJO
            this.botConfig = MAPPER.readValue(this.configFile, this.clazz);

            if (updated) {
                this.logger.info("🔄 Synced new configuration fields into '{}'", this.configFile.getName());
            }
            logger.info("✅ JSON5 configuration successfully reloaded from '{}'", configFile.getAbsolutePath());
        } catch (JacksonException e) {
            // Catch syntax errors explicitly (Jackson prints the exact line and column where the error occurred)
            logger.error("❌ JSON SYNTAX ERROR IN FILE '{}'\nDetails: {}", configFile.getName(), e.getMessage());
        } catch (Exception e) {
            logger.error("❌ System error occurred while reading configuration file '{}'", configFile.getAbsolutePath(), e);
        }
    }

    /**
     * Saves the current configuration instance state back to the JSON5 file.
     */
    public void saveConfig() {
        try {
            MAPPER.writeValue(configFile, botConfig);
            logger.info("✅ JSON5 configuration successfully saved to '{}'", configFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("❌ Error occurred while saving JSON5 configuration to '{}'", configFile.getAbsolutePath(), e);
        }
    }

    /**
     * Match the user's config file with the default file in resources.
     * Automatically add missing fields without losing the existing configuration.
     */
    private boolean syncMissingKeysWithDefault() {
        try (InputStream in = this.clazz.getClassLoader().getResourceAsStream(this.defaultResource)) {
            if (in == null) return false;

            JsonNode defaultTree = MAPPER.readTree(in);
            JsonNode userTree = MAPPER.readTree(this.configFile);

            if (!(defaultTree instanceof ObjectNode defaultObj) || !(userTree instanceof ObjectNode userObj)) {
                return false;
            }

            // Perform recursive merge
            boolean modified = mergeMissingFields(defaultObj, userObj);

            // If a new field is added, overwrite the file on disk
            if (modified) {
                MAPPER.writeValue(this.configFile, userObj);
                return true;
            }
        } catch (Exception e) {
            this.logger.warn("⚠️ Could not auto-sync missing config fields: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Iterate through each field of the default: if the user doesn't have it, add it.
     * If it's nested objects, continue to traverse recursively.
     */
    private boolean mergeMissingFields(ObjectNode defaultNode, ObjectNode userNode) {
        boolean modified = false;
        for (Map.Entry<String, JsonNode> entry : defaultNode.properties()) {
            String fieldName = entry.getKey();
            JsonNode defaultValue = entry.getValue();

            if (!userNode.has(fieldName)) {
                // The user does not have this field -> Add default value
                userNode.set(fieldName, defaultValue);
                modified = true;
                this.logger.info("➕ Added missing configuration field: '{}'", fieldName);
            } else if (defaultValue.isObject() && userNode.get(fieldName).isObject()) {
                // If both are objects (like warningDatabaseConfig), recurse into them
                boolean childModified = mergeMissingFields((ObjectNode) defaultValue, (ObjectNode) userNode.get(fieldName));
                if (childModified) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    public File getConfigFile() {
        return configFile;
    }

    public T getBotConfig() {
        return botConfig;
    }
}
