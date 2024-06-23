package org.bot.telegram.blackout_alerts.repository;

import org.bot.telegram.blackout_alerts.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}
