package bgu.spl.mics.application.objects;


import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static Cluster instance = null;
	private ConcurrentHashMap<GPU, LinkedList<DataBatch>> gpuQueues = new ConcurrentHashMap<>();  	//gpus processed DataBatches queue
	private LinkedList<CPU> cpus = new LinkedList<>();									//cpus unprocessed DataBatches queue
	private LinkedList<Model> trainedModels = new LinkedList<>(); 							//TODO: to add trained models
	private LinkedBlockingQueue<DataBatch> unProcessedDB = new LinkedBlockingQueue<>();
	private AtomicInteger dbProcessed = new AtomicInteger(0);
	private AtomicInteger cpuTimeTicks = new AtomicInteger(0);
	private AtomicInteger gpuTimeTicks = new AtomicInteger(0);

	private void Cluster(){}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Cluster getInstance() {
		if (instance == null){
			synchronized (Cluster.class){
				if (instance == null){
					Cluster x = new Cluster();
					instance = x;
				}
			}
		}
		return instance;
	}



	public LinkedBlockingQueue<DataBatch> getUnProcessedDB() {
		return unProcessedDB;
	}

	public int getDbProcessed() {return dbProcessed.get();}

	public int getCpuTimeTicks() {return cpuTimeTicks.get();}

	public int getGpuTimeTicks() {return gpuTimeTicks.get();}

	public void addGPU(GPU _gpu){
		gpuQueues.put(_gpu , new LinkedList<>());
	}

	public void addCPU(CPU _cpu){
		cpus.push(_cpu);
	}

	public synchronized void addTrainedModel(Model _model){ trainedModels.push(_model); }

	public void addCpuTimeTicks(int _cpuTimeTicks) {
		int val;
		do { val = cpuTimeTicks.get(); }
		while (!cpuTimeTicks.compareAndSet(val, val + _cpuTimeTicks));
	}

	public void addGpuTimeTicks(int _gpuTimeTicks) {
		int val;
		do { val = gpuTimeTicks.get(); }
		while (!gpuTimeTicks.compareAndSet(val, val + _gpuTimeTicks));
	}

	public void addDBProcessed() {
		int val;
		do { val = dbProcessed.get(); }
		while (!dbProcessed.compareAndSet(val, val + 1));
	}

	public void addToUnprocessedDB(DataBatch db){
		unProcessedDB.add(db);
	}


}
