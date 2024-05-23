package bgu.spl.mics.application.tests;

import bgu.spl.mics.application.objects.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class GPUTest {

    private static GPU gpu;
    private static Model model;
    private static Cluster cluster;
    private static Data data;


    @Before
    public void setUp() throws Exception {
        data = new Data("Images", 20000);
        model = new Model("name", data);
        cluster = Cluster.getInstance();
        gpu = new GPU("RTX2080","gpu");
    }

    @Test
    public void testSendDataBatches() {
        gpu.makeDataBatches();
        gpu.sendDataBatches();
        assertFalse(Cluster.getInstance().getUnProcessedDB().isEmpty());
    }

    @Test
    public void testMakeDataBatches() {
        gpu.makeDataBatches();
        assertFalse(gpu.getUnprocessedDB().isEmpty());
    }

    @Test
    public void training() throws InterruptedException {
        gpu.makeDataBatches();
        DataBatch db = gpu.getUnprocessedDB().pop();
        db.setStatus(DataBatch.dbStatus.PreTrained);
        gpu.getProcessedDBs().add(db);
        int pro = data.getProcessed();
        gpu.training();
        assertEquals(DataBatch.dbStatus.Training,gpu.getTrainingDB().getStatus());
        assertEquals(pro + 1000, data.getProcessed());
    }

    @Test
    public void updateTimeTick() {
        int time = gpu.getTimeTicks();
        gpu.updateTimeTick();
        assertEquals(time + 1, gpu.getTimeTicks());
    }
}