package com.datafactory.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态工具类
 *
 * 提供状态筛选参数的解析等通用方法。
 */
public class StatusUtils {

    /**
     * 解析状态筛选参数
     *
     * 将逗号分隔的状态字符串解析为状态值列表。
     * 例如："0,1,2" → [0, 1, 2]，无效值将被忽略。
     *
     * @param status 逗号分隔的状态字符串
     * @return 状态值列表
     */
    public static List<Integer> parseStatusList(String status) {
        List<Integer> statusList = new ArrayList<>();
        if (status == null || status.isBlank()) {
            return statusList;
        }
        for (String s : status.split(",")) {
            try {
                statusList.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                // 忽略无效状态值
            }
        }
        return statusList;
    }
}
