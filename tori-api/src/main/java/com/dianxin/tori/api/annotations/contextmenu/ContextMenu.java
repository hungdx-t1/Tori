package com.dianxin.tori.api.annotations.contextmenu;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu một class là User Context Menu
 * <br>
 * interactionName sẽ hiển thị trong menu chuột phải
 * của Discord (User context)
 * <br>
 * Ví dụ:
 * {@code
 * @ContextMenu(interactionName = "View Profile")
 * }
 */
// TODO
@ApiStatus.AvailableSince("1.2")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.2.5")
public @interface ContextMenu {
    String interactionName();
}
