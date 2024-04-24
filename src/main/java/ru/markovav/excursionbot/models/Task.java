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
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "index", nullable = false)
  private Integer index;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "text", nullable = false)
  private String text;

  @OneToMany(mappedBy = "task", orphanRemoval = true)
  @OrderBy("index")
  private List<Hint> hints = new ArrayList<>();

  @OneToMany(mappedBy = "task", orphanRemoval = true)
  @OrderBy("text")
  private List<AnswerVariant> answerVariants = new ArrayList<>();

  @ManyToOne(optional = false)
  @JoinColumn(name = "excursion_route_id", nullable = false)
  private Route route;
}
