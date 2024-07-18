package org.bot.telegram.blackout_alerts.service;

import static org.bot.telegram.blackout_alerts.util.UserSessionUtil.getAddress;
import static org.bot.telegram.blackout_alerts.util.UserSessionUtil.getAddressEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.session.Address;
import org.bot.telegram.blackout_alerts.model.session.UserSession;
import org.bot.telegram.blackout_alerts.repository.AddressEntityRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AddressService {

    private final AddressEntityRepository addressRepository;

    public Optional<Address> getAddressFromDb(UserSession userSession) {
        List<AddressEntity> addresses = addressRepository.findAllByCityContainsAndStreetContainsAndHouse(
            userSession.getUserCity(), userSession.getUserStreet(), userSession.getUserHouse());

        if (addresses.isEmpty()) {
            return Optional.empty();
        }

        if (addresses.size() > 1) {
            log.info("Chat id: {}. Found more then one address in DB. Getting latest", userSession.getChatId());
            List<AddressEntity> sortedAddresses = addresses.stream()
                .sorted(Comparator.comparingInt(AddressEntity::getId))
                .collect(Collectors.toList());

            AddressEntity result = sortedAddresses.remove(sortedAddresses.size() - 1);

            sortedAddresses.forEach(address -> addressRepository.deleteById(address.getId()));

            log.info("Latest address: {}, {}, {}", result.getCity(), result.getStreet(), result.getHouse());
            return Optional.of(getAddress(result));
        } else {
            return Optional.of(getAddress(addresses.get(0)));
        }
    }

    public void updateAddressInDb(UserSession userSession) {
        AddressEntity address = getAddressEntity(userSession);
        addressRepository.findByCityAndStreetAndHouse(address.getCity(), address.getStreet(), address.getHouse())
            .ifPresent(addressEntity -> address.setId(addressEntity.getId()));
        addressRepository.save(address);
    }
}
