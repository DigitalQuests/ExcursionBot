package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Excursion;
import ru.markovav.excursionbot.models.Route;

import java.util.UUID;

public interface RouteRepository extends CrudRepository<Route, UUID> {
}
