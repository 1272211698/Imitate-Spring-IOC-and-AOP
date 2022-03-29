package MySpring.pojo;

import MySpring.MyIOC.anno.Component;
import MySpring.MyIOC.anno.Value;

@Component("java")
public class Book {

    @Value("1999")
    private int number;

    public Book() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    @Override
    public String toString() {
        return "Book{" +
                "number=" + number +
                '}';
    }
}
