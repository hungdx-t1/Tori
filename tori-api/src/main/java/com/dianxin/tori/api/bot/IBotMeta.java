package com.dianxin.tori.api.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

@SuppressWarnings("unused")
public interface IBotMeta {
    @NotNull String botName();
    @UnknownNullability String botDescription();
    @NotNull String botVersion();
    @NotNull String botAuthor();
    @NotNull List<String> botContributors();
    @NotNull String mainClassPath();

    // @Nullable String botWebsite(); coming soon
}