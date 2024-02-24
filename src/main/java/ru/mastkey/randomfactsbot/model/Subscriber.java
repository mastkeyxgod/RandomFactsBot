package ru.mastkey.randomfactsbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private Long chatId;

    private Time time;

    public Subscriber(Long chatId, Time time) {
        this.chatId = chatId;
        this.time = time;
    }


}
