package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    enum StudentStatus {Nothing, Training, Testing, Publishing }

    private Student student;
    private String name;
    private Future future;
    private StudentStatus studentStatus;

    public StudentService(String _name, Student _student) {
        super(_name);
        name = _name;
        student = _student;
        studentStatus = StudentStatus.Nothing;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast)->{
            tickReaction();
        });

        subscribeBroadcast(PublishConferenceBroadcast.class, (PublishConferenceBroadcast)-> {
            student.setPapersRead(PublishConferenceBroadcast.getNumOfPapers());
        });

        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast)->{
            terminate();
        });
    }

    public void setFuture(Future _future) {
        future = _future;
    }

    public Student getStudent() {
        return student;
    }

    public Future getFuture() {
        return future;
    }

    public void createAndSendTrainEvent(){
        Model temp = student.popModel();
        if (temp!=null) {
            TrainModelEvent e = new TrainModelEvent(temp);
            future = sendEvent(e);


        }
    }

    public void createAndSendTestEvent(Model model){
        TestModelEvent e = new TestModelEvent(model);
        future = sendEvent(e);
    }

    public void createAndSendResultEvent(Model model){
        PublishResultsEvent e = new PublishResultsEvent(model);
        sendEvent(e);
    }

    private void tickReaction() throws InterruptedException {
        if (future == null) {
            int i = student.getModels().size();
            this.createAndSendTrainEvent();
            if (i>student.getModels().size())
                studentStatus = StudentStatus.Training;
        }
        else if (future.isDone()) {
            if (studentStatus == StudentStatus.Training){
                studentStatus = StudentStatus.Testing;
                createAndSendTestEvent((Model) future.get());
            }
            else if (studentStatus == StudentStatus.Testing){
                Model model = (Model)future.get();
                if (model.getResult() == Model.TestResult.Good){
                    createAndSendResultEvent(model);
                    student.addPublications(model);
                    createAndSendTrainEvent();
                    studentStatus = StudentStatus.Training;
                }
                else{
                    createAndSendTrainEvent();
                    studentStatus = StudentStatus.Training;
                }
            }
        }
    }
}
