package ru.mastkey.randomfactsbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mastkey.randomfactsbot.model.Subscriber;

import java.sql.Time;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Subscriber getSubscriberByChatId(Long chatId);

    List<Subscriber> getSubscribersByTime(Time time);

}
