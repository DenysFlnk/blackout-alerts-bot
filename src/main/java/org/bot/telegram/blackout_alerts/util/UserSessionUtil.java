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

    public static UserInfo getUserInfo(UserSession session) {
        UserInfo userInfo = new UserInfo();
        userInfo.setChatId(session.getChatId());
        userInfo.setSessionState(session.getSessionState());
        userInfo.setUserCity(session.getUserCity());
        userInfo.setUserStreet(session.getUserStreet());
        userInfo.setUserHouse(session.getUserHouse());
        return userInfo;
    }

    public static AddressEntity getAddressEntity(UserSession session) {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCity(session.getUserCity());
        addressEntity.setStreet(session.getUserStreet());
        addressEntity.setHouse(session.getUserHouse());
        addressEntity.setShutdownGroup(session.getShutdownGroup());
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

    public static void handleAddressCorrection(UserSession session) {
        Optional<AddressField> field = Arrays.stream(AddressField.values())
            .filter(addressField -> session.getText().contains(addressField.toString()))
            .findAny();

        if (field.isEmpty()) {
            return;
        }

        AddressField addressField = field.get();
        String updatedAddress = session.getText().replace(addressField.toString(), "").trim();
        log.info("Chat id: {}. Replace {} to new value {}", session.getChatId(), addressField, updatedAddress);

        switch (addressField) {
            case CITY -> session.setUserCity(updatedAddress);
            case STREET -> session.setUserStreet(updatedAddress);
            case HOUSE -> session.setUserHouse(updatedAddress);
            default -> log.warn("Address field {} not found", addressField);
        }

        session.setText("/" + session.getSessionState().toString().toLowerCase());
    }
}
