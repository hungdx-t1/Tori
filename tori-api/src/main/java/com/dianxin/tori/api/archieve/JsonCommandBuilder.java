package com.dianxin.tori.api.archieve;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.io.File;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.2.5")
@NullMarked
@SuppressWarnings("unused")
@ApiStatus.AvailableSince("2.3")
public class JsonCommandBuilder {
    public static CommandData of(File file) {
        return null;
    }

    public static class CommandDataBuilder {
        public static CommandData of(String commandName, @Nullable String description) {
            return Commands.slash(commandName, description == null ? "No description provided" : description);
        }
    }

    public static class OptionDataBuilder {
        public static OptionType typeOf(String optType) throws IllegalArgumentException {
            return switch (optType.toLowerCase()) {
                case "int", "integer" -> OptionType.INTEGER;
                case "string" -> OptionType.STRING;
                case "boolean" -> OptionType.BOOLEAN;
                case "user" -> OptionType.USER;
                case "channel"  -> OptionType.CHANNEL;
                case "role" -> OptionType.ROLE;
                case "mentionable" -> OptionType.MENTIONABLE;
                case "number" ->  OptionType.NUMBER;
                case "attachment" -> OptionType.ATTACHMENT;
                default -> throw new IllegalArgumentException("Unknown option type: " + optType);
            };
        }

        public static OptionData of(String optType, String optionName, @Nullable String description) throws IllegalArgumentException {
            OptionType type = OptionDataBuilder.typeOf(optType);
            return new OptionData(type, optionName, description == null ? "No description provided" : description);
        }
    }
}
