package blue.endless.tinyevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an Event where listeners can be fired on other threads via thread Executors. However, registering and
 * unregistering handlers MUST happen on the thread the event was created on.
 */
public class ExecutableEvent<T> {
	private static final Executor DIRECT_EXECUTOR = (it) -> it.run();
	
	public static record Entry<T>(T handler, Object key, Executor executor) {}
	
	private final T invoker;
	protected List<Entry<T>> handlers = new ArrayList<>();
	private Thread thread;
	
	protected ExecutableEvent(Function<List<Entry<T>>, T> invokerFactory) {
		this.invoker = invokerFactory.apply(handlers);
		this.thread = Thread.currentThread();
	}
	
	/**
	 * Gets an object that can be used to fire this event
	 * @return The invoker, which is the same type as this event's handlers
	 */
	public T invoker() {
		return invoker;
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. Must be called on the same thread that
	 * the event was created on. To unregister this handler, you need the exact object reference of the handler!
	 * @param handler an event-handler that will respond to this event
	 */
	public void register(T handler) {
		register(handler, handler, DIRECT_EXECUTOR);
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. Must be called on the same thread that
	 * the event was created on. To unregister this handler, you will need the key object you provided to this method.
	 * @param handler an event-handler that will respond to this event
	 * @param key an object that can be used to refer to this event-handler when unregistering it. Often a String or other identifier
	 */
	public void register(T handler, Object key) {
		register(handler, key, DIRECT_EXECUTOR);
	}

	/**
	 * Registers an event-handler that will be called when this event is fired. Must be called on the same thread that
	 * the event was created on. To unregister this handler, you will need the key object you provided to this method.
	 * @param handler an event-handler that will respond to this event
	 * @param key an object that can be used to refer to this event-handler when unregistering it. Often a String or other identifier
	 * @param executor an Executor which will be used to schedule the event's execution on a particular thread
	 */
	public void register(T handler, Object key, Executor executor) {
		checkThread();
		handlers.add(new Entry<T>(handler, key, executor));
	}
	
	/**
	 * Unregisters an event-handler. Note that this interacts badly with lambdas. If you're having trouble unregistering
	 * events, use a key object or save a reference to the exact object you registered.
	 * @param key the key provided when the event was registered, or if no key was provided, the handler object
	 *            reference itself. A new, identical lambda will not work.
	 */
	public void unregister(Object key) {
		checkThread();
		handlers.removeIf(it -> Objects.equals(key, it.key));
	}
	
	/**
	 * Unregisters all event-handlers. Must be called on the same thread that the event was created on.
	 */
	public void clear() {
		checkThread();
		handlers.clear();
	}
	
	private void checkThread() {
		if (!Objects.equals(thread, Thread.currentThread())) throw new IllegalStateException("Must be executed on the thread that the event was created on");
	}
	
	
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to.
	 * @return the new Event
	 */
	public static ExecutableEvent<Runnable> runnable() {
		return new ExecutableEvent<Runnable>(
				(handlers) -> () -> {
					for(Entry<Runnable> entry : handlers) {
						entry.executor.execute(entry.handler);
					}
				}
			);
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to.
	 * @param <T> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <X> ExecutableEvent<Consumer<X>> consumer() {
		return new ExecutableEvent<Consumer<X>>(
				(handlers) -> (X eventPayload) -> {
					for(Entry<Consumer<X>> entry : handlers) {
						entry.executor.execute(() -> {
							entry.handler.accept(eventPayload);
						});
					}
				}
			);
	}
	
	/**
	 * Creates an event that BiConsumer event-handlers may be registered to.
	 * @param <T> The type of the first parameter that event-handlers will receive
	 * @param <U> The type of the second parameter that event-handlers will receive
	 * @return the new Event
	 */
	public static <X, Y> ExecutableEvent<BiConsumer<X, Y>> biConsumer() {
		return new ExecutableEvent<BiConsumer<X, Y>>(
				(handlers) -> (X x, Y y) -> {
					for(Entry<BiConsumer<X, Y>> entry : handlers) {
						entry.executor.execute(() -> {
							entry.handler.accept(x, y);
						});
					}
				}
			);
	}
}
