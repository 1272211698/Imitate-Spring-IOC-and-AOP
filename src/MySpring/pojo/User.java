package MySpring.pojo;

import MySpring.MyIOC.anno.*;

import java.util.Arrays;

/**
 * @author 张梓毅
 */
@Component("user")
@Scope("singleton")
public class User {
    @Value("zzy")
    private String name;
    @Value("20")
    private int age;
    private int[] scores;
    private String[] subjects;
    @Autowired
    @Qualifier("java")
    private Book book;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int[] getScores() {
        return scores;
    }

    public void setScores(int[] scores) {
        this.scores = scores;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public void setSubjects(String[] subjects) {
        this.subjects = subjects;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", scores=" + Arrays.toString(scores) +
                ", subjects=" + Arrays.toString(subjects) +
                ", book=" + book +
                '}';
    }
}
