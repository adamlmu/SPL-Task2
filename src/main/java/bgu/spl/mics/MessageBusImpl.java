package bgu.spl.mics;

import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static MessageBusImpl instance = null;
	private ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventSubscribers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event, Future> futures = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> messageQueues = new ConcurrentHashMap<>();

	private MessageBusImpl(){}

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl() ;
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static MessageBusImpl getInstance() {
		if (instance == null){
			synchronized (MessageBusImpl.class){
				if (instance == null){
					MessageBusImpl x = new MessageBusImpl();
					instance = x;
				}
			}
		}
		return instance;
	}

	/**
	 *
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 *
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (eventSubscribers.containsKey(type)) {
			eventSubscribers.get(type).add(m);
			m.setSubscribed(true);
		}
		else {
			eventSubscribers.put(type, new ConcurrentLinkedQueue<MicroService>());
			eventSubscribers.get(type).add(m);
			m.setSubscribed(true);
		}
	}

	/**
	 *
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (messageQueues.containsKey(m)) {
			if (broadcastSubscribers.containsKey(type)) {
				broadcastSubscribers.get(type).add(m);
				m.setSubscribed(true);
			}
			else {
				broadcastSubscribers.put(type, new ConcurrentLinkedQueue<MicroService>());
				broadcastSubscribers.get(type).add(m);
				m.setSubscribed(true);
			}
		}
	}

	/**
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		if (futures.containsKey(e)) {
			futures.get(e).resolve(result);
			futures.remove(e);
		}
	}

	/**
	 *
	 * @param b 	The message to added to the queues.
	 * @post 		subscriber.queue.last() == b
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (b.getClass()) {
			if (broadcastSubscribers.containsKey(b.getClass())) {                //is any service subscribed
				for (MicroService m : broadcastSubscribers.get(b.getClass())) {
					if (messageQueues.get(m) != null) {
						messageQueues.get(m).add(b);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param e		The event to add to the queue.
	 * @post		subscriber.queue.last() == b
	 * @return
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (e.getClass()) {
			if (e != null) {
				if (eventSubscribers.containsKey(e.getClass())) {
					MicroService m = (eventSubscribers.get(e.getClass())).poll();
					if (messageQueues.get(m) != null) {
						(messageQueues.get(m)).add(e);
						(eventSubscribers.get(e.getClass())).add(m);
					}
				}
				Future<T> future = new Future<>();
				futures.putIfAbsent(e, future);
				return future;
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m) {
		if (!messageQueues.containsKey(m)){
			messageQueues.put(m, new LinkedBlockingQueue<Message>());
			m.setRegistered(true);
		}

	}

	@Override
	public void unregister(MicroService m) {
			messageQueues.remove(m);
			broadcastSubscribers.forEach((e,l) -> l.remove(m));
			eventSubscribers.forEach((b,l) -> l.remove(m));
			m.setRegistered(false);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
//		if (m.isRegistered() && messageQueues.containsKey(m)) {
//			LinkedBlockingQueue<Message> l = messageQueues.get(m);
//			while (l.isEmpty()) {
//				try {
//					synchronized (messageQueues.get(m)) {
//						messageQueues.get(m).wait();
//					}
//				} catch (InterruptedException e) {}
//			}
//			return l.take();
//		}
//		else return null;
		Message a=null;
		try {
			 a =  messageQueues.get(m).take();
		}catch (InterruptedException e ){}
		return a;
	}

	public ConcurrentHashMap<Event, Future> getFutures() {
		return futures;
	}

	public ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> getEventSubscribers() {
		return eventSubscribers;
	}
}
