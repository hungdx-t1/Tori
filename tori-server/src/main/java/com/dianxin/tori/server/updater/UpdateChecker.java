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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
