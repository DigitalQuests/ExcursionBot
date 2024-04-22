package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order", nullable = false)
    private Integer order;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "text", nullable = false)
    private String text;

    @OneToMany(mappedBy = "task", orphanRemoval = true)
    @OrderBy("order")
    private List<Hint> hints = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "excursion_route_id", nullable = false)
    private Route route;
}
