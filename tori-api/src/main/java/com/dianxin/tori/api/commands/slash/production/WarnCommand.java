package com.dianxin.tori.api.commands.slash.production;

import com.dianxin.tori.api.commands.slash.develop.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Command(name = "warn", description = "Hệ thống cảnh cáo thành viên")
public class WarnCommand {

    @Subcommand(name = "add", description = "Thêm cảnh cáo cho người dùng")
    public static class AddWarn {
        @CommandOption(type = OptionType.USER, name = "target", description = "Người bị phạt", required = true)
        private Member target;

        @CommandOption(type = OptionType.STRING, name = "reason", description = "Lý do cảnh cáo", required = true)
        private String reason;

        @Execute
        public void run(SlashCommandInteractionEvent event) {
            event.getHook().sendMessage("⚠️ Đã cảnh cáo " + target.getAsMention() + " vì lý do: " + reason).queue();
        }
    }

    @Subcommand(name = "remove", description = "Xóa cảnh cáo")
    public static class RemoveWarn {
        @CommandOption(type = OptionType.USER, name = "target", description = "Người được gỡ phạt", required = true)
        private Member target;

        @CommandOption(type = OptionType.INTEGER, name = "id", description = "ID cảnh cáo", required = true)
        private int warnId;

        @Execute
        public void run(SlashCommandInteractionEvent event) {
            event.getHook().sendMessage("✅ Đã xóa cảnh cáo #" + warnId + " của " + target.getAsMention()).queue();
        }
    }
}