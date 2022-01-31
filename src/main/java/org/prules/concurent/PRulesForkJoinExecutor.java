package org.prules.concurent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class PRulesForkJoinExecutor implements PRulesExecutorService {

    private static volatile PRulesForkJoinExecutor instance;
    private static Object mutex = new Object();
    private ForkJoinPool pool = new ForkJoinPool();

    private PRulesForkJoinExecutor(){ }

    static PRulesForkJoinExecutor getInstance() {
        PRulesForkJoinExecutor result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new PRulesForkJoinExecutor();
            }
        }
        return result;
    }

    @Override
    public int getParallelizmLevel() {
        return pool.getParallelism();
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return pool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return pool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return pool.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return pool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return pool.invokeAll(tasks,timeout,unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return pool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return pool.invokeAny(tasks,timeout,unit);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
