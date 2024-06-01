package Models;

import javax.persistence.*;

@Entity
@Table(name = "INSURANCE")
public class Insurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "EXP_DATE")
    private String expDate;

    public Insurance() {}

    public Insurance(String type, String expDate) {
        this.type = type;
        this.expDate = expDate;
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }
}
