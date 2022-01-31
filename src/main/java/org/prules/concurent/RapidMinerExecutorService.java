package org.prules.concurent;

import com.rapidminer.core.concurrency.ConcurrencyContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class RapidMinerExecutorService implements PRulesExecutorService {

    ConcurrencyContext context;

    public RapidMinerExecutorService(ConcurrencyContext context) {
        this.context = context;
    }

    @Override
    public int getParallelizmLevel() {
        return context.getParallelism();
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return context.submit(new LinkedList<Callable<T>>()).get(0);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Callable<T> call = Executors.callable(task,result);
        return submit(call);
    }

    @Override
    public Future<?> submit(Runnable task) {
        Callable<?> call = Executors.callable(task);
        return submit(call);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Callable<T>> list = new LinkedList<>(tasks);

        List<Future<T>> out = new LinkedList<>();
        try {
            List<T> res = context.call(list);
            res.forEach(e -> {
                out.add(new Future<T>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public T get() throws InterruptedException, ExecutionException {
                        return e;
                    }

                    @Override
                    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        throw new UnsupportedOperationException();
                    }
                });
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    @Override
    public void execute(Runnable command) {
        throw new UnsupportedOperationException("Not allowed in RapidMinerExecutorService");
    }

    public ConcurrencyContext getContext() {
        return context;
    }

    public void setContext(ConcurrencyContext context) {
        this.context = context;
    }
}
