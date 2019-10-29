package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Vector;

@Slf4j
@Entity
@Data
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonNull
    private Long id;

    @NotBlank(message = "Name is required!")
    @NonNull
    private String name;

    @NotBlank(message = "Password is required!")
    @NonNull
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
    @NonNull
    private Vector<Image> images;
}
