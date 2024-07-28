package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alert_subscription")
@Data
@NoArgsConstructor
public class AlertSubscription {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @ManyToOne(fetch = FetchType.EAGER)
    private AddressEntity address;
}
