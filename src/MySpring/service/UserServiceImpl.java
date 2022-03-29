package MySpring.service;


public class UserServiceImpl implements UserService{

    @Override
    public void add(int i) {
        System.out.println("添加了一条数据" + i);
    }

    @Override
    public void delete() {
        System.out.println("删除了一条数据");
    }
}
