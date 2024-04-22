package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Excursion;

import java.util.UUID;

public interface ExcursionRepository extends CrudRepository<Excursion, UUID> {
}
