package de.jvstvshd.foxesbot.scheduling;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.jvstvshd.foxesbot.FoxesBot;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class FoxesScheduler implements Scheduler {

    private final ScheduledExecutorService schedulerService;
    private final ExecutorService taskService;
    private final FoxesBot bot;
    private final Set<ScheduleTask> tasks = Sets.newConcurrentHashSet();

    private final PriorityQueue<FoxesTask> queue = new PriorityQueue<>(10);

    public FoxesScheduler(FoxesBot bot) {
        this.bot = bot;
        this.schedulerService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Chilling Foxes Task Scheduler Timer").build());
        this.taskService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Chilling Foxes Task Scheduler - #%d").build());
    }

    @Override
    public ScheduleTask run(Runnable task) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask runAsync(Runnable task) {
        return build(new FoxesTask(task, UUID.randomUUID(), 0L, 0L)).schedule();
    }

    @Override
    public ScheduleTask delay(Runnable task, long delay) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask delay(Runnable task, long delay, TimeUnit timeUnit) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask delayAsync(Runnable task, long delay) {
        return delayAsync(task, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduleTask delayAsync(Runnable task, long delay, TimeUnit timeUnit) {
        return build(new FoxesTask(task, UUID.randomUUID(), timeUnit.toMillis(delay), 0L)).schedule();
    }

    @Override
    public ScheduleTask schedule(Runnable task, long period) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask schedule(Runnable task, long period, TimeUnit timeUnit) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask schedule(Runnable task, long period, long initialDelay) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask schedule(Runnable task, long period, long initialDelay, TimeUnit timeUnit) {
        handleUnsupportedSyncOperation();
        return null;
    }

    @Override
    public ScheduleTask scheduleAsync(Runnable task, long period) {
        return scheduleAsync(task, period, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduleTask scheduleAsync(Runnable task, long period, TimeUnit timeUnit) {
        return scheduleAsync(task, period, 0L, timeUnit);
    }

    @Override
    public ScheduleTask scheduleAsync(Runnable task, long period, long initialDelay) {
        return scheduleAsync(task, period, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduleTask scheduleAsync(Runnable task, long period, long initialDelay, TimeUnit timeUnit) {
        return build(new FoxesTask(task, UUID.randomUUID(), timeUnit.toMillis(initialDelay), timeUnit.toMillis(period))).schedule();
    }

    @Override
    public void cancelTask(UUID id) {
        tasks.stream().filter(scheduleTask -> scheduleTask.getUuid().equals(id)).findFirst().ifPresent(ScheduleTask::cancel);
    }

    private FoxesTask build(FoxesTask task) {
        return task;
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        ImmutableList<ScheduleTask> immutableList;
        synchronized (this.tasks) {
            immutableList = ImmutableList.copyOf(this.tasks);
        }
        for (ScheduleTask scheduleTask : immutableList) {
            scheduleTask.cancel();
        }
        this.schedulerService.shutdown();
        this.taskService.shutdown();
        return this.taskService.awaitTermination(2L, TimeUnit.SECONDS);
    }

    public class FoxesTask implements ScheduleTask, Runnable {

        private final Runnable task;
        private final UUID id;
        private final long delay;

        private long period;
        private ScheduledFuture<?> future;
        private volatile Thread currentThread;

        public FoxesTask(Runnable task, UUID id, long delay, long period) {
            this.task = task;
            this.id = id;
            this.delay = delay;
            this.period = period;
        }

        @Override
        public void cancel() {
            if (this.future != null) {
                this.future.cancel(false);
                Thread cur = this.currentThread;
                if (cur != null)
                    cur.interrupt();
                onFinish();
            }
        }

        @Override
        public void run() {
            FoxesScheduler.this.taskService.execute((() -> {
                this.currentThread = Thread.currentThread();
                try {
                    this.task.run();
                } catch (Throwable e) {
                    FoxesScheduler.this.bot.getLogger().error("Exception in task {}", this.task, e);
                } finally {
                    if (this.period == 0L) {
                        onFinish();
                    }
                    this.currentThread = null;
                }
            }));
        }

        public UUID getUuid() {
            return id;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public FoxesTask schedule() {
            FoxesScheduler.this.tasks.add(this);
            if (this.period == 0L) {
                this.future = FoxesScheduler.this.schedulerService.schedule(this, delay, TimeUnit.MILLISECONDS);
            } else {
                this.future = FoxesScheduler.this.schedulerService.scheduleAtFixedRate(this, this.delay, this.period, TimeUnit.MILLISECONDS);
            }
            return this;
        }

        private void onFinish() {
            FoxesScheduler.this.tasks.remove(this);
        }
    }

    private void handleUnsupportedSyncOperation() {
        throw new UnsupportedOperationException("Sync execution is currently disabled, coming soon....");
    }
}
