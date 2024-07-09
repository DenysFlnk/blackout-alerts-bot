package org.bot.telegram.blackout_alerts.repository;

import java.util.List;
import java.util.Optional;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressEntityRepository extends JpaRepository<AddressEntity, Integer> {

    List<AddressEntity> findAllByCityContainsAndStreetContainsAndHouse(String city, String street, String house);

    Optional<AddressEntity> findByCityAndStreetAndHouse(String city, String street, String house);
}
