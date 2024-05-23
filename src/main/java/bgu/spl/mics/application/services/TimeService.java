package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private TickBroadcast tick = new TickBroadcast();
	private TerminateBroadcast terminate = new TerminateBroadcast();
	private int duration;
	private int speed;
	private Timer timer = new Timer();
	private AtomicInteger time = new AtomicInteger(1);


	public TimeService(String _name) {
		super(_name);
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	protected void initialize() {
		int totalTicks = duration;
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				increaseTime();
				if (time.get() > duration) {
					sendBroadcast(terminate);
					terminate();
				} else {
					sendBroadcast(tick);
					//System.out.println("this is tick num: " + time.get());
				}
			}
		}, 0, speed);
	}


	public void increaseTime(){
		int val;
		do { val = time.get(); }
		while (!time.compareAndSet(val, val + 1));
	}

}

