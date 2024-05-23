package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private ConcurrentLinkedQueue<DataBatch> data;
    private Cluster cluster;
    private AtomicInteger timeTick;
    private DataBatch processingDB = null;
    private String name;

    public CPU(String _name, int _cores){
        name = _name;
        cores = _cores;
        cluster = Cluster.getInstance();
        timeTick = new AtomicInteger(1);
        data = new ConcurrentLinkedQueue<>();
    }

    public String getName() {
        return name;
    }

    public int getCores(){
        return this.cores;
    }

    public int getTimeTick() { return this.timeTick.get(); }

    public ConcurrentLinkedQueue<DataBatch> getData(){  return this.data;    }

    public Cluster getCluster(){
        return this.cluster;
    }

    /**
     * updates the timeTick with the broadcast update
     * @inv timeTicks > 0
     * @post timeTicks = 1 + @PRE timeTicks
     */
    public void updateTimeTick(){
        int val;
        do { val = timeTick.get(); }
        while (!timeTick.compareAndSet(val, val + 1));
    }

    /**
     * Process the DataBatch for the GPU for the cluster
     * @pre db.isCPUProcessed == false
     * @post db.isCPUProcessed == true
     */
    public void process() throws InterruptedException {
        if (processingDB == null)
            takeDB();
        if (processingDB != null) {
            if (processingDB.getData().getSize() == processingDB.getData().getProcessed()){
                takeDB();
            }
            if (processingDB.getStatus() == DataBatch.dbStatus.PreProcessed) {
                processingDB.setStatus(DataBatch.dbStatus.Processing);
                processingDB.updateStartingTick(timeTick.get());
            }
            if (processingDB.getStatus() == DataBatch.dbStatus.Processing) {
                int t;
                if (processingDB.getType() == Data.Type.Images) t = 4;
                else if (processingDB.getType() == Data.Type.Text) t = 2;
                else t = 1;

                if (timeTick.get() >= ((processingDB.getStartingTick() + ((32 / cores) * t)))) {
                    processingDB.setStatus(DataBatch.dbStatus.PreTrained);
                    cluster.addDBProcessed();
                    cluster.addCpuTimeTicks((32 / cores) * t);
                    sendDataBatch(processingDB);
                    takeDB();
                }
            }
        }
    }


    public void takeDB() throws InterruptedException {
        if (!cluster.getUnProcessedDB().isEmpty()){
            processingDB = cluster.getUnProcessedDB().poll();
        }
    }

    /**
     * sends the processed DataBatch to the GPU's queue of processed DataBatches in the cluster
     * @param db the DataBatch that needs to be sent to the GPU via the cluster
     * @pre db.isCPUProcessed == true
     * @pre db.isGPUTrained == false
     * @pre db.getGPU.vRAM < gpu.type vRAM limit
     * @post db.getGPU.getQueue.size == @pre db.getGPUQueue.size + 1
     */
    public  void sendDataBatch(DataBatch db) throws InterruptedException {
        synchronized (db.getGPU().getProcessedDBs()){
            if (db.getStatus() == DataBatch.dbStatus.PreTrained)
                db.getGPU().getProcessedDBs().add(db);
        }
    }
}
