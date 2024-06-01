package Models;

import javax.persistence.Entity;

@Entity
public class Student extends Client {

    public Student() {}

    public Student(String name, int age, String phoneNumber, Insurance insurance) {
        super(name, age, phoneNumber, insurance);
    }

//    @Override
//    public double billTotal() {
//        double total = 0;
//        for (Treatment t : getTreatments()) {
//            total += t.getPrice() * 0.5;
//        }
//        return total;
//    }
}
