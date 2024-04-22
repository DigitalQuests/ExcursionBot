package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@Builder
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

    @Column(name = "welcome_message", length = 1024, nullable = false)
    private String welcome_message;

    @OneToMany(mappedBy = "route", orphanRemoval = true)
    @OrderBy("index")
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "route", orphanRemoval = true)
    private Set<Excursion> excursions = new LinkedHashSet<>();

}
