package Models;

import javax.persistence.Entity;

@Entity
public class Senior extends Client {

    public Senior() {}

    public Senior(String name, int age, String phoneNumber, Insurance insurance) {
        super(name, age, phoneNumber, insurance);
    }

//    @Override
//    public double billTotal() {
//        double total = 0;
//        for (Treatment t : getTreatments()) {
//            total += t.getPrice() * 0.6;
//        }
//        return total;
//    }
}
