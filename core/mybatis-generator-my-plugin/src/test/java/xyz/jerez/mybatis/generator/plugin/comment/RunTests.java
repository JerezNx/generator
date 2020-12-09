package xyz.jerez.mybatis.generator.plugin.comment;

import org.junit.Test;
import org.mybatis.generator.api.ShellRunner;

/**
 * @author liqilin
 * @since 2020/12/8 14:55
 */

public class RunTests {

    @Test
    public void test() {
        String[] args = new String[]{"-configfile", "C:\\work\\mybatis-generator\\generatorEntryConfig.xml", "-overwrite"};
        ShellRunner.main(args);
    }

}
