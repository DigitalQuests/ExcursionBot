package ru.markovav.excursionbot.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.AnswerVariant;

import java.util.UUID;

public interface AnswerVariantRepository extends CrudRepository<AnswerVariant, UUID> {}
