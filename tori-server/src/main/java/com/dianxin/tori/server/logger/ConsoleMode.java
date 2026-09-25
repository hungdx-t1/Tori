package com.dianxin.tori.server.logger;

public enum ConsoleMode {
    CLASSIC("[%d{HH:mm:ss}] %-5level %msg%n%xEx"),
    SEMI_MODERN("[%d{HH:mm:ss}] %highlight{%-5level} %magenta{[%t]} - %highlight{%msg}{FATAL=red bold, ERROR=red bold, WARN=yellow bold, INFO=normal, DEBUG=magenta bold}%n%style{%xEx}{red}"),
    MODERN("[%d{HH:mm:ss}] %highlight{%-5level} %magenta{[%t]} %cyan{[%logger{3}]} - %highlight{%msg}{FATAL=red bold, ERROR=red bold, WARN=yellow bold, INFO=normal, DEBUG=magenta bold}%n%style{%xEx}{red}"),
    FULL_MODERN("[%d{HH:mm:ss}] %highlight{%-5level} %magenta{[%t]} %cyan{[%logger]} - %highlight{%msg}{FATAL=red bold, ERROR=red bold, WARN=yellow bold, INFO=normal, DEBUG=magenta bold}%n%style{%xEx}{red}");

    private final String pattern;

    ConsoleMode(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public static ConsoleMode fromString(String modeStr) {
        if (modeStr == null) return MODERN;
        try {
            return ConsoleMode.valueOf(modeStr.trim().toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return MODERN;
        }
    }
}