package ru.markovav.excursionbot.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import ru.markovav.excursionbot.models.User;

public interface UserRepository extends CrudRepository<User, UUID> {
  Optional<User> findByTelegramId(Long telegramId);
}
