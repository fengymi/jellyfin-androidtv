package com.tv.fengymi.danmu.utils;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DanmuUtils {
    public static int MAX_THREAD_NUM = 3;
    private static volatile ExecutorService danmuExecutorService;

    /**
     * 获取可执行的线程池
     * @return 可执行的线程池
     */
    public static ExecutorService getDanmuExecutorService() {
        if (danmuExecutorService == null) {
            synchronized (DanmuUtils.class) {
                if (danmuExecutorService == null) {
                    danmuExecutorService = Executors.newFixedThreadPool(MAX_THREAD_NUM);
                }
            }
        }

        return danmuExecutorService;
    }

    /**
     * 提交任务
     * @param runnable 提交任务
     */
    public static void submit(Runnable runnable) {
        getDanmuExecutorService().execute(runnable);
    }
    /**
     * 提交任务
     * @param callable 提交任务
     */
    public static <V> Future<V> submit(Callable<V> callable) {
        return getDanmuExecutorService().submit(callable);
    }
}
