package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.DataBatch;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private AtomicInteger timeTick = new AtomicInteger(1);

    private void updateTimeTick(){
        int val;
        do { val = timeTick.get(); }
        while (!timeTick.compareAndSet(val, val + 1));
        cpu.updateTimeTick();
    }

    public CPUService(CPU _cpu,String _name) {
        super(_name);
        cpu = _cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast)->{
            updateTimeTick();
            processing();
        });

        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast)->{
            terminate();
        });
    }

    private void processing() throws InterruptedException {
        cpu.process();
    }
}
