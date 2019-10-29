package de.unipassau.sep19.hafenkran.clusterservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDateTime;

@Slf4j
@Entity
@Data
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private String repository;

    @NonNull
    private String imageId;

    @NonNull
    @Basic
    private LocalDateTime timestamp;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @NonNull
    private int size;
}
