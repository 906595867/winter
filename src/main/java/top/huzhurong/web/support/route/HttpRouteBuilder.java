package top.huzhurong.web.support.route;

import top.huzhurong.util.StringUtil;
import top.huzhurong.web.annotation.Json;
import top.huzhurong.web.annotation.PathVariable;
import top.huzhurong.web.annotation.RequestMapping;
import top.huzhurong.web.annotation.RequestParam;
import top.huzhurong.web.asm.AsmParameterNameDiscover;
import top.huzhurong.web.asm.ParameterNameDiscoverer;
import top.huzhurong.web.support.http.RequestMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author luobo.cs@raycloud.com
 * @since 2018/9/20
 */
public class HttpRouteBuilder {

    private static final Map<String, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);
    private static final Pattern PATTERN = Pattern.compile("\\{(?<name>.*?)}");

    static {
        primitiveWrapperTypeMap.put("boolean", Boolean.class);
        primitiveWrapperTypeMap.put("int", Integer.class);
        primitiveWrapperTypeMap.put("char", Character.class);
        primitiveWrapperTypeMap.put("double", Double.class);
        primitiveWrapperTypeMap.put("float", Float.class);
        primitiveWrapperTypeMap.put("byte", Byte.class);
        primitiveWrapperTypeMap.put("long", Long.class);
        primitiveWrapperTypeMap.put("short", Short.class);
    }

    private ParameterNameDiscoverer parameterNameDiscoverer = new AsmParameterNameDiscover();

    /**
     * 基本类型靠parse，自定义类型，就只能反射set了
     */
    public List<Route> buildRoute(Object instance) {
        List<Route> routeList = new LinkedList<>();
        RequestMapping requestMapping = instance.getClass().getDeclaredAnnotation(RequestMapping.class);
        Method[] declaredMethods = instance.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (Modifier.isPublic(declaredMethod.getModifiers()) && !Modifier.isStatic(declaredMethod.getModifiers())) {
                RequestMapping declaredAnnotation = declaredMethod.getDeclaredAnnotation(RequestMapping.class);
                if (declaredAnnotation != null) {
                    Json json = declaredMethod.getDeclaredAnnotation(Json.class);
                    RequestMethod[] requestMethods = declaredAnnotation.method();

                    List<String> tags = new LinkedList<>();

                    String parent = requestMapping.value();
                    if (parent.trim().length() != 0 && !parent.startsWith("/")) {
                        parent = "/" + parent;
                    }

                    String child = declaredAnnotation.value();
                    if (StringUtil.containSpace(child)) {
                        throw new RuntimeException("class:" + instance.getClass().getName() +
                                "--RequestMapping#value can't be null or contain writeSpace");
                    }
                    if (!child.startsWith("/")) {
                        child = "/" + child;
                    }

                    String key = parent + child;
                    if (requestMethods.length == 0) {
                        tags.add((key + "#" + "post".toUpperCase()));
                        tags.add((key + "#" + "get".toUpperCase()));
                        tags.add((key + "#" + "put".toUpperCase()));
                        tags.add((key + "#" + "delete".toUpperCase()));
                    } else {
                        for (RequestMethod requestMethod : requestMethods) {
                            String name = requestMethod.name();
                            String tag = (key + "#" + name.toUpperCase());
                            tags.add(tag);
                        }
                    }

                    Parameter[] parameters = declaredMethod.getParameters();
                    for (String tag : tags) {
                        Route route = new Route();
                        Map<String, Class<?>> routeParameters = route.getParameters();
                        Map<String, String> parameterNames = parameterNameDiscoverer.getParameterNames(declaredMethod);
                        int i = 0;
                        for (Map.Entry<String, String> entry : parameterNames.entrySet()) {
                            if (primitiveWrapperTypeMap.get(entry.getKey()) == null) {
                                try {
                                    String nname;
                                    Class<?> aClass = Class.forName(entry.getValue());
                                    if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                                        RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
                                        nname = requestParam.value();
                                    } else if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                                        PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
                                        nname = pathVariable.value();
                                    } else {
                                        nname = entry.getKey();
                                    }
                                    i++;
                                    routeParameters.put(nname, aClass);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        route.setJson(json != null);
                        route.setTargetClass(instance.getClass());
                        route.setMethod(declaredMethod);
                        route.setTarget(instance);
                        route.setMapping(tag);
                        routeList.add(route);
                    }


                }
            }
        }
        return routeList;
    }

}
