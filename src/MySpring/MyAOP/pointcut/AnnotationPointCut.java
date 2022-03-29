package MySpring.MyAOP.pointcut;

import MySpring.MyAOP.anno.After;
import MySpring.MyAOP.anno.Aspect;
import MySpring.MyAOP.anno.Before;
import MySpring.MyAOP.anno.Pointcut;

@Aspect
public class AnnotationPointCut {
    @Pointcut("MySpring.service.UserServiceImpl.*")
    public void point(){

    }
    @Before("point()")
    public void before(){
        System.out.println("方法执行前");
    }

    @After("point()")
    public void after(){
        System.out.println("方法执行后");
    }
}
