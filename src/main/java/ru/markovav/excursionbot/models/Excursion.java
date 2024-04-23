package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

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
@Table(name = "excursions")
public class Excursion {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "route_id", nullable = false)
  private Route route;

  @ManyToOne(optional = false)
  @JoinColumn(name = "guide_id", nullable = false)
  private User guide;

  @ManyToMany
  @JoinTable(
      name = "excursions_participants",
      joinColumns = @JoinColumn(name = "excursion_id"),
      inverseJoinColumns = @JoinColumn(name = "participants_id"))
  private Set<User> participants = new LinkedHashSet<>();
}
