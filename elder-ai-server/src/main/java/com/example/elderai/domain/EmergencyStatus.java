package com.example.elderai.domain;

import com.example.elderai.common.BusinessException;

import java.util.Map;
import java.util.Set;

/** 紧急求助状态机。 */
public final class EmergencyStatus {

    public static final int SUBMITTED = 0;
    public static final int ACKNOWLEDGED = 1;
    public static final int PROCESSING = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;
    public static final int ESCALATED = 5;

    private static final Map<Integer, Set<Integer>> TRANSITIONS = Map.of(
            SUBMITTED, Set.of(ACKNOWLEDGED, CANCELLED, ESCALATED),
            ACKNOWLEDGED, Set.of(PROCESSING, COMPLETED),
            PROCESSING, Set.of(COMPLETED),
            ESCALATED, Set.of(ACKNOWLEDGED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    private EmergencyStatus() {
    }

    public static void requireTransition(int current, int target) {
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessException(400, "当前求助状态不允许执行该操作");
        }
    }

    public static boolean isTerminal(int status) {
        return status == COMPLETED || status == CANCELLED;
    }
}
