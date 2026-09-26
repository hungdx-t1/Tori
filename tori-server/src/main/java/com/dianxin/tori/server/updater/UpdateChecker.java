package com.dianxin.tori.server.updater;

import com.dianxin.tori.api.base.Constants;
import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.concurrent.FutureAction;
import com.dianxin.tori.base.lifecycle.ExecutorManager;
import net.dv8tion.jda.api.JDAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@ReleasedSince("26.8.301")
@SuppressWarnings({"JavadocLinkAsPlainText", "LoggingSimilarMessage"})
public class UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(UpdateChecker.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final String TORI_API_URL = "https://api.github.com/repos/hungdx-t1/Tori/releases/latest";
    private static final String JDA_API_URL = "https://api.github.com/repos/discord-jda/JDA/releases/latest";

    private static final String DOWNLOAD_URL_TEMPLATE = "https://github.com/hungdx-t1/Tori/releases/download/%s/%s";

    /**
     * Get latest version tag from a specified GitHub repository releases API endpoint.
     */
    private static String fetchLatestTag(String apiUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Tori-Framework-Updater")
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IllegalStateException("No releases found on GitHub repository: " + apiUrl);
        }

        if (response.statusCode() != 200) {
            throw new IllegalStateException("GitHub API responded with HTTP " + response.statusCode());
        }

        JsonNode root = MAPPER.readTree(response.body());
        return root.path("tag_name").asString().trim();
    }

    /**
     * Get latest version tag from GitHub Releases.
     * Usage: UpdateChecker.checkLatestVersionAsync().queue(latestTag -> ..., ex -> ...);
     * <p>
     * Example:
     * <code><pre>
     * UpdateChecker.checkLatestVersionAsync().queue(
     *     latestTag -> {
     *         String current = Constants.TORI_SERVER_VERSION;
     *         int cmp = UpdateChecker.compareVersions(current, latestTag);
     *
     *         if (cmp < 0) {
     *             sender.sendMessage("⚠️ New version found: " + latestTag + " (Current: " + current + ")");
     *             sender.sendMessage("🔗 https://github.com/hungdx-t1/Tori/releases/tag/" + latestTag);
     *         } else if (cmp == 0) {
     *             sender.sendMessage("✅ Tori is up to date (" + current + ").");
     *         } else {
     *             sender.sendMessage("🚀 Running a dev build (" + current + ") ahead of release (" + latestTag + ").");
     *         }
     *     },
     * error -> sender.sendMessage("❌ Failed to check for update: " + error.getMessage())
     * );
     * </pre></code>
     */
    public static FutureAction<String> checkLatestVersionAsync() {
        return FutureAction.action(() -> fetchLatestTag(TORI_API_URL), ExecutorManager.io());
    }

    /**
     * Get latest release version tag of JDA from GitHub Releases.
     */
    @ReleasedSince("26.9.1")
    public static FutureAction<String> checkLatestJdaVersionAsync() {
        return FutureAction.action(() -> fetchLatestTag(JDA_API_URL), ExecutorManager.io());
    }

    /**
     * Automatically check and download the latest JAR file into the root directory.
     *
     * @param withDave true if downloading the version with Dave audio, false if downloading the core version.
     * @return FutureAction return true if download successful, false if there is no new update.
     */
    public static FutureAction<Boolean> downloadLatestVersionIfAvailable(boolean withDave) {
        return checkLatestVersionAsync().map(latestTag -> {
            String current = Constants.TORI_SERVER_VERSION;
            int compare = compareVersions(current, latestTag);

            if (compare >= 0) {
                log.info("Server is already running the latest version or a newer build ({} >= {}). Download skipped.", current, latestTag);
                return false;
            }

            String jarFileName = withDave ? "tori-server-full-dave.jar" : "tori-server-core.jar";
            String downloadUrl = String.format(DOWNLOAD_URL_TEMPLATE, latestTag, jarFileName);

            log.info("🚀 Preparing to download Tori Server update: {} ({})", latestTag, jarFileName);
            log.info("🔗 Fetching from: {}", downloadUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .timeout(Duration.ofMinutes(3))
                    .header("User-Agent", "Tori-Framework-Updater")
                    .GET()
                    .build();

            // First save to a temporary file in the root folder to avoid the file getting corrupted if the network drops
            Path tempTarget = Path.of("server-update.jar.tmp");
            Path finalTarget = Path.of("server.jar");

            HttpResponse<InputStream> response;
            try {
                response = CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Cannot handle HTTPResponse", e);
            }

            // Handle redirects if GitHub Release redirects to S3 / Objects CDN (HTTP 302 / 301)
            if (response.statusCode() == 302 || response.statusCode() == 301) {
                String redirectedUrl = response.headers().firstValue("Location").orElseThrow();
                HttpRequest redirectRequest = HttpRequest.newBuilder()
                        .uri(URI.create(redirectedUrl))
                        .timeout(Duration.ofMinutes(5))
                        .GET()
                        .build();
                try {
                    response = CLIENT.send(redirectRequest, HttpResponse.BodyHandlers.ofInputStream());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Cannot handle HTTPResponse", e);
                }
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Failed to download JAR file, GitHub responded with status: " + response.statusCode());
            }

            try (InputStream in = response.body()) {
                Files.copy(in, tempTarget, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Cannot download Tori Server update.", e);
            }

            // override main file
            try {
                Files.move(tempTarget, finalTarget, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                log.info("✅ Successfully updated '{}' to version {}! Please restart the server to apply changes.", finalTarget.getFileName(), latestTag);
            } catch (Exception e) {
                // On some operating systems (Windows), a running file is locked and cannot be replaced immediately
                Path alternative = Path.of(jarFileName);
                try {
                    Files.move(tempTarget, alternative, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new RuntimeException("Cannot handle moving file request.", ex);
                }
                log.warn("⚠️ Cannot directly replace running 'server.jar'. Saved update as '{}'. Replace it manually when the server stops.", alternative.getFileName());
            }

            return true;
        });
    }

    /**
     * Check for both Tori and JDA updates and print status to console.
     * Usage: UpdateChecker.checkForUpdateAsync().queue(success -> ..., failure -> ...);
     */
    public static FutureAction<Void> checkForUpdateAsync() {
        return FutureAction.action(() -> {
            // check update for Tori Framework
            try {
                String latestToriTag = checkLatestVersionAsync().submit().get();
                String currentToriVersion = Constants.TORI_SERVER_VERSION;
                int toriComparison = compareVersions(currentToriVersion, latestToriTag);

                if (toriComparison < 0) {
                    log.warn("==================================================================");
                    log.warn("🔔 A new Tori update is available: {} -> {}", currentToriVersion, latestToriTag);
                    log.warn("🔗 Release link: https://github.com/hungdx-t1/Tori/releases/tag/{}", latestToriTag);
                    log.warn("==================================================================");
                } else if (toriComparison == 0) {
                    log.info("Tori server is up to date (version: {}).", currentToriVersion);
                } else {
                    log.info("Running on a development build ({}) ahead of latest release ({}).", currentToriVersion, latestToriTag);
                }
            } catch (Exception e) {
                log.warn("Failed to check for Tori updates: {}", e.getMessage());
            }

            // check update for JDA
            try {
                String latestJdaTag = checkLatestJdaVersionAsync().submit().get();
                String currentJdaVersion = JDAInfo.VERSION; // jda version in internal runtime
                int jdaComparison = compareVersions(currentJdaVersion, latestJdaTag);

                if (jdaComparison < 0) {
                    log.warn("------------------------------------------------------------------");
                    log.warn("📦 A newer JDA (Java Discord API) release is detected: {} -> {}", currentJdaVersion, latestJdaTag);
                    log.warn("💡 You may contact or open an issue for Tori Server developers to request a JDA dependency update if needed.");
                    log.warn("🔗 JDA Releases: https://github.com/discord-jda/JDA/releases/tag/{}", latestJdaTag);
                    log.warn("------------------------------------------------------------------");
                } else {
                    log.info("JDA dependency is up to date (version: {}).", currentJdaVersion);
                }
            } catch (Exception e) {
                log.warn("Failed to check for JDA updates: {}", e.getMessage());
            }

            return null;
        }, ExecutorManager.io());
    }

    /**
     * Compare two version strings by each array of numbers (x.y.z)
     * @return < 0 if v1 < v2, 0 if equals, > 0 if v1 > v2
     */
    public static int compareVersions(String v1, String v2) {
        String clean1 = v1.startsWith("v") ? v1.substring(1) : v1;
        String clean2 = v2.startsWith("v") ? v2.substring(1) : v2;

        String[] parts1 = clean1.split("[.-]");
        String[] parts2 = clean2.split("[.-]");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? tryParse(parts1[i]) : 0;
            int num2 = i < parts2.length ? tryParse(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private static int tryParse(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
