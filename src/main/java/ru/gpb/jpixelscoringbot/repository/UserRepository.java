package ru.gpb.jpixelscoringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gpb.jpixelscoringbot.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByTelegramId(Long telegramId);
}
