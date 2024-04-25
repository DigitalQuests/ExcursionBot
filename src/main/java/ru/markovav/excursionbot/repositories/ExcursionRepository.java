package ru.markovav.excursionbot.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Excursion;

public interface ExcursionRepository extends CrudRepository<Excursion, UUID> {
    Optional<Excursion> findFirstByParticipants_IdAndFinishedAtNullAndStartedAtNotNullOrderByStartedAtDesc(UUID id);

}
