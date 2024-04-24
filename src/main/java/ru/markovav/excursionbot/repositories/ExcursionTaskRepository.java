package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.ExcursionTask;

import java.util.List;
import java.util.UUID;

public interface ExcursionTaskRepository extends CrudRepository<ExcursionTask, UUID> {
  List<ExcursionTask> findByExcursion_IdAndParticipant_IdOrderByTask_IndexDesc(UUID excursionId, UUID participantId);
}
