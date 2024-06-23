package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bot.telegram.blackout_alerts.model.session.SessionState;

@Entity
@Data
@NoArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    private SessionState sessionState;

    private String userCity;

    private String userStreet;

    private String userHouse;
}
