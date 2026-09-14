package com.dianxin.tori.server.updater;

import com.dianxin.tori.api.base.Constants;
import com.dianxin.tori.base.concurrent.FutureAction;
import com.dianxin.tori.base.lifecycle.ExecutorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@SuppressWarnings({"JavadocLinkAsPlainText", "LoggingSimilarMessage"})
public class UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(UpdateChecker.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final String API_URL = "https://api.github.com/repos/hungdx-t1/Tori/releases/latest";

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
        return FutureAction.action(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Tori-Framework-Updater")
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new IllegalStateException("No releases found on GitHub repository.");
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException("GitHub API responded with HTTP " + response.statusCode());
            }

            JsonNode root = MAPPER.readTree(response.body());
            return root.path("tag_name").asString().trim();
        }, ExecutorManager.io());
    }

    /**
     * Check for update and print to console (use on startup or run console command).
     * Usage: UpdateChecker.checkForUpdateAsync().queue(success -> ..., failure -> ...);
     */
    public static FutureAction<Void> checkForUpdateAsync() {
        return FutureAction.action(() -> {
            String latestTag = checkLatestVersionAsync().submit().get();
            String currentVersion = Constants.TORI_SERVER_VERSION;

            int comparison = compareVersions(currentVersion, latestTag);

            if (comparison < 0) {
                log.warn("==================================================================");
                log.warn("🔔 A new Tori update is available: {} -> {}", currentVersion, latestTag);
                log.warn("🔗 Release link: https://github.com/hungdx-t1/Tori/releases/tag/{}", latestTag);
                log.warn("==================================================================");
            } else if (comparison == 0) {
                log.info("Tori server is up to date (version: {}).", currentVersion);
            } else {
                log.info("Running on a development build ({}) ahead of latest release ({}).", currentVersion, latestTag);
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
