package net.zylesh.dystellarcore.utils;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;

import java.util.Iterator;

public class Scheduler {

    public static <T> void splitIteration(Iterable<T> collection, GenericRunnable<T> task, int maxIterationsPerTick) {
        Iterator<T> iterator = collection.iterator();
        Bukkit.getScheduler().runTaskTimer(DystellarCore.getInstance(), () -> {
            if (iterator.hasNext()) {
                next(iterator, task, maxIterationsPerTick);
            }
        }, 0L, 1L);
    }

    private static <T> void next(Iterator<T> iterator, GenericRunnable<T> task, int maxIterationsPerTick) {
        int index = 0;
        while (iterator.hasNext() && index < maxIterationsPerTick) {
            index++;
            T next = iterator.next();
            task.run(next);
        }
    }
}
