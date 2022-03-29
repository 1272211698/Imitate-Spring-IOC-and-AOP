package MySpring.MyAOP.dynamic;

import java.lang.reflect.Proxy;

public class AopProxy {
    public static Object getProxy(Object bean, Advice advice) {
        return Proxy.newProxyInstance(AopProxy.class.getClassLoader(),
                bean.getClass().getInterfaces(), advice);
    }
}
