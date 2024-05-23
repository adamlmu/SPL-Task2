package bgu.spl.mics.application.tests;

import bgu.spl.mics.application.objects.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CPUTest {
    private static CPU cpu;
    private static Cluster cluster;
    private static ArrayList<DataBatch> dataBatches;
    private static GPU gpu;
    private static DataBatch db;

    @Before
    public void setUp() throws Exception {
        gpu = new GPU("RTX2080","gpu");
        db = new DataBatch(new Data("Images",20000),gpu,0);
        cluster = new Cluster();
        cpu = new CPU("1", 32);
    }

    @Test
    public void testProcess() throws InterruptedException {
       Cluster.getInstance().getUnProcessedDB().add(db);
       assertEquals(DataBatch.dbStatus.PreProcessed,db.getStatus());
       cpu.process();
       assertEquals(DataBatch.dbStatus.Processing,db.getStatus());
    }

    @Test
    public void testSendDataBatch() throws InterruptedException {
        assertTrue(gpu.getProcessedDBs().isEmpty());
        cpu.sendDataBatch(db);
        assertFalse(gpu.getProcessedDBs().isEmpty());
    }
}