package org.bot.telegram.blackout_alerts.util;

import org.bot.telegram.blackout_alerts.model.entity.AddressEntity;
import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.bot.telegram.blackout_alerts.model.session.Address;
import org.bot.telegram.blackout_alerts.model.session.UserSession;

public class UserSessionUtil {

    private UserSessionUtil() {
    }

    public static String parseStreet(String street) {
        String[] split = street.split(" ");
        return split.length > 1 ? split[split.length - 1] : street;
    }

    public static String parseHouseNumber(String houseNumber) {
        return houseNumber.replace("\\", "/")
            .replace(",", " ")
            .replaceAll("[a-zA-Z]", " ")
            .split(" ")[0];
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
}
