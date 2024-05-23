package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConferenceInformation conferenceInformation;
    private AtomicInteger timeTick = new AtomicInteger(1);
    private boolean published;

    public ConferenceService(String name, ConferenceInformation _conferenceInformation) {
        super(name);
        conferenceInformation = _conferenceInformation;
        published = false;
    }

    private void updateTimeTick(){
        int val;
        do { val = timeTick.get(); }
        while (!timeTick.compareAndSet(val, val + 1));
        if (conferenceInformation.updateTimeTick()){
            LinkedList<Model> _models = conferenceInformation.turnIntoBC();
            sendBroadcast(new PublishConferenceBroadcast(_models));
            terminate();

        }
    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class, (PublishResultsEvent)->{
            conferenceInformation.addEvent(PublishResultsEvent);
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast)->{
            updateTimeTick();
        });

        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast)->{
            terminate();
        });
    }



}
