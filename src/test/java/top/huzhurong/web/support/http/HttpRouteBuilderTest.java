package top.huzhurong.web.support.http;

import org.junit.Before;
import org.junit.Test;
import top.huzhurong.ioc.bean.ClassInfo;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author luobo.cs@raycloud.com
 * @since 2018/9/20
 */
public class HttpRouteBuilderTest {

    private HttpRouteBuilder httpRouteBuilder;

    @Before
    public void before() {
        httpRouteBuilder = new HttpRouteBuilder();
    }

    @Test
    public void buildRoute() {
        UserCrtl userCrtl = new UserCrtl();
        ClassInfo classInfo = new ClassInfo(userCrtl.getClass(), "userCrtl");
        List<Route> routeList = httpRouteBuilder.buildRoute(classInfo, userCrtl);

        assertEquals(4, routeList.size());


        routeList.forEach(System.out::println);
        System.out.println();
    }
}