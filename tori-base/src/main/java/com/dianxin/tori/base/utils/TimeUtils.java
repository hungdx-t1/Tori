package com.dianxin.tori.base.utils;

/**
 * <b>[EN]</b> Utility class for handling time formatting and parsing.<br>
 * Primarily used for media playback features like seeking and duration display.
 * <br><br>
 * <b>[VN]</b> Lớp tiện ích để xử lý việc định dạng và phân tích thời gian.<br>
 * Chủ yếu được sử dụng cho các tính năng phát phương tiện như tua (seek) và hiển thị thời lượng.
 */
@SuppressWarnings("unused")
public final class TimeUtils {

    /**
     * <b>[EN]</b> Formats a duration in milliseconds into a human-readable string.<br>
     * Format: {@code HH:MM:SS} or {@code MM:SS}. Returns "LIVE" if duration is {@link Long#MAX_VALUE}.
     * <br><br>
     * <b>[VN]</b> Định dạng khoảng thời gian (mili giây) thành chuỗi dễ đọc.<br>
     * Định dạng: {@code HH:MM:SS} hoặc {@code MM:SS}. Trả về "LIVE" nếu thời lượng là {@link Long#MAX_VALUE}.
     *
     * @param duration
     * <b>[EN]</b> Time in milliseconds.<br>
     * <b>[VN]</b> Thời gian tính bằng mili giây.
     * @return
     * <b>[EN]</b> Formatted time string.<br>
     * <b>[VN]</b> Chuỗi thời gian đã được định dạng.
     */
    public static String formatTime(long duration) {
        if (duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration / 1000.0);
        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        long minutes = seconds / 60;
        seconds %= 60;
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    /**
     * <b>[EN]</b> Parses a seek time string into milliseconds and determines if it's relative.<br>
     * Supports "colon time" (e.g., {@code 1:20:30}) or "unit time" (e.g., {@code 1h20m}).<br>
     * Also detects relative seeking (starting with {@code +} or {@code -}).
     * <br><br>
     * <b>[VN]</b> Phân tích chuỗi thời gian thành mili giây và xác định xem đó có phải là thời gian tương đối không.<br>
     * Hỗ trợ "colon time" (vd: {@code 1:20:30}) hoặc "unit time" (vd: {@code 1h20m}).<br>
     * Cũng phát hiện việc tua tương đối (bắt đầu bằng {@code +} hoặc {@code -}).
     *
     * @param args
     * <b>[EN]</b> The time string to parse.<br>
     * <b>[VN]</b> Chuỗi thời gian cần phân tích.
     * @return
     * <b>[EN]</b> A {@link SeekTime} object containing milliseconds and relative flag, or {@code null} if parsing failed.<br>
     * <b>[VN]</b> Đối tượng {@link SeekTime} chứa số mili giây và cờ tương đối, hoặc {@code null} nếu phân tích thất bại.
     */
    public static SeekTime parseTime(String args) {
        if (args.isEmpty()) return null;
        String timestamp = args;
        boolean relative = false; // seek forward or backward
        boolean isSeekingBackwards = false;
        char first = timestamp.charAt(0);
        if (first == '+' || first == '-') {
            relative = true;
            isSeekingBackwards = first == '-';
            timestamp = timestamp.substring(1);
        }

        long milliseconds = parseColonTime(timestamp);
        if (milliseconds == -1) milliseconds = parseUnitTime(timestamp);
        if (milliseconds == -1) return null;

        milliseconds *= isSeekingBackwards ? -1 : 1;

        return new SeekTime(milliseconds, relative);
    }

    /**
     * <b>[EN]</b> Parses a timestamp formatted with colons.<br>
     * Format accepted: {@code HH:MM:SS}, {@code MM:SS}, or {@code SS}.
     * <br><br>
     * <b>[VN]</b> Phân tích chuỗi thời gian được định dạng bằng dấu hai chấm.<br>
     * Các định dạng chấp nhận: {@code HH:MM:SS}, {@code MM:SS}, hoặc {@code SS}.
     *
     * @param timestamp
     * <b>[EN]</b> Timestamp string (e.g., "1:30:00").<br>
     * <b>[VN]</b> Chuỗi thời gian (vd: "1:30:00").
     * @return
     * <b>[EN]</b> Time in milliseconds, or -1 if format is invalid.<br>
     * <b>[VN]</b> Thời gian tính bằng mili giây, hoặc -1 nếu định dạng không hợp lệ.
     */
    public static long parseColonTime(String timestamp) {
        String[] timestampSplitArray = timestamp.split(":+");
        if (timestampSplitArray.length > 3)
            return -1;
        double[] timeUnitArray = new double[3]; // hours, minutes, seconds
        for (int index = 0; index < timestampSplitArray.length; index++) {
            String unit = timestampSplitArray[index];
            if (unit.startsWith("+") || unit.startsWith("-")) return -1;
            unit = unit.replace(",", ".");
            try {
                timeUnitArray[index + 3 - timestampSplitArray.length] = Double.parseDouble(unit);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return Math.round(timeUnitArray[0] * 3600000 + timeUnitArray[1] * 60000 + timeUnitArray[2] * 1000);
    }

    /**
     * <b>[EN]</b> Parses a time string formatted with units.<br>
     * Examples: {@code 20m10s}, {@code 1d5h}, {@code 1h and 20m}.
     * <br><br>
     * <b>[VN]</b> Phân tích chuỗi thời gian được định dạng kèm đơn vị.<br>
     * Ví dụ: {@code 20m10s}, {@code 1d5h}, {@code 1h and 20m}.
     *
     * @param timestr
     * <b>[EN]</b> The unit time string.<br>
     * <b>[VN]</b> Chuỗi thời gian kèm đơn vị.
     * @return
     * <b>[EN]</b> Time in milliseconds, or -1 if parsing fails.<br>
     * <b>[VN]</b> Thời gian tính bằng mili giây, hoặc -1 nếu phân tích thất bại.
     */
    public static long parseUnitTime(String timestr) {
        timestr = timestr.replaceAll("(?i)(\\s|,|and)", "")
                .replaceAll("(?is)(-?\\d+|[a-z]+)", "$1 ")
                .trim();
        String[] vals = timestr.split("\\s+");
        int time = 0;
        try {
            for (int j = 0; j < vals.length; j += 2) {
                int num = Integer.parseInt(vals[j]);

                if (vals.length > j + 1) {
                    if (vals[j + 1].toLowerCase().startsWith("m"))
                        num *= 60;
                    else if (vals[j + 1].toLowerCase().startsWith("h"))
                        num *= 60 * 60;
                    else if (vals[j + 1].toLowerCase().startsWith("d"))
                        num *= 60 * 60 * 24;
                }

                time += num * 1000;
            }
        } catch (Exception ex) {
            return -1;
        }
        return time;
    }

    /**
     * <b>[EN]</b> Record to store the result of time parsing.<br>
     * Contains the calculated duration and whether the seek is relative to current position.
     * <br><br>
     * <b>[VN]</b> Record để lưu trữ kết quả phân tích thời gian.<br>
     * Chứa khoảng thời gian đã tính toán và cờ xác định xem có phải là tua tương đối so với vị trí hiện tại hay không.
     *
     * @param milliseconds
     * <b>[EN]</b> The parsed time in milliseconds.<br>
     * <b>[VN]</b> Thời gian phân tích được tính bằng mili giây.
     * @param relative
     * <b>[EN]</b> {@code true} if seeking relative to current position (starts with +/-).<br>
     * <b>[VN]</b> {@code true} nếu tua tương đối so với vị trí hiện tại (bắt đầu bằng +/-).
     */
    public record SeekTime(long milliseconds, boolean relative) {
    }
}