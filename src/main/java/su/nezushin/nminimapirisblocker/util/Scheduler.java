package su.nezushin.nminimapirisblocker.util;

public interface Scheduler {



    void cancelAllTasks();

    RunningTask async(Runnable run, long delay, long period);

    RunningTask async(Runnable run, long delay);

    void sync(Runnable run);

    boolean isFolia();
}
