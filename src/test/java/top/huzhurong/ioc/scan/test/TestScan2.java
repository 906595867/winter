package top.huzhurong.ioc.scan.test;

import top.huzhurong.ioc.annotation.Bean;
import top.huzhurong.ioc.annotation.Inject;

/**
 * @author chenshun00@gmail.com
 * @since 2018/9/8
 */
@Bean
public class TestScan2 {

    @Inject("gg")
    private TestScan3 TestScan3;

    public void hello() {
        System.out.println("----$ start invoke TestScan2's hello method $----");
        TestScan3.test3();
        System.out.println("----$ end invoke TestScan2's hello method $----");

    }
}
