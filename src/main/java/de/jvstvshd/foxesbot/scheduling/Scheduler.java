package de.jvstvshd.foxesbot.scheduling;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

    /**
     * Runs the <code>task</code>
     *
     * @param task Task to run
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask run(Runnable task);

    /**
     * Runs the <code>task</code> async.
     *
     * @param task Task to run
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask runAsync(Runnable task);

    /**
     * Runs the <code>task</code> after the delay.
     *
     * @param task  task to run
     * @param delay delay in milliseconds
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask delay(Runnable task, long delay);

    /**
     * Runs the <code>task</code> after the delay.
     *
     * @param task     task to run
     * @param delay    delay in timeUnit
     * @param timeUnit timeunit for the delay
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask delay(Runnable task, long delay, TimeUnit timeUnit);

    /**
     * Runs the <code>task</code> after the delay async.
     *
     * @param task  task to run
     * @param delay delay in milliseconds
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask delayAsync(Runnable task, long delay);

    /**
     * Runs the <code>task</code> after the delay async.
     *
     * @param task     task to run
     * @param delay    delay in timeUnit
     * @param timeUnit timeunit for the delay
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask delayAsync(Runnable task, long delay, TimeUnit timeUnit);

    /**
     * Runs the <code>task</code> every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task   task to run
     * @param period period in milliseconds
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask schedule(Runnable task, long period);

    /**
     * Runs the <code>task</code> every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task     task to run
     * @param period   period in milliseconds
     * @param timeUnit timeunit for the <code>period</code>
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask schedule(Runnable task, long period, TimeUnit timeUnit);

    /**
     * Runs the <code>task</code> every <code>period</code> , until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task         task to run
     * @param period       period in milliseconds
     * @param initialDelay delay before the first execution starts
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask schedule(Runnable task, long period, long initialDelay);

    /**
     * Runs the <code>task</code> every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task         task to run
     * @param period       period in milliseconds
     * @param initialDelay delay before the first execution starts
     * @param timeUnit     {@link TimeUnit} for <code>period</code> and <code>initialDelay</code>
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask schedule(Runnable task, long period, long initialDelay, TimeUnit timeUnit);

    /**
     * Runs the <code>task</code> async every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task   task to run
     * @param period period in milliseconds
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask scheduleAsync(Runnable task, long period);

    /**
     * Runs the <code>task</code> async every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task     task to run
     * @param period   period in milliseconds
     * @param timeUnit timeunit for the <code>period</code>
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask scheduleAsync(Runnable task, long period, TimeUnit timeUnit);

    /**
     * Runs the <code>task</code> async every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task         task to run
     * @param period       period in milliseconds
     * @param initialDelay initial delay in milliseconds
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask scheduleAsync(Runnable task, long period, long initialDelay);

    /**
     * Runs the <code>task</code> async every <code>period</code>, until the task is stopped through {@link ScheduleTask#cancel()}
     *
     * @param task         task to run
     * @param period       period in timeUnit
     * @param initialDelay initial delay in timeUnit
     * @return a new {@link ScheduleTask}
     */
    ScheduleTask scheduleAsync(Runnable task, long period, long initialDelay, TimeUnit timeUnit);

    /**
     * Cancels the task with the <code>id</code> if it exists.
     * @param id id of the task to cancel
     */
    void cancelTask(final UUID id);

    boolean shutdown() throws InterruptedException;
}
