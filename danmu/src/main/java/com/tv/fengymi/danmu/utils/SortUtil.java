package com.tv.fengymi.danmu.utils;

import java.util.List;

public class SortUtil {

    @SuppressWarnings("unchecked")
    public static <T> int findFirstGreaterThanOrEqual(List<? extends Comparable<T>> arr, Comparable<T> target, int minIndex) {
        int low = minIndex + 1;
        int high = arr.size() - 1;
        int result = -1; // 如果未找到任何大于等于 x 的元素，就返回 -1

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (arr.get(mid).compareTo((T) target) >= 0) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return result;
    }
}
