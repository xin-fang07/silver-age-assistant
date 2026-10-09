package com.example.elderai.domain;

import com.example.elderai.common.BusinessException;

import java.time.LocalDateTime;

/** 周期提醒的下一次时间计算。 */
public final class ReminderSchedule {

    public static final String ONCE = "ONCE";
    public static final String DAILY = "DAILY";
    public static final String WEEKLY = "WEEKLY";

    private ReminderSchedule() {
    }

    public static String normalize(String repeatType) {
        return repeatType == null || repeatType.isBlank() ? ONCE : repeatType;
    }

    public static boolean recurring(String repeatType) {
        String normalized = normalize(repeatType);
        return DAILY.equals(normalized) || WEEKLY.equals(normalized);
    }

    public static LocalDateTime nextAfter(LocalDateTime scheduled, String repeatType,
                                          LocalDateTime referenceTime) {
        if (scheduled == null) {
            throw new BusinessException(400, "提醒时间不能为空");
        }
        String normalized = normalize(repeatType);
        if (ONCE.equals(normalized)) {
            throw new BusinessException(400, "单次提醒没有下一次时间");
        }
        LocalDateTime next = scheduled;
        do {
            next = DAILY.equals(normalized) ? next.plusDays(1) : next.plusWeeks(1);
        } while (!next.isAfter(referenceTime));
        return next;
    }

    /** 统计截至指定时间已到期但尚未处理的周期次数。 */
    public static int dueOccurrences(LocalDateTime scheduled, String repeatType,
                                     LocalDateTime referenceTime) {
        if (scheduled == null || scheduled.isAfter(referenceTime)) {
            return 0;
        }
        String normalized = normalize(repeatType);
        if (ONCE.equals(normalized)) {
            return 1;
        }
        int count = 0;
        LocalDateTime cursor = scheduled;
        while (!cursor.isAfter(referenceTime)) {
            count++;
            cursor = DAILY.equals(normalized) ? cursor.plusDays(1) : cursor.plusWeeks(1);
        }
        return count;
    }
}
