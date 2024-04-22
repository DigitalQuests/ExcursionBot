package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "welcome_message", nullable = false)
    private String welcome_message;

    @OneToMany(mappedBy = "route", orphanRemoval = true)
    @OrderBy("order")
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "route", orphanRemoval = true)
    private Set<Excursion> excursions = new LinkedHashSet<>();

}
