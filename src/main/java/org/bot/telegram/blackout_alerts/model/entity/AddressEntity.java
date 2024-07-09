package org.bot.telegram.blackout_alerts.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "address")
@Data
@NoArgsConstructor
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "house")
    private String house;

    @Column(name = "shutdown_group")
    private byte shutdownGroup;

    public AddressEntity(Integer id, String city, String street, String house, byte shutdownGroup) {
        this.id = id;
        this.city = city;
        this.street = street;
        this.house = house;
        this.shutdownGroup = shutdownGroup;
    }
}
