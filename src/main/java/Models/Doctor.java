package Models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DOCTOR")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PHONE", nullable = false)
    private String phone;

    @Column(name = "SPECIALIZATION", nullable = false)
    private String specialization;

    @Column(name = "EMPLOYMENT_DATE", nullable = false)
    private LocalDateTime employmentDate;

    public Doctor(){}
    public Doctor(String name,String phone, String specialization, LocalDateTime employmentDate){
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.employmentDate = employmentDate;
    }

    public void setId(long id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public void setSpecialization(String specialization){
        this.specialization = specialization;
    }
    public void setEmploymentDate(LocalDateTime employmentDate){
        this.employmentDate = employmentDate;
    }


    public Long getId(){return id;}
    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
    public String getSpecialization(){
        return specialization;
    }

    public LocalDateTime getEmploymentDate() {
        return employmentDate;
    }

    @Override
    public String toString() {
        return "Doctor " + name + ", tel:" + phone + ", specialization: " + specialization + ", employed: " + employmentDate;
    }
}
