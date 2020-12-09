package xyz.jerez.mybatis.generator.plugin.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.config.Context;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author liqilin
 * @since 2020/12/9 16:59
 */
public class JavaPropertyPlugin implements Plugin {

    private String prefix;

    private static final String DEFAULT_PREFIX = "_";

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            String javaProperty = column.getJavaProperty();
            if (javaProperty != null && Character.isDigit(javaProperty.charAt(0))) {
                javaProperty = prefix + javaProperty;
                column.setJavaProperty(javaProperty);
            }
        }
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public void setProperties(Properties properties) {
        prefix = Optional.ofNullable(properties.getProperty("prefix")).orElse(DEFAULT_PREFIX);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}
