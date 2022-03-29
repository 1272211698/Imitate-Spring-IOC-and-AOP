package MySpring.MyIOC.scan;

/**
 * @MethodName:
 * @Description: //BeanDefinition，包含了类对象、作用域
 * @Author: zzy
 * @Date: 2022/3/29 13:49
 * @Param:
 * @Return:
 */
public class BeanDefinition {
    private Class type;
    private String scope;

    public BeanDefinition(Class type, String scope) {
        this.type = type;
        this.scope = scope;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
