package com.munjie.omni.utils;

public class UserContext {
    private static final ThreadLocal<Integer> CURRENT_USER = new ThreadLocal<>();
    private static final Integer SUPER_ADMIN_ID = 1;

    public static void setUserId(Integer userId) {
        CURRENT_USER.set(userId);
    }

    public static Integer getUserId() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
    public static boolean isSuperAdmin() {
        Integer userId = getUserId();
        return userId != null && userId.equals(SUPER_ADMIN_ID);
    }
}