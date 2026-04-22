package com.dianxin.tori.api.archieve;

import com.dianxin.tori.api.commands.CommandRegistrar;
import com.dianxin.tori.api.commands.registry.MaincommandRegistry;
import com.dianxin.tori.api.commands.slash.LegacyBaseCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @deprecated use {@link CommandRegistrar} instead
 */
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.2.5")
@SuppressWarnings("all")
public class LegacyCommandHandler {
    private final JDA jda;

    // Lưu trữ các lệnh đã đăng ký.
    // Key là tên lệnh (vd: "play", "ban"), Value là class thực thi lệnh đó.
    private final Map<String, LegacyBaseCommand> commands = new HashMap<>();

    private final AtomicBoolean commitedAll = new AtomicBoolean(false);

    public LegacyCommandHandler(JDA jda) {
        this.jda = jda;
    }

    /**
     * Đăng ký một hoặc nhiều lệnh vào bộ nhớ của bot.
     */
    public LegacyCommandHandler registerCommands(LegacyBaseCommand... cmdInstances) {
        if (commitedAll.get()) {
            throw new IllegalStateException("Không thể đăng ký thêm lệnh sau khi đã commit!");
        }

        for (LegacyBaseCommand cmd : cmdInstances) {
            if (cmd instanceof MaincommandRegistry registry) {
                String commandName = registry.getCommand().getName(); // Tự động lấy tên lệnh từ CommandData để làm Key
                commands.put(commandName, cmd);
            } else {
                throw new IllegalArgumentException("Lệnh " + cmd.getClass().getSimpleName() + " phải implements MaincommandRegistry!");
            }
        }
        return this; // Hỗ trợ chaining (nối chuỗi lệnh)
    }

    /**
     * Gửi toàn bộ danh sách lệnh lên máy chủ Discord.
     */
    public void commitAllCommands(@Nullable Guild guild) {
        if (commitedAll.getAndSet(true)) return; // Tránh gọi 2 lần

        CommandListUpdateAction updateAction = guild == null ? jda.updateCommands() : guild.updateCommands();
        List<CommandData> commandDataList = new ArrayList<>();

        for (LegacyBaseCommand cmd : commands.values()) {
            CommandData data = ((MaincommandRegistry) cmd).getCommand();
            commandDataList.add(data);
        }

        updateAction.addCommands(commandDataList).queue(
                commands -> System.out.println("✅ Đã cập nhật thành công " + commandDataList.size() + " lệnh lên Discord!"),
                error -> System.err.println("❌ Lỗi cập nhật lệnh: " + error.getMessage())
        );
    }

    /**
     * Hàm này sẽ được gọi từ một ListenerAdapter để xử lý khi có người dùng gõ lệnh.
     */
    public void onEvent(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        LegacyBaseCommand command = commands.get(commandName);
        if (command == null) {
            event.reply("❌ Lệnh này không tồn tại hoặc chưa được nạp vào hệ thống.").setEphemeral(true).queue();
            return;
        }

        // Thực thi lệnh (BaseCommand.handle sẽ tự động check quyền, defer reply các kiểu)
        // command.handle(event);
    }
}
