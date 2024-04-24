package ru.markovav.excursionbot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "telegram_id", nullable = false, unique = true)
  private Long telegramId;

  @Column(name = "username")
  private String username;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 32)
  private Role role;

  @ManyToMany(mappedBy = "participants")
  @OrderBy("createdAt")
  private List<Excursion> excursions = new ArrayList<>();

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy hp
            ? hp.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy hp
            ? hp.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    if (!(o instanceof User user)) return false;
    return getId() != null && Objects.equals(getId(), user.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hp
        ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }

  @Override
  public String toString() {
    if (username != null) {
      return "@" + username;
    }
    return "<a href=\"tg://user?id=" + telegramId + "\">" + firstName + (lastName == null ? "" : " " + lastName) + "</a>";
  }

  public record ResultsStorage(int solved, int mistakes, int hints, Instant lastSolvedAt, User user)
      implements Comparable<ResultsStorage> {
    @Override
    public int compareTo(@NotNull ResultsStorage o) {
      // Firstly compare by solved DESC, then by mistakes ASC, then by hints ASC, then by lastSolvedAt ASC
      int solvedComparison = Integer.compare(o.solved, solved);
      if (solvedComparison != 0) {
        return solvedComparison;
      }
      int mistakesComparison = Integer.compare(mistakes, o.mistakes);
      if (mistakesComparison != 0) {
        return mistakesComparison;
      }
      int hintsComparison = Integer.compare(hints, o.hints);
      if (hintsComparison != 0) {
        return hintsComparison;
      }
      return lastSolvedAt.compareTo(o.lastSolvedAt);
    }

    @Override
    public String toString() {
      return user.toString() + " (" + solved + ", " + mistakes + ", " + hints + ")";
    }
  }
}
