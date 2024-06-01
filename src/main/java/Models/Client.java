package Models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CLIENT")
@Inheritance(strategy = InheritanceType.JOINED)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "AGE", nullable = false)
    private int age;

    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "INSURANCE_ID", referencedColumnName = "id")
    private Insurance insurance;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Treatment> treatments = new ArrayList<>();

    public Client() {}

    public Client(String name, int age, String phoneNumber, Insurance insurance) {
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.insurance = insurance;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setInsurance(Insurance insurance){
        this.insurance = insurance;
    }

    public void setAge(int age){
        this.age = age;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public Long getId() {
        return id;
    }

    public double billTotal(){
        double total = 0;
        for(Treatment t: treatments){
            total += t.getPrice();
        }

        return total;
    }

    public String getName(){
        return name;
    }
    public int getAge(){
        return age;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }

    public Insurance getInsurance(){
        return insurance;
    }


    @Override
    public String toString() {
        String insuranceInfo = (insurance != null) ? ", Insurance: " + insurance.getType() + ", ExpDate: " + insurance.getExpDate() : "";
        return "Client: " + name + ", age: "+ age + ", tel:" + phoneNumber + insuranceInfo;
    }



}

