package com.dianxin.tori.server.bot;

import com.dianxin.tori.api.bot.IBotMeta;
import com.dianxin.tori.api.bot.JavaDiscordBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class BotLoaderTest {

    // create a virtual bot
    static class GhostBot extends JavaDiscordBot {
        @Override
        public void onShutdown() {
            // intentionally throw errors
            throw new NoClassDefFoundError("com/dianxin/bot/DatabaseManager (File deleted)");
        }

        @Override
        protected String getBotToken() {
            return "TEST_DISCORD_TOKEN";
        }

        @Override
        public void start() {
            // Do nothing, only testing shutdown behavior
        }

        // if getJda() exists in JavaDiscordBot, auto return null to easy passed Phase 2
    }

    // create virtual meta
    private IBotMeta createDummyMeta() {
        return new IBotMeta() {
            @Override
            public @NotNull String botName() {
                return "GhostBotTest";
            }

            @Override
            public @UnknownNullability String botDescription() {
                return "";
            }

            @Override
            public @NotNull String botVersion() {
                return "1.0.0";
            }

            @Override
            public @NotNull String botAuthor() {
                return "Tori";
            }

            @Override
            public @NotNull List<String> botContributors() {
                return List.of();
            }

            @Override
            public @NotNull String mainClassPath() {
                return "com.test.GhostBot";
            }

            @Override
            public @NotNull String botWebsite() {
                return "";
            }

            @Override
            public @NotNull String botOwnerId() {
                return "123456789";
            }
        };
    }

    @Test
    @DisplayName("Verification: Safely disable bot when JAR file is deleted (Throws NoClassDefFoundError)")
    void testShutdownGracefully_WhenJarDeleted() throws Exception {
        // Constructs
        BotLoader botLoader = new BotLoader();
        GhostBot ghostBot = new GhostBot();
        IBotMeta dummyMeta = createDummyMeta();

        // unlock private method "shutdownBotGracefully" by Reflection
        Method shutdownMethod = BotLoader.class.getDeclaredMethod("shutdownBotGracefully", JavaDiscordBot.class, IBotMeta.class);
        shutdownMethod.setAccessible(true);

        // act and assert
        Assertions.assertDoesNotThrow(() -> {
            try {
                shutdownMethod.invoke(botLoader, ghostBot, dummyMeta);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getTargetException();
            }
        }, "System crashed! NoClassDefFoundError was not caught properly.");
    }
}