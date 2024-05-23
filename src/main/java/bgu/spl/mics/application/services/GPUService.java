package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private Model model;
    private TrainModelEvent currentEvent;
    private AtomicInteger timeTick = new AtomicInteger(1);
    private boolean training;
    LinkedList<Event> awaitingEvents;

    private void updateTimeTick(){
        int val;
        do { val = timeTick.get(); }
        while (!timeTick.compareAndSet(val, val + 1));
        gpu.updateTimeTick();
    }

    public GPUService(String _name, GPU _gpu) {
        super(_name);
        gpu = _gpu;
        training = false;
        awaitingEvents = new LinkedList<>();
    }

    public void setModel(Model model) {
        this.model = model;
        gpu.setModel(model);
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast)->{
            //updating TimeTick:
            updateTimeTick();

            //continue training:
            gpu.sendDataBatches();
            gpu.training();

            if (model != null) {
                if ((this.model.getData().getSize() <= this.model.getData().getProcessed())){           //If i trained all dataBatches:

                    gpu.doneTraining();
                    getMessageBus().complete(currentEvent, currentEvent.getModel());
                    training = false;
                    model = null;
                }
            }

            if(!training && !awaitingEvents.isEmpty()){
                Event tempEvent = awaitingEvents.pop();
                if (tempEvent instanceof TrainModelEvent){
                    startTraining((TrainModelEvent)tempEvent);
                }
                else if(tempEvent instanceof TestModelEvent){
                    testModel((TestModelEvent) tempEvent);
                }
            }
        });

        subscribeEvent(TrainModelEvent.class, this::startTraining);

        subscribeEvent(TestModelEvent.class, this::testModel);

        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast)->{
            terminate();
        });
    }

    public void testModel(TestModelEvent event){
        if (!training) {
            setModel(event.getModel());
            gpu.test(model);
            getMessageBus().complete(event, model);
        }
        else { awaitingEvents.push(event); }
    }

    public void setCurrentEvent(TrainModelEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void startTraining(TrainModelEvent event){
        if (!training) {
            try {
                training = true;
                setModel(event.getModel());
                setCurrentEvent(event);
                gpu.makeDataBatches();
                gpu.sendDataBatches();
                gpu.training();
            } catch (InterruptedException e) {}
        } else {
            awaitingEvents.add(event);
        }
    }
}

