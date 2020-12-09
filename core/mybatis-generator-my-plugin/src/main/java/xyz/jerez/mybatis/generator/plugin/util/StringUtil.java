package xyz.jerez.mybatis.generator.plugin.util;

/**
 * @author liqilin
 * @since 2020/12/9 17:12
 */
public class StringUtil {

    /**
     * 字符串判空
     */
    public static boolean isEmpty(String columnRemark) {
        return columnRemark == null || "".equals(columnRemark);
    }

}
