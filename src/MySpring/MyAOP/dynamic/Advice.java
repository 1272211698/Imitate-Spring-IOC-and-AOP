package MySpring.MyAOP.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Advice implements InvocationHandler {
    private Object bean;
    private Object invocationBean;
    private Method beforeMethod;
    private String beforeName;
    private Method afterMethod;
    private String afterName;

    public Advice(Object bean, Object invocationBean, Method beforeMethod, String beforeName, Method afterMethod, String afterName) {
        this.bean = bean;
        this.invocationBean = invocationBean;
        this.beforeMethod = beforeMethod;
        this.beforeName = beforeName;
        this.afterMethod = afterMethod;
        this.afterName = afterName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String invocationClass = bean.getClass().getSimpleName();
        String beforeClass = null;
        String afterClass = null;
        String beforeBeanMethod = null;
        String afterBeanMethod = null;
        String[] beforeArr;
        String[] afterArr;
        if(beforeName != null) {
            beforeArr = beforeName.split("\\.");
            for (String before : beforeArr) {
                if(before.equals(invocationClass)){
                    beforeClass = before;
                }
            }
            beforeBeanMethod = beforeArr[beforeArr.length - 1];
        }
        if(afterName != null) {
            afterArr = afterName.split("\\.");
            for (String after : afterArr) {
                if(after.equals(invocationClass)){
                    afterClass = after;
                }
            }
            afterBeanMethod = afterArr[afterArr.length - 1];
        }
        if(beforeMethod != null && beforeClass != null && (beforeBeanMethod != null && (beforeBeanMethod.equals("*") || beforeBeanMethod.equals(method.getName())))){
            beforeMethod.invoke(invocationBean);
        }
        method.invoke(bean,args);
        if(afterMethod != null && afterClass != null && (afterBeanMethod != null && (afterBeanMethod.equals("*") || afterBeanMethod.equals(method.getName())))){
            afterMethod.invoke(invocationBean);
        }
        return null;
    }
}
