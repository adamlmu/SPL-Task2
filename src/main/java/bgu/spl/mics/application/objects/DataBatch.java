package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    public enum dbStatus {PreProcessed, Processing, PreTrained, Training, Done}

    private Data data;
    private final GPU gpu;
    private final int start_index;
    private dbStatus status;
    private AtomicInteger startingTick = new AtomicInteger(1);

    public DataBatch(Data _data, GPU _gpu, int index){//
        data = _data;
        gpu = _gpu;
        start_index = index;
        status = dbStatus.PreProcessed;

    }

    public Data.Type getType(){
        return data.getType();
    }

    public GPU getGPU() { return gpu; }

    public Data getData() { return data; }

    public int getStartingTick(){
        return startingTick.get();
    }

    public void updateStartingTick(int i){
        int val;
        do { val = startingTick.get(); }
        while (!startingTick.compareAndSet(val, i));
    }

    public dbStatus getStatus() {
        return status;
    }

    public void setStatus(dbStatus status) {
        this.status = status;
    }
}
