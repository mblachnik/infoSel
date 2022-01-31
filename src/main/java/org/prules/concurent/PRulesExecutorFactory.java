package org.prules.concurent;

import com.rapidminer.core.concurrency.ConcurrencyContext;

import java.util.concurrent.ExecutorService;

public class PRulesExecutorFactory {
    private static PRulesExecutorService service;

    public static synchronized void registerRapidMinerConcurencyContext(ConcurrencyContext context){
        if (service==null)
            service = new RapidMinerExecutorService(context);
        else{
            if (((RapidMinerExecutorService)service).getContext() != context){
                throw new RuntimeException("Problem with parallel context - not the same");
            }
        }
    }

    public static synchronized PRulesExecutorService getInstance(){
        if (service==null) {
            service = PRulesForkJoinExecutor.getInstance();
            //throw new UnsupportedOperationException("In RapidMiner please call before registerRapidMinerConcurencyContext method");
        }
        return service;
    }
}
