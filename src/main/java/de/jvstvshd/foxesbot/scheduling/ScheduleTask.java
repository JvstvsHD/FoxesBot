package de.jvstvshd.foxesbot.scheduling;

import java.util.UUID;

public interface ScheduleTask {

    void cancel();

    UUID getUuid();
}
