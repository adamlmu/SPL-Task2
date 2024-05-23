package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

public class PublishConferenceBroadcast implements Broadcast {
    private LinkedList<Model> models;

    public PublishConferenceBroadcast(LinkedList<Model> _models){
        models = _models;
    }

    public LinkedList<Model> getModels() {
        return models;
    }

    public int getNumOfPapers(){
        System.out.println(models.size());
        return models.size();
    }

}
