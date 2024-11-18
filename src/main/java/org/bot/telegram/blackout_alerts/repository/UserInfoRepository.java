package org.bot.telegram.blackout_alerts.repository;

import java.util.List;
import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    @Query("SELECT us.chatId FROM UserInfo us")
    List<Long> getAllUserIds();
}
