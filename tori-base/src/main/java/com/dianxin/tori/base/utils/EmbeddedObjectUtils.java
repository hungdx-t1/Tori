package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public final class EmbeddedObjectUtils {

    // Chặn khởi tạo
    private EmbeddedObjectUtils() { }

    /**
     * Tạo HashCode dựa trên các trường dữ liệu.
     * Wrapper cho Objects.hash()
     */
    public static int generateHashCode(Object... objects) {
        return Objects.hash(objects);
    }

    /**
     * Tạo chuỗi toString format dạng: ClassName{key1=val1, key2=val2}
     *
     * @param clazz Class hiện tại
     * @param objects Danh sách tham số đan xen: Tên trường, Giá trị, Tên trường, Giá trị...
     * Ví dụ: "name", name, "age", age
     */
    @NotNull
    public static String generateToString(@NotNull Class<?> clazz, Object... objects) {
        // Format: ClassName{...}
        StringJoiner joiner = new StringJoiner(", ", clazz.getSimpleName() + "{", "}");

        if (objects != null) {
            for (int i = 0; i < objects.length; i += 2) {
                // lấy tên trường (Key)
                String key = String.valueOf(objects[i]);

                // lấy giá trị (Value), kiểm tra nếu mảng lẻ thì gán "null" hoặc ""
                Object value = (i + 1 < objects.length) ? objects[i + 1] : "null";

                joiner.add(key + "= \"" + value + "\""); // có thể dùng dấu ": " hoặc "=" tùy sở thích
            }
        }
        return joiner.toString();
    }
}