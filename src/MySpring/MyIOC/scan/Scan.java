package MySpring.MyIOC.scan;


import MySpring.MyIOC.anno.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: IOC核心实现类
 * @Author: zzy
 * @Date: 2022/3/29 21:11
 */
public class Scan {
    /**
     * 作为BeanDefinition容器
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    /**
     * 作为Bean单例池
     */
    private Map<String, Object> singletonMap = new HashMap<>();


    /**
     * @MethodName: putBeanDefinition
     * @Description: 将扫描到的BeanDefinition放到容器中
     * @Author: zzy
     * @Date: 2022/3/29 15:03
     * @Param: [classPath]
     * @Return: void
     */
    public void putBeanDefinitionFromXml(String classPath) throws Exception {
        NodeList nodes = Utils.scanBefore(classPath);
        //遍历nodes
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                //如果开启了注解的支持
                if ("component-scan".equals(node.getNodeName())) {
                    putBeanDefinitionByAnno(element, null);
                } else {
                    putBeanDefinitionByXml(element);
                }
            }
        }
    }

    /**
     * @MethodName: putBeanDefinitionByAnnoFromXml
     * @Description: 通过XML中的ComponentScan扫描对应包下的所有类，封装为beanDefinition放到beanDefinitionMap中
     * @Author: zzy
     * @Date: 2022/3/29 20:06
     * @Param: [element]
     * @Return: void
     */
    public void putBeanDefinitionByAnno(Element element, Class<?> configClass) {
        String packagePath = "";
        if (element != null) {
            packagePath = element.getAttribute("base-package");
        }
        if (configClass != null) {
            if (!configClass.isAnnotationPresent(ComponentScan.class)) {
                throw new IllegalArgumentException("选择的Config类不正确！");
            }
            packagePath = configClass.getAnnotation(ComponentScan.class).value();
        }
        ClassLoader classLoader = Scan.class.getClassLoader();
        File file = Utils.getFileFromPath(packagePath, classLoader);
        //如果是文件夹，说明要获取到这个文件夹下的所有类
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                try {
                    //获取扫描的包下的类
                    String absolutePath = f.getAbsolutePath();
                    absolutePath = absolutePath.substring(absolutePath.indexOf("MySpring"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\", ".");
                    Class<?> beanClass = classLoader.loadClass(absolutePath);
                    String scopeStr = "singleton";
                    //通过反射获取到@Scope注解的value，判断是singleton还是prototype
                    if (beanClass.isAnnotationPresent(Scope.class) && "prototype".equals(beanClass.getAnnotation(Scope.class).value())) {
                        scopeStr = "prototype";

                    }
                    //如果这个类加了@Component注解
                    if (beanClass.isAnnotationPresent(Component.class)) {
                        Component componentAnnotation = beanClass.getAnnotation(Component.class);
                        String beanName = componentAnnotation.value();
                        //如果没有给@Component注解value值，默认为首字母小写
                        if ("".equals(beanName)) {
                            beanName = (char) (32 + beanClass.getSimpleName().charAt(0)) + beanClass.getSimpleName().substring(1);
                        }
                        //封装为BeanDefinition
                        BeanDefinition beanDefinition = new BeanDefinition(beanClass, scopeStr);
                        //将BeanDefinition加到BeanDefinitionMap中
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @MethodName: putBeanDefinitionByXml
     * @Description: 扫描XML配置文件下的所有bean，封装为beanDefinition放到beanDefinitionMap中
     * @Author: zzy
     * @Date: 2022/3/29 20:09
     * @Param: [element]
     * @Return: void
     */
    private void putBeanDefinitionByXml(Element element) throws Exception {
        //得到bean的id和class
        String beanName = element.getAttribute("id");
        String className = element.getAttribute("class");
        //根据class路径得到Class对象
        Class<?> beanClass = null;
        try {
            beanClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        String scopeStr = "singleton";
        //通过反射获取到@Scope注解的value，判断是singleton还是prototype
        if (beanClass.isAnnotationPresent(Scope.class) && "prototype".equals(beanClass.getAnnotation(Scope.class).value())) {
            scopeStr = "prototype";

        }
        //封装为BeanDefinition
        BeanDefinition beanDefinition = new BeanDefinition(beanClass, scopeStr);
        //放到BeanDefinitionMap中
        beanDefinitionMap.put(beanName, beanDefinition);
        //如果单例池中有这个bean了，就不用再实例化了
        if(singletonMap.containsKey(beanName)){
            return;
        }
        //利用反射实例化bean对象
        Object bean = beanClass.newInstance();
        NodeList propertyList = element.getElementsByTagName("property");
        for (int j = 0; j < propertyList.getLength(); j++) {
            Node property = propertyList.item(j);
            if (property instanceof Element) {
                //得到bean的name
                String name = ((Element) property).getAttribute("name");
                //可能是数组注入
                Node arrayNode = ((Element) property).getElementsByTagName("array").item(0);
                //利用反射得到这个属性和可访问权限
                Field field = bean.getClass().getDeclaredField(name);
                field.setAccessible(true);
                //利用反射得到set方法和可访问权限
                Method method = bean.getClass().getDeclaredMethod("set" + (char) (name.charAt(0) - 32) + name.substring(1), field.getType());
                method.setAccessible(true);
                //判断是不是数组注入
                if (arrayNode == null) {
                    //得到每个property中的value和ref
                    String value = ((Element) property).getAttribute("value");
                    String ref = ((Element) property).getAttribute("ref");
                    //判断是value还是ref
                    if (!"".equals(value) && "".equals(ref)) {
                        if ("java.lang.String".equals(field.getType().getName())) {
                            method.invoke(bean, value);
                        } else if ("int".equals(field.getType().getName())) {
                            method.invoke(bean, Integer.parseInt(value));
                        }
                    } else if (!"".equals(ref) && "".equals(value)) {
                        method.invoke(bean, getBeanByName(ref));
                    } else {
                        throw new IllegalArgumentException("id为" + beanName + "的bean配置有误");
                    }
                } else {
                    //标识是int数组还是String数组
                    boolean flag = false;
                    //得到多个value标签
                    NodeList valueList = ((Element) arrayNode).getElementsByTagName("value");
                    //根据数组类型创建数组
                    Object arr = Array.newInstance(field.getType().getComponentType(), valueList.getLength());
                    //如果是int数组，把标识改为true
                    if ("int".equals(field.getType().getComponentType().getName())) {
                        flag = true;
                    }
                    //遍历每个value节点
                    for (int k = 0; k < valueList.getLength(); k++) {
                        Node valueNode = valueList.item(k);
                        //获取value
                        String value = valueNode.getTextContent();
                        if ("".equals(value)) {
                            throw new IllegalArgumentException("id为" + beanName + "的bean配置有误");
                        }
                        if (flag) {
                            Array.set(arr, k, Integer.parseInt(value));
                        } else {
                            Array.set(arr, k, value);
                        }
                    }
                    method.invoke(bean, arr);
                }
            }
        }
        singletonMap.put(beanName, bean);
    }

    /**
     * @MethodName: createBean
     * @Description: 实例化Bean对象
     * @Author: zzy
     * @Date: 2022/3/29 15:31
     * @Param: [beanName, beanDefinition]
     * @Return: java.lang.Object
     */
    private Object createBean(BeanDefinition beanDefinition) throws IllegalAccessException {
        //通过反射实例化bean对象
        Class beanClass = beanDefinition.getType();
        Object bean = null;
        try {
            bean = beanClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回之前调用InjectField方法注入属性
        InjectField(beanClass, bean);
        return bean;
    }

    /**
     * @MethodName: InjectField
     * @Description: 注入属性
     * @Author: zzy
     * @Date: 2022/3/29 16:06
     * @Param: [beanClass, bean]
     * @Return: void
     */
    private void InjectField(Class beanClass, Object bean) throws IllegalAccessException {
        //如果这个类有@Component注解，说明要进行注入
        if (beanClass.isAnnotationPresent(Component.class)) {
            //反射获取Field
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                //根据@Value的值来注入
                if (field.isAnnotationPresent(Value.class)) {
                    Value value = field.getAnnotation(Value.class);
                    String beanValue = value.value();
                    if ("java.lang.String".equals(field.getType().getName())) {
                        field.set(bean, beanValue);
                    } else if ("int".equals(field.getType().getName())) {
                        field.set(bean, Integer.parseInt(beanValue));
                    }
                }
                //根据@Autowired和@Qualifier来注入引用对象
                if (field.isAnnotationPresent(Autowired.class)) {
                    String beanName = "";
                    if (field.isAnnotationPresent(Qualifier.class)) {
                        beanName = field.getAnnotation(Qualifier.class).value();
                    }
                    if ("".equals(beanName)) {
                        beanName = field.getName();
                    }
                    field.set(bean, getBeanByName(beanName));
                }
                //根据@Resource注解的name属性来注入
                if (field.isAnnotationPresent(Resource.class)) {
                    String beanName = field.getAnnotation(Resource.class).name();
                    field.set(bean, getBeanByName(beanName));
                }
            }
        }
    }

    /**
     * @MethodName: getBeanByScope
     * @Description: 获取bean对象
     * @Author: zzy
     * @Date: 2022/3/29 15:12
     * @Param: [name]
     * @Return: java.lang.Object
     */
    public Object getBeanByName(String beanName) throws IllegalAccessException {
        //如果beanDefinitionMap包含key为beanName的键值对
        if (beanDefinitionMap.containsKey(beanName)) {
            //获取beanDefinition
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            //判断是不是单例，如果是singleton
            if ("singleton".equals(beanDefinition.getScope())) {
                //从单例池中获取bean对象
                Object singletonBean = singletonMap.get(beanName);
                //如果这个bean对象还没有加进去，就去实例化这个bean
                if (singletonBean == null) {
                    //调用createBean方法来实例化Bean对象，注入也会在这个方法中完成
                    singletonBean = createBean(beanDefinition);
                    //放到单例池中
                    singletonMap.put(beanName, singletonBean);
                } else {
                    //如果单例池中存在bean对象，考虑到是通过配置文件注入的，还需要再注入一次
                    InjectField(beanDefinition.getType(), singletonBean);
                }
                //返回单例Bean对象
                return singletonBean;
            } else if ("prototype".equals(beanDefinition.getScope())) {
                //如果是多例，就直接实例化bean对象，返回，注入也会在这个方法中完成
                return createBean(beanDefinition);
            }
        }
        return null;
    }

}
