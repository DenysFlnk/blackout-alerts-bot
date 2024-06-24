package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bot.telegram.blackout_alerts.model.session.SessionState;

@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
public class UserInfo {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "session_state")
    @Enumerated(EnumType.STRING)
    private SessionState sessionState;

    @Column(name = "user_city")
    private String userCity;

    @Column(name = "user_street")
    private String userStreet;

    @Column(name = "user_house")
    private String userHouse;
}
