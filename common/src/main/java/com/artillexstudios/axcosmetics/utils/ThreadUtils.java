package com.artillexstudios.axcosmetics.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.logging.LogUtils;

public final class ThreadUtils {

    public static void ensureMain(String message) {
        if (!Scheduler.get().isGlobalTickThread()) {
            LogUtils.error("Thread {} failed main thread check: {}", Thread.currentThread().getName(), message, new Throwable());
            throw new IllegalStateException(message);
        }
    }
}
