package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Hint;
import ru.markovav.excursionbot.models.Route;

import java.util.UUID;

public interface HintRepository extends CrudRepository<Hint, UUID> {
}
