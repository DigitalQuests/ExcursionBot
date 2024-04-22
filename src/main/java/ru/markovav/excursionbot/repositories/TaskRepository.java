package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Hint;
import ru.markovav.excursionbot.models.Task;

import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {
}
