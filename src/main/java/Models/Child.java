package Models;

import javax.persistence.Entity;

@Entity
public class Child extends Client {

    public Child() {}

    public Child(String name, int age, String phoneNumber, Insurance insurance) {
        super(name, age, phoneNumber, insurance);
    }



//    @Override
//    public double billTotal() {
//        double total = 0;
//        for (Treatment t : getTreatments()) {
//            total += t.getPrice() * 0.8;
//        }
//        return total;
//    }
}
