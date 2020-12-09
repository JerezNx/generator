package xyz.jerez.mybatis.generator.plugin.comment;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * 自定义注释
 *
 * @author liqilin
 * @since 2020/12/8 13:20
 */
@SuppressWarnings("ALL")
public class MyModelCommentGenerator extends AbstractCommentGenerator {

    private void init(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        解决sqlserver注释问题
        SqlServerCommentGenerator.freshTables(introspectedTable);
    }

    /**
     * 实体类
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        init(topLevelClass, introspectedTable);
        if (suppressAllComments || !addRemarkComments) {
            return;
        }
        topLevelClass.addJavaDocLine("/**");
        String remarks = introspectedTable.getRemarks();
        if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * " + remarkLine);
            }
        }
        String s = " * " + introspectedTable.getFullyQualifiedTable();
        topLevelClass.addJavaDocLine(s);
        topLevelClass.addJavaDocLine(" *");
        topLevelClass.addJavaDocLine(getAuthorTag());
        topLevelClass.addJavaDocLine(getDateTag());
        topLevelClass.addJavaDocLine(" */");
        if (useSwagger) {
            topLevelClass.addImportedType("io.swagger.annotations.ApiModel");
            topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
            topLevelClass.addAnnotation(String.format("@ApiModel(value = \"%s\", description = \"%s\")",
                    introspectedTable.getFullyQualifiedTable(), remarks));
        }
    }


    /**
     * 字段注释
     *
     * @param field              the field
     * @param introspectedTable  the introspected table
     * @param introspectedColumn
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        if (suppressAllComments) {
            return;
        }
        field.addJavaDocLine("/**");
        String remarks = introspectedColumn.getRemarks();
        if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                field.addJavaDocLine(" *   " + remarkLine);
            }
        }
        field.addJavaDocLine(" */");
        if (useSwagger) {
            field.addAnnotation(String.format("@ApiModelProperty(value = \"%s\")", remarks));
        }
    }

    /**
     * 方法注释
     * @param method
     * @param introspectedTable
     */
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * " + method.getName());
        final List<Parameter> parameters = method.getParameters();
        if (parameters.size() > 0) {
            method.addJavaDocLine(" * ");
            for (Parameter parameter : parameters) {
                method.addJavaDocLine(" * @param " + parameter.getName() + " " + parameter.getName());
            }
        }
        if (method.getReturnType().isPresent()) {
            if (parameters.size() == 0) {
                method.addJavaDocLine(" * ");
            }
            method.addJavaDocLine(" * @return " + method.getReturnType().get() + " " + "res");
        }
        method.addJavaDocLine(" */");
    }

    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
//        empty
    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
//       empty
    }
}
