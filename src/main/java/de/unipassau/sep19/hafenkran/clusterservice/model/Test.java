package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "testdetails")
@Entity
public class Test {

    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    public Test() {
    }
}
