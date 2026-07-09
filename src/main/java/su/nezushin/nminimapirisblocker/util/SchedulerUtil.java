package su.nezushin.nminimapirisblocker.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;

import java.util.concurrent.TimeUnit;


//Folia compatibility
public class SchedulerUtil {

    private static Scheduler scheduler;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            scheduler = new FoliaScheduler();
        } catch (ClassNotFoundException e) {
            scheduler = new SpigotScheduler();
        }
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

    public static interface RunningTask {

        public void cancel();
    }

    public static interface Scheduler {

        public void cancelAllTasks();

        public RunningTask async(Runnable run, long delay, long period);

        public void async(Runnable run, long delay);

        public void sync(Runnable run);

        public boolean isFolia();
    }

    private static class SpigotScheduler implements Scheduler {


        @Override
        public void cancelAllTasks() {
            Bukkit.getScheduler().cancelTasks(NMinimapIrisBlocker.getInstance());
        }

        @Override
        public RunningTask async(Runnable run, long delay, long period) {
            var task = Bukkit.getScheduler().runTaskTimerAsynchronously(NMinimapIrisBlocker.getInstance(), run, delay, period);
            return task::cancel;
        }

        @Override
        public void async(Runnable run, long delay) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(NMinimapIrisBlocker.getInstance(), run, delay);

        }

        @Override
        public void sync(Runnable run) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMinimapIrisBlocker.getInstance(), run);
        }

        @Override
        public boolean isFolia() {
            return false;
        }
    }

    private static class FoliaScheduler implements Scheduler {

        @Override
        public void cancelAllTasks() {
            Bukkit.getAsyncScheduler().cancelTasks(NMinimapIrisBlocker.getInstance());
            Bukkit.getGlobalRegionScheduler().cancelTasks(NMinimapIrisBlocker.getInstance());
        }

        @Override
        public RunningTask async(Runnable run, long delay, long period) {
            var task = Bukkit.getAsyncScheduler().runAtFixedRate(NMinimapIrisBlocker.getInstance(), (ScheduledTask scheduledTask) -> {
                        run.run();
                    },
                    delay * 50L, period * 50L, TimeUnit.MILLISECONDS
            );
            return task::cancel;
        }

        @Override
        public void async(Runnable run, long delay) {
            Bukkit.getAsyncScheduler().runDelayed(NMinimapIrisBlocker.getInstance(), (ScheduledTask scheduledTask) -> {
                        run.run();
                    },
                    delay * 50L, TimeUnit.MILLISECONDS
            );
        }

        @Override
        public void sync(Runnable run) {
            Bukkit.getGlobalRegionScheduler().execute(NMinimapIrisBlocker.getInstance(), run);
        }

        @Override
        public boolean isFolia() {
            return true;
        }
    }
}
