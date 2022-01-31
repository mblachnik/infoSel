package org.prules.concurent;

import java.util.concurrent.ExecutorService;

public interface PRulesExecutorService extends ExecutorService {
    int getParallelizmLevel();
}
