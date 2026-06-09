package com.dianxin.tori.base.utils.example;

import com.dianxin.tori.base.utils.EmbeddedObjectUtils;
import com.dianxin.tori.base.utils.EntityString;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public record StaticPlayerStats(
        String name,
        int kills,
        int deaths,
        LocalDateTime firstJoin,
        LocalDateTime lastJoin,
        int level,
        int totalExpLevels,
        Map<String, Object> placeholders
) {
    @Override
    public @NonNull String toString() {
        // 1. Xử lý format Map cho gọn (nếu Map null thì trả về "empty")
        String placeholdersStr = (placeholders == null || placeholders.isEmpty())
                ? "{}"
                : placeholders.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));

        // 2. Sử dụng Utils
        return EmbeddedObjectUtils.generateToString(
                StaticPlayerStats.class,
                "name", name,
                "kills", kills,
                "deaths", deaths,
                "firstJoin", firstJoin != null ? firstJoin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "null",
                "lastJoin", lastJoin != null ? lastJoin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "null",
                "level", level,
                "totalExpLevels", totalExpLevels,
                "placeholders", placeholdersStr
        );
    }

    // @Override
    public String toString2() {
        return new EntityString(this)
                .add("name", name)
                .add("kills", kills)
                .add("deaths", deaths)
                // Format ngày tháng trực tiếp ở đây hoặc để Utils tự lo
                .add("firstJoin", firstJoin)
                .add("lastJoin", lastJoin)
                .add("level", level)
                .add("totalExp", totalExpLevels)
                // Map placeholders có thể add thẳng object, nó sẽ gọi toString của Map
                .add("placeholders", placeholders)
                .toString();
    }

    // Nếu bạn muốn override cả equals/hashCode (dù record đã có sẵn)
    @Override
    public int hashCode() {
        return EmbeddedObjectUtils.generateHashCode(name, kills, deaths, firstJoin, lastJoin, level, totalExpLevels, placeholders);
    }
}