package xyz.jerez.mybatis.generator.plugin.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import xyz.jerez.mybatis.generator.plugin.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 解决 sqlserver 无法生成注释问题
 *
 * @author liqilin
 * @since 2020/12/9 13:35
 */
public class SqlServerCommentGenerator {

    /**
     * 是否已执行过
     */
    private static boolean refreshed = false;

    /**
     * sqlServer 查询字段注释 sql
     */
    private static final String TABLE_COLUMN_COMMENT_SQL = "SELECT convert(varchar(100), A.name)  AS table_name,\n" +
            "       convert(varchar(100), B.name)  AS column_name,\n" +
            "       convert(varchar(100), C.value) AS column_remark\n" +
            "FROM sys.tables A\n" +
            "         INNER JOIN sys.columns B ON B.object_id = A.object_id\n" +
            "         LEFT JOIN sys.extended_properties C ON C.major_id = B.object_id AND C.minor_id = B.column_id\n" +
            "WHERE A.name IN (%s)";

    /**
     * sqlServer 查询表注释 sql
     */
    private static final String TABLE_COMMENT_SQL = "SELECT DISTINCT convert(varchar(100), d.name)  as table_name,\n" +
            "                convert(varchar(100), f.value) as table_remark\n" +
            "FROM sys.syscolumns a\n" +
            "         LEFT JOIN sys.systypes b ON a.xusertype = b.xusertype\n" +
            "         INNER JOIN sys.sysobjects d ON a.id = d.id\n" +
            "    AND d.xtype = 'U'\n" +
            "    AND d.name <> 'dtproperties'\n" +
            "         LEFT JOIN sys.syscomments e ON a.cdefault = e.id\n" +
            "         LEFT JOIN sys.extended_properties g ON a.id = G.major_id\n" +
            "    AND a.colid = g.minor_id\n" +
            "         LEFT JOIN sys.extended_properties f ON d.id = f.major_id\n" +
            "    AND f.minor_id = 0\n" +
            "WHERE d.name in (%s)";

    /**
     * 更新所有表及其字段的注释信息
     */
    public static void freshTables(IntrospectedTable introspectedTable) {
        if (refreshed) {
            return;
        }
        refreshed = true;
        final Context context = introspectedTable.getContext();
        Connection connection = null;
        try {
            if (!isSqlServer(context)) {
                return;
            }
            connection = getConnection(context);
            final List<IntrospectedTable> allTables = context.getIntrospectedTables();
            final String tableNameCondition = generateSqlCondition(allTables);
            final Statement statement = connection.createStatement();
            freshTableComments(statement, allTables, tableNameCondition);
            freshTableColumnComments(allTables, tableNameCondition, statement);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 生成 where in 条件
     */
    private static String generateSqlCondition(List<IntrospectedTable> allTables) {
        return allTables.stream()
                .map(IntrospectedTable::getTableConfiguration)
                .map(TableConfiguration::getTableName)
                .map(tableName -> "'" + tableName + "'")
                .collect(Collectors.joining(","));
    }

    /**
     * 是否为 sqlServer
     */
    private static boolean isSqlServer(Context context) throws NoSuchFieldException, IllegalAccessException {
        final java.lang.reflect.Field filed = Context.class.getDeclaredField("jdbcConnectionConfiguration");
        filed.setAccessible(true);
        final JDBCConnectionConfiguration jdbcConnectionConfiguration = (JDBCConnectionConfiguration) filed.get(context);
        return jdbcConnectionConfiguration.getDriverClass().endsWith("SQLServerDriver");
    }

    /**
     * 更新字段注释
     */
    private static void freshTableColumnComments(List<IntrospectedTable> allTables, String tableNameCondition, Statement statement) throws SQLException {
        final ResultSet resultSet = statement.executeQuery(String.format(TABLE_COLUMN_COMMENT_SQL, tableNameCondition));
        while (resultSet.next()) {
            final String tableName = resultSet.getString("table_name");
            final String columnName = resultSet.getString("column_name");
            final String columnRemark = resultSet.getString("column_remark");
            if (!StringUtil.isEmpty(columnRemark)) {
                for (IntrospectedTable table : allTables) {
                    if (table.getTableConfiguration().getTableName().equals(tableName)) {
                        final Optional<IntrospectedColumn> column = table.getColumn(columnName);
                        column.ifPresent(introspectedColumn -> introspectedColumn.setRemarks(columnRemark));
                        break;
                    }
                }
            }
        }
    }

    /**
     * 更新表的注释
     */
    private static void freshTableComments(Statement statement, List<IntrospectedTable> allTables, String tableNameCondition) throws SQLException {
        final ResultSet resultSet = statement.executeQuery(String.format(TABLE_COMMENT_SQL, tableNameCondition));
        while (resultSet.next()) {
            final String tableName = resultSet.getString("table_name");
            final String tableRemark = resultSet.getString("table_remark");
            if (!StringUtil.isEmpty(tableRemark)) {
                for (IntrospectedTable table : allTables) {
                    if (table.getTableConfiguration().getTableName().equals(tableName)) {
                        table.setRemarks(tableRemark);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 反射调用 context.getConnection ，获取连接
     */
    private static Connection getConnection(Context context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method getConnection = Context.class.getDeclaredMethod("getConnection");
        getConnection.setAccessible(true);
        return (Connection) getConnection.invoke(context);
    }

    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

}
