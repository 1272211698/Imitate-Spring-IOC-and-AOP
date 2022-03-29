package MySpring.MyIOC.context;

import MySpring.MyAOP.anno.After;
import MySpring.MyAOP.anno.Aspect;
import MySpring.MyAOP.anno.Before;
import MySpring.MyAOP.anno.Pointcut;
import MySpring.MyAOP.dynamic.Advice;
import MySpring.MyAOP.dynamic.AopProxy;
import MySpring.MyIOC.scan.Scan;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @MethodName:
 * @Description: 基于配置文件和注解的IOC
 * @Author: zzy
 * @Date: 2022/3/29 13:41
 * @Param:
 * @Return:
 */
public class ClassPathXmlApplicationContext extends Scan {
    private String classpath;

    public ClassPathXmlApplicationContext(String classpath) {
        this.classpath = Objects.requireNonNull(Scan.class.getClassLoader().getResource(classpath)).getFile();
    }

    /**
     * @MethodName: getBean
     * @Description: 提供IOC服务的方法
     * @Author: zzy
     * @Date: 2022/3/29 23:54
     * @Param: [beanName]
     * @Return: java.lang.Object
     */
    public Object getBean(String beanName) throws Exception {
        putBeanDefinitionFromXml(classpath);
        return super.getBeanByName(beanName);
    }

    /**
     * @MethodName: getBean
     * @Description: 为IOC提供AOP服务的方法
     * @Author: zzy
     * @Date: 2022/3/29 23:54
     * @Param: [beanName, cl]
     * @Return: java.lang.Object
     */
    public Object getBean(String beanName, Class cl) throws Exception {
        String pointcutName = null;
        Method pointcutMethod = null;
        Method beforeMethod = null;
        Method afterMethod = null;
        String beforeName = null;
        String afterName = null;
        //通过反射获取注解
        Aspect aspect = (Aspect) cl.getAnnotation(Aspect.class);
        if (aspect == null) {
            throw new IllegalArgumentException("该类不是切面类");
        }
        Method[] methods = cl.getDeclaredMethods();
        for (Method method : methods) {
            Pointcut pointcut = method.getAnnotation(Pointcut.class);
            if (pointcut != null) {
                pointcutMethod = method;
                pointcutName = pointcut.value();
                break;
            }
        }
        for (Method method : methods) {
            Boolean flag = null;
            String value = null;
            Before before = method.getAnnotation(Before.class);
            After after = method.getAnnotation(After.class);
            if (before != null) {
                flag = true;
                value = before.value();
                beforeMethod = method;
            }else if(after != null){
                flag = false;
                value = after.value();
                afterMethod = method;
            }
            if (flag != null && pointcutMethod != null && (value.equals(pointcutMethod.getName() + "()"))) {
                if(flag){
                    beforeName = pointcutName;
                }else{
                    afterName = pointcutName;
                }
            }else{
                if(flag != null){
                    if (flag) {
                        beforeName = value;
                    }else{
                        afterName = value;
                    }
                }
            }
        }
        putBeanDefinitionFromXml(classpath);
        Object bean = super.getBeanByName(beanName);
        Advice advice = new Advice(bean,cl.newInstance(),beforeMethod,beforeName,afterMethod,afterName);
        Object beanPorxy = AopProxy.getProxy(bean,advice);
        return beanPorxy;
    }
}
