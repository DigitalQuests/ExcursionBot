package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "answer_variants")
public class AnswerVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "text", nullable = false)
  private String text;

  @Column(name = "is_correct", nullable = false)
  private Boolean isCorrect;

  @ManyToOne(optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;
}
