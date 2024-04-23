package ru.markovav.excursionbot.repositories;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.Route;

public interface RouteRepository extends CrudRepository<Route, UUID> {}
