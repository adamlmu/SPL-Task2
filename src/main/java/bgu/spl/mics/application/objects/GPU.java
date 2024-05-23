package bgu.spl.mics.application.objects;



import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model = null;
    private Cluster cluster;
    private AtomicInteger timeTick;  //ticks from the TimeService are updated here
    private final int vRAM;   //represent the vRAM limit of the specific model
    private AtomicInteger processed;
    private LinkedList<DataBatch> unprocessedDB;
    private int counter;
    private DataBatch trainingDB = null;
    private String name;
    private ConcurrentLinkedQueue<DataBatch> processedDBs;

    public GPU(String _type, String _name){
        if (_type.equals("RTX3090")) type = Type.RTX3090;
        else if (_type.equals("RTX2080")) type = Type.RTX2080;
        else type = Type.GTX1080;

        name = _name;
        timeTick = new AtomicInteger(1);
        processed = new AtomicInteger(0);
        unprocessedDB = new LinkedList<>();
        cluster = Cluster.getInstance();
        if (type.equals(Type.RTX3090)) vRAM = 32;
        else if (type.equals(Type.RTX2080)) vRAM = 16;
        else vRAM = 8;
        counter = vRAM;
        processedDBs = new ConcurrentLinkedQueue<>();
    }

    public void setCluster(){
        cluster = Cluster.getInstance();
    }

    public void setModel(Model _model) {
        model = _model;
    }

    public Type getType(){
        return this.type;
    }

    public Model getModel(){
        return this.model;
    }

    public Cluster getCluster(){ return this.cluster; }

    public int getTimeTicks() {  return timeTick.get();  }

    public int getVRAM() { return this.vRAM; }

    /**
     * Sends the DataBatches to the CPUs via the cluster to be processed by the CPUs
     * @pre dataBatches.isEmpty == false
     */
    public void sendDataBatches(){
        if (counter > 0) {
            for (int i = 0; i < 5; i++) {
                if (!unprocessedDB.isEmpty()) {
                    cluster.addToUnprocessedDB(unprocessedDB.pop());
                }
            }
            counter--;
        }
    }

    public LinkedList<DataBatch> getUnprocessedDB() {
        return unprocessedDB;
    }

    /**
     * Divides the Data from the model into CPU processable batches
     * @pre model.getData.getProcessed == 0
     * @post DataBatches.size == (model.getData.size)/1000
     */
    public void makeDataBatches(){
        for (int i=1 ; i<model.getData().getSize(); i=i+1000 )
            unprocessedDB.add(new DataBatch(model.getData(), this,i-1));
    }

    /**
     * Trains the ModelEvent with the processed data
     * @pre processedGPUQueue.isEmpty == false
     * @post model.getData.getProcessed = @pre(model.getData.getProcessed) + 1000
     * @post db.isGPUTrained == true
     */
    public void training() throws InterruptedException {            //need to do something with the Future
        takeProcessedDB();
        if (trainingDB != null) {
            if (model.getStatus().equals(Model.Status.PreTrained)) model.setStatus(Model.Status.Training);
            if (trainingDB.getStatus() == DataBatch.dbStatus.PreTrained) {
                trainingDB.setStatus(DataBatch.dbStatus.Training);
                trainingDB.updateStartingTick(timeTick.get());
            }
            if (trainingDB.getStatus() == DataBatch.dbStatus.Training){
                int t;
                if (type.equals(Type.RTX3090)) t = 1;
                else if (type.equals(Type.RTX2080)) t = 2;
                else t = 4;

                if ((timeTick.get() >= (trainingDB.getStartingTick() + t))) {
                    trainingDB.setStatus(DataBatch.dbStatus.Done);
                    cluster.addGpuTimeTicks(t);
                    model.getData().increaseProcessed();
                    counter++;
                    trainingDB=null;
                }
            }
        }
    }

    public void test(Model model){                                              // something with future
        if ( model.getStatus().equals(Model.Status.Trained) ){
            int r = (int) (Math.random() * 10); // 0 <= r <= 9
            if ((model.getStudent().getDegree()).equals(Student.Degree.MSc)) {
                if (r > 5) model.setResult(Model.TestResult.Bad);       // 40% for bad result
                else{
                    model.setResult(Model.TestResult.Good);
                }
            }
            else {
                if (r > 7) model.setResult(Model.TestResult.Bad);       // 20% for bad result
                else{
                    model.setResult(Model.TestResult.Good);
                }
            }
            model.setStatus(Model.Status.Tested);
        }
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

    public int getCounter() {
        return counter;
    }

    public void takeProcessedDB(){
        if (trainingDB == null){
            if (!(processedDBs.isEmpty())) {
                trainingDB = processedDBs.poll();
            }
        }
    }

    public void doneTraining(){
        model.setStatus(Model.Status.Trained);
        cluster.addTrainedModel(model);
        counter = vRAM;
    }

    public DataBatch getTrainingDB() {
        return trainingDB;
    }

    public void setTrainingDB(DataBatch trainingDB) {
        this.trainingDB = trainingDB;
    }

    public String getName() {
        return name;
    }

    public ConcurrentLinkedQueue<DataBatch> getProcessedDBs() {
         return processedDBs;
    }
}
