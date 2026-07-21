package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Tiện ích tạo chuỗi toString() theo phong cách Fluent Builder.
 * Thay thế cho EmbeddedObjectUtils.generateToString dùng varargs thiếu an toàn.
 * <p>
 * Usage:
 * <pre><code>
 *      literal @Override
 *      public String toString() {
 *          return new EntityString(this)
 *              .add("id", 123)
 *              .add("name", "Mino")
 *              .toString();
 *      }
 * </code></pre>
 * Output: ClassName{id=123, name=Mino}
 */
@SuppressWarnings("unused")
public class EntityString {
    private final String entityName;
    private final StringJoiner joiner;

    public EntityString(@NotNull Object entity) {
        this(resolveName(entity.getClass()));
    }

    public EntityString(@NotNull Class<?> clazz) {
        this(resolveName(clazz));
    }

    // Constructor cho trường hợp muốn custom tên
    public EntityString(@NotNull String name) {
        this.entityName = name;
        // Format chuẩn: Name{key=value, key2=value2}
        this.joiner = new StringJoiner(", ", "{", "}");
    }

    /**
     * Thêm một cặp key-value vào chuỗi.
     */
    public EntityString add(@NotNull String key, @Nullable Object value) {
        // Tự động handle null value thành chuỗi "null"
        joiner.add(key + "=" + value);
        return this;
    }

    /**
     * Thêm cặp key-value nhưng format giá trị là chuỗi (có dấu nháy kép).
     * Ví dụ: name="Mino" thay vì name=Mino
     */
    public EntityString addString(@NotNull String key, @Nullable String value) {
        if (value == null) {
            joiner.add(key + "=null");
        } else {
            joiner.add(key + "=\"" + value + "\"");
        }
        return this;
    }

    /**
     * Chỉ thêm nếu giá trị khác null (Conditional Add).
     * Giúp toString gọn hơn, đỡ bị spam null.
     */
    public EntityString addIfNotNull(@NotNull String key, @Nullable Object value) {
        if (value != null) {
            add(key, value);
        }
        return this;
    }

    @Override
    public String toString() {
        // Kết hợp tên entity và nội dung trong ngoặc
        return entityName + joiner.toString();
    }

    // --- Helper ---
    private static String resolveName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        // Nếu là Anonymous class (VD: new Runnable() { ... }) thì lấy tên cha
        if (name.isEmpty()) {
            name = clazz.getSuperclass().getSimpleName();
        }
        // Xử lý logic clean tên giống JDA (bỏ Impl, thay $ bằng .)
        return name.replace("Impl", "").replace("$", ".");
    }
}