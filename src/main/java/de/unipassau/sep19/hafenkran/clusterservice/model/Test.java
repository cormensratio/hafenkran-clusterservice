package de.unipassau.sep19.hafenkran.clusterservice.model;

import javax.persistence.*;

@Entity
@Table(name = "testdetails", schema = "hafenkran")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Test() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
