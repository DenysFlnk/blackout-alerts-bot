package org.bot.telegram.blackout_alerts.util;

import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bot.telegram.blackout_alerts.exception.address.AddressField;
import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.bot.telegram.blackout_alerts.model.session.Address;
import org.bot.telegram.blackout_alerts.model.session.UserSession;

@Slf4j
public class UserSessionUtil {

    private UserSessionUtil() {
    }

    public static UserSession getUserSession(UserInfo userInfo) {
        Address address = new Address();
        address.setCity(userInfo.getUserCity());
        address.setStreet(userInfo.getUserStreet());
        address.setHouse(userInfo.getUserHouse());

        UserSession session = new UserSession(userInfo.getChatId());
        session.setAddress(address);
        session.setSessionState(userInfo.getSessionState());
        return session;
    }

    public static UserInfo getUserInfo(UserSession userSession) {
        UserInfo userInfo = new UserInfo();
        userInfo.setChatId(userSession.getChatId());
        userInfo.setSessionState(userSession.getSessionState());
        userInfo.setUserCity(userSession.getUserCity());
        userInfo.setUserStreet(userSession.getUserStreet());
        userInfo.setUserHouse(userSession.getUserHouse());
        return userInfo;
    }

    public static AddressEntity getAddressEntity(UserSession userSession) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCity(userSession.getUserCity());
        addressEntity.setStreet(userSession.getUserStreet());
        addressEntity.setHouse(userSession.getUserHouse());
        addressEntity.setShutdownGroup(userSession.getShutdownGroup());
        return addressEntity;
    }

    public static Address getAddress(AddressEntity addressEntity) {
        Address address = new Address();
        address.setCity(addressEntity.getCity());
        address.setStreet(addressEntity.getStreet());
        address.setHouse(addressEntity.getHouse());
        address.setShutdownGroup(addressEntity.getShutdownGroup());
        return address;
    }

    public static void handleAddressCorrection(UserSession userSession) {
        Optional<AddressField> field = Arrays.stream(AddressField.values())
            .filter(addressField -> userSession.getText().contains(addressField.toString()))
            .findAny();

        if (field.isEmpty()) {
            return;
        }

        AddressField addressField = field.get();
        String updatedAddress = userSession.getText().replace(addressField.toString(), "").trim();
        log.info("Chat id: {}. Replace {} to new value {}", userSession.getChatId(), addressField, updatedAddress);

        switch (addressField) {
            case CITY -> userSession.setUserCity(updatedAddress);
            case STREET -> userSession.setUserStreet(updatedAddress);
            case HOUSE -> userSession.setUserHouse(updatedAddress);
            default -> log.warn("Address field {} not found", addressField);
        }

        userSession.setText("/" + userSession.getSessionState().toString().toLowerCase());
    }
}
