package MySpring;

import MySpring.MyAOP.pointcut.AnnotationPointCut;
import MySpring.MyIOC.context.ClassPathXmlApplicationContext;
import MySpring.pojo.User;
import MySpring.service.UserService;

public class MyTest {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("MySpring/Resource/applicationContext.xml");
        User user1 = (User) context.getBean("user");
        System.out.println(user1.toString());
        User user2 = (User) context.getBean("user");
        System.out.println(user2.toString());
        System.out.println(user1 == user2);
    }
}
