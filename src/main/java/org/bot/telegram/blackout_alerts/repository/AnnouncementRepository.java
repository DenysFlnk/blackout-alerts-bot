package org.bot.telegram.blackout_alerts.repository;

import java.util.List;
import org.bot.telegram.blackout_alerts.model.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    @Query("SELECT a FROM Announcement a WHERE a.isAnnounced=false")
    List<Announcement> findAllNotAnnounced();
}
