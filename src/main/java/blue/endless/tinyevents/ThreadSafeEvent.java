package blue.endless.tinyevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an event which can accept registrations from other threads, and whose invoker MAY make use of a thread
 * pool to call multiple handlers at the same time. Handlers may not be registered with executors; it is their
 * responsibility to hop to another thread if needed.
 */
public class ThreadSafeEvent<T> {
	public static record Entry<T>(T handler, Object key) {}
	
	private final Function<Entry<T>[], T> invokerFactory;
	private T invoker;
	/** All access should be synchronized on `this` */
	private List<Entry<T>> handlers = new ArrayList<>();
	/** After creation, each bakedHandlers object remains unmodified */
	@SuppressWarnings("unchecked")
	protected Entry<T>[] bakedHandlers = (Entry<T>[]) new Entry[0];
	
	/**
	 * Creates an AsyncEvent using the specified invoker factory.
	 * @param invokerFactory A function which will create an object that, when called, will invoke each listener. The
	 * invokers provided MAY execute handlers out of order or on different threads, but MUST NOT make any changes to the
	 * array it receives or cache the array in any way.
	 */
	public ThreadSafeEvent(Function<Entry<T>[], T> invokerFactory) {
		this.invokerFactory = invokerFactory;
		invoker = invokerFactory.apply(bakedHandlers);
	}
	
	/**
	 * Gets an object that can be used to fire this event
	 * @return The invoker, which is the same type as this event's handlers
	 */
	public T invoker() {
		return invoker;
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. To unregister this handler, you need the
	 * exact object reference of the handler!
	 * @param handler an event-handler that will respond to this event
	 */
	public void register(T handler) {
		register(handler, handler);
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. To unregister this handler, you will
	 * need the key object you provided to this method.
	 * @param handler an event-handler that will respond to this event
	 * @param key an object that can be used to refer to this event-handler when unregistering it. Often a String or other identifier
	 */
	public void register(T handler, Object key) {
		synchronized(this) {
			handlers.add(new Entry<>(handler, key));
			bakedHandlers = handlers.toArray(bakedHandlers);
			invoker = invokerFactory.apply(bakedHandlers);
		}
	}
	
	/**
	 * Unregisters an event-handler. Note that this interacts badly with lambdas. If you're having trouble unregistering
	 * events, use a key object or save a reference to the exact object you registered.
	 * @param key the key provided when the event was registered, or if no key was provided, the handler object
	 *            reference itself. A new, identical lambda will not work.
	 */
	public void unregister(Object key) {
		synchronized(this) {
			handlers.removeIf(it -> Objects.equals(key, it.key));
			bakedHandlers = handlers.toArray(bakedHandlers);
			invoker = invokerFactory.apply(bakedHandlers);
		}
	}
	
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to.
	 * @return the new Event
	 */
	public static ThreadSafeEvent<Runnable> runnable() {
		return new ThreadSafeEvent<>((arr) -> () -> {
			for(Entry<Runnable> r : arr) {
				r.handler.run();
			}
		});
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to.
	 * @param <T> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <T> ThreadSafeEvent<Consumer<T>> consumer() {
		return new ThreadSafeEvent<>((arr) -> (T t) -> {
			for(Entry<Consumer<T>> r : arr) {
				r.handler.accept(t);
			}
		});
	}
	
	/**
	 * Creates an event that BiConsumer event-handlers may be registered to.
	 * @param <T> The type of the first parameter that event-handlers will receive
	 * @param <U> The type of the second parameter that event-handlers will receive
	 * @return the new Event
	 */
	public static <T, U> ThreadSafeEvent<BiConsumer<T, U>> biConsumer() {
		return new ThreadSafeEvent<>((arr) -> (T t, U u) -> {
			for(Entry<BiConsumer<T, U>> r : arr) {
				r.handler.accept(t, u);
			}
		});
	}
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to. Execution of event-handlers will be scheduled
	 * on the provided Executor, which can be a ScheduledExecutorService to run multiple handlers at the same time.
	 * @param executor an object which will be used to schedule event-handler execution
	 * @return the new Event
	 */
	public static ThreadSafeEvent<Runnable> pooledRunnable(Executor executor) {
		return new ThreadSafeEvent<>((arr) -> () -> {
			for(Entry<Runnable> r : arr) {
				executor.execute(r.handler());
			}
		});
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to. Execution of event-handlers will be scheduled
	 * on the provided Executor, which can be a ScheduledExecutorService to run multiple handlers at the same time.
	 * @param <T> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <T> ThreadSafeEvent<Consumer<T>> pooledConsumer(Executor executor) {
		return new ThreadSafeEvent<>((arr) -> (T t) -> {
			for(Entry<Consumer<T>> r : arr) {
				executor.execute(() -> r.handler().accept(t));
			}
		});
	}
	
	/**
	 * Creates an event that BiConsumer event-handlers may be registered to. Execution of event-handlers will be
	 * scheduled on the provided Executor, which can be a ScheduledExecutorService to run multiple handlers at the same
	 * time.
	 * @param <T> The type of the first parameter that event-handlers will receive
	 * @param <U> The type of the second parameter that event-handlers will receive
	 * @return the new Event
	 */
	public static <T, U> ThreadSafeEvent<BiConsumer<T, U>> pooledBiConsumer(Executor executor) {
		return new ThreadSafeEvent<>((arr) -> (T t, U u) -> {
			for(Entry<BiConsumer<T, U>> r : arr) {
				executor.execute(() -> r.handler.accept(t, u));
			}
		});
	}
}
