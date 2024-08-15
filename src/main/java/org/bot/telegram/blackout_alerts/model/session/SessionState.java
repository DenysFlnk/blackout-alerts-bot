package org.bot.telegram.blackout_alerts.model.session;

import com.google.common.collect.Sets;
import java.util.Set;

public enum SessionState {
    START,
    WAIT_FOR_CITY,
    WAIT_FOR_STREET,
    WAIT_FOR_HOUSE_NUMBER,
    ADDRESS_ACQUIRED,
    CHECK_SHUTDOWN_STATUS,
    TODAY_SCHEDULE,
    WEEK_SCHEDULE,
    WAIT_FOR_QUESTION,
    QUESTION_ASKED,
    ANSWER_TO_USER,
    QUESTION_ANSWERED;

    public static final Set<SessionState> WAIT_FOR_INPUTS = Sets.immutableEnumSet(WAIT_FOR_CITY, WAIT_FOR_STREET,
        WAIT_FOR_HOUSE_NUMBER);
}
