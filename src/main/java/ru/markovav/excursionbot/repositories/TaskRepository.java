package ru.markovav.excursionbot.repositories;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Task;

public interface TaskRepository extends CrudRepository<Task, UUID> {}
