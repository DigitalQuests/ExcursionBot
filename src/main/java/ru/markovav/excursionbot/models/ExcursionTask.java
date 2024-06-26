package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "excursion_task")
public class ExcursionTask {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "excursion_id", nullable = false)
  private Excursion excursion;

  @ManyToOne(optional = false)
  @JoinColumn(name = "participant_id", nullable = false)
  private User participant;

  @ManyToOne(optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(name = "mistakes", nullable = false)
  private Integer mistakes = 0;

  @Column(name = "used_hints", nullable = false)
  private Integer usedHints = 0;

  @Column(name = "solved_at")
  private Instant solvedAt;

  @ManyToMany
  @JoinTable(name = "excursion_task_used_answers",
      joinColumns = @JoinColumn(name = "excursion_task_id"),
      inverseJoinColumns = @JoinColumn(name = "answer_variants_id"))
  private Set<AnswerVariant> usedAnswers = new LinkedHashSet<>();

}
