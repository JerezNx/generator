package xyz.jerez.mybatis.generator.plugin.comment;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;

import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * 如果使用 lombok
 * - 实体类取消生成get、set
 * - import
 * - 添加注解
 *
 * @author liqilin
 * @since 2020/12/9 14:50
 */
public class LombokPlugin implements Plugin {

    private boolean useLombok;

    @Override
    public void setContext(Context context) {

    }

    @Override
    public void setProperties(Properties properties) {
        useLombok = isTrue(properties.getProperty("useLombok"));
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !useLombok;
    }

    private void addInfoIfUseLombok(TopLevelClass topLevelClass) {
        if (useLombok) {
            topLevelClass.addImportedType("lombok.Data");
            topLevelClass.addAnnotation("@Data");
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addInfoIfUseLombok(topLevelClass);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addInfoIfUseLombok(topLevelClass);
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !useLombok;
    }
}
