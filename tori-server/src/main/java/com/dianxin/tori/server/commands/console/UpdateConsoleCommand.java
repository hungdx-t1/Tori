package com.dianxin.tori.server.commands.console;

import com.dianxin.tori.base.annotations.ReleasedSince;
import com.dianxin.tori.base.console.commands.AbstractConsoleCommand;
import com.dianxin.tori.server.updater.UpdateChecker;

@ReleasedSince("26.9.1")
public class UpdateConsoleCommand extends AbstractConsoleCommand {
    public UpdateConsoleCommand() {
        super("update");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            getLogger().info("Checking for updates, please wait...");
            UpdateChecker.checkForUpdateAsync().queue(
                    success -> {},
                    error -> getLogger().error("Error while checking for updates: {}", error.getMessage())
            );
            return;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("download") || subCommand.equals("dl")) {
            boolean withDave = false;

            if (args.length >= 2) {
                String flag = args[1].toLowerCase();
                if (flag.equals("--dave") || flag.equals("-d")) {
                    withDave = true;
                } else if (!flag.equals("--core") && !flag.equals("-c")) {
                    getLogger().warn("Unknown flag '{}'. Available flags: --core, --dave", flag);
                    return;
                }
            }

            getLogger().info("Starting update process (Type: {})...", withDave ? "FULL-DAVE" : "CORE");
            UpdateChecker.downloadLatestVersionIfAvailable(withDave).queue(
                    downloaded -> {
                        if (downloaded) {
                            getLogger().info("🎉 Update download completed successfully!");
                        } else {
                            getLogger().info("No newer updates were downloaded.");
                        }
                    },
                    error -> getLogger().error("❌ Failed to download update: {}", error.getMessage(), error)
            );
            return;
        }

        getLogger().info("Usage:");
        getLogger().info("  update                   - Check for new Tori Framework and JDA releases.");
        getLogger().info("  update download [--core] - Download latest Tori Server Core JAR into root.");
        getLogger().info("  update download --dave   - Download latest Tori Server with JDave JAR into root.");
    }
}
