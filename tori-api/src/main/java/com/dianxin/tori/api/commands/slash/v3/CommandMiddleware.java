package com.dianxin.tori.api.commands.slash.v3;

@FunctionalInterface
public interface CommandMiddleware {
    /** Trả về true nếu cho phép request đi tiếp */
    boolean handle(SlashContext ctx, ICommand command);
}