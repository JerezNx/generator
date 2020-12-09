package xyz.jerez.mybatis.generator.plugin.plugin;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author liqilin
 * @since 2020/12/9 16:11
 */
public class MapperCommentPlugin implements Plugin {

    private DateTimeFormatter dateFormat;

    @Override
    public boolean clientGenerated(Interface mapper, IntrospectedTable introspectedTable) {
        mapper.addJavaDocLine("/**");
        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                mapper.addJavaDocLine(" * " + remarkLine);
            }
        }
        String s = " * " + introspectedTable.getFullyQualifiedTable();
        mapper.addJavaDocLine(s);
        mapper.addJavaDocLine(" *");
        mapper.addJavaDocLine(getAuthorTag());
        mapper.addJavaDocLine(getDateTag());
        mapper.addJavaDocLine(" */");
        return true;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public void setProperties(Properties properties) {
        String dateFormatString = Optional.ofNullable(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT))
                .orElse("yyyy-MM-dd HH:mm:ss");
        if (StringUtility.stringHasValue(dateFormatString)) {
            dateFormat = DateTimeFormatter.ofPattern(dateFormatString);
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private String getDateString() {
        if (dateFormat != null) {
            return LocalDateTime.now().format(dateFormat);
        } else {
            return LocalDateTime.now().toString();
        }
    }

    private String getAuthorTag() {
        return " * @author " + System.getProperty("user.name");
    }

    private String getDateTag() {
        return " * @since " + getDateString();
    }
}
