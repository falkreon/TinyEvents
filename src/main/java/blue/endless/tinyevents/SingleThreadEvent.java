package blue.endless.tinyevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a high-performance event which cannot be safely accessed off-thread.
 */
public class SingleThreadEvent<T> {
	public static record Entry<T>(T handler, Object key) {}
	
	private final T invoker;
	protected final List<Entry<T>> handlers = new ArrayList<>();
	
	/**
	 * Creates a new event using the specified invoker factory.
	 * @param invokerFactory A function which will be called to turn a list of handlers into an invoker object.
	 */
	public SingleThreadEvent(Function<List<Entry<T>>, T> invokerFactory) {
		this.invoker = invokerFactory.apply(handlers);
	}
	
	/**
	 * Gets an object that can be used to fire this event
	 * @return The invoker, which is the same type as this event's handlers
	 */
	public T invoker() {
		return this.invoker;
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
		handlers.add(new Entry<T>(handler, key));
	}
	
	/**
	 * Unregisters an event-handler. Note that this interacts badly with lambdas. If you're having trouble unregistering
	 * events, use a key object or save a reference to the exact object you registered.
	 * @param key the key provided when the event was registered, or if no key was provided, the handler object
	 *            reference itself. A new, identical lambda will not work.
	 */
	public void unregister(Object key) {
		handlers.removeIf(it -> Objects.equals(key, it.key));
	}
	
	/**
	 * Unregisters all event-handlers.
	 */
	public void clear() {
		handlers.clear();
	}
	
	
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to.
	 * @return the new Event
	 */
	public static SingleThreadEvent<Runnable> runnable() {
		return new SingleThreadEvent<Runnable>(
				(handlers) -> () -> {
					for(Entry<Runnable> entry : handlers) {
						entry.handler.run();
					}
				}
			);
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to.
	 * @param <X> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <X> SingleThreadEvent<Consumer<X>> consumer() {
		return new SingleThreadEvent<Consumer<X>>(
				(handlers) -> (X eventPayload) -> {
					for(Entry<Consumer<X>> entry : handlers) {
						entry.handler.accept(eventPayload);
					}
				}
			);
	}
	
	/**
	 * Creates an event that BiConsumer event-handlers may be registered to.
	 * @param <X> The type of the first parameter that event-handlers will receive
	 * @param <Y> The type of the second parameter that event-handlers will receive
	 * @return the new Event
	 */
	public static <X, Y> SingleThreadEvent<BiConsumer<X, Y>> biConsumer() {
		return new SingleThreadEvent<BiConsumer<X, Y>>(
				(handlers) -> (X x, Y y) -> {
					for(Entry<BiConsumer<X, Y>> entry : handlers) {
						entry.handler.accept(x, y);
					}
				}
			);
	}
	
	/**
	 * Creates an event that Suppliers may respond to. Responders which are registered later will override
	 * earlier ones.
	 * @param <X> The type of data that event-handlers will provide
	 * @return the new Event
	 */
	public static <X> SingleThreadEvent<Supplier<X>> supplier() {
		return supplier((a,b) -> b);
	}
	
	/**
	 * Creates an event that Suppliers may respond to. All the responders are coalesced with the reducer function. For
	 * instance, if responders reply with `[ 1, 2, 3, 4 ]` with reducer function `(a, b) -&gt; a + b`, the operations
	 * performed will be `((1 + 2) + 3) + 4`.
	 * @param <X> The type of data that event-handlers will provide
	 * @param reducer a function which will be used to coalesce responses into a single value
	 * @return the new Event
	 */
	public static <X> SingleThreadEvent<Supplier<X>> supplier(BinaryOperator<X> reducer) {
		return new SingleThreadEvent<Supplier<X>>(
				(handlers) -> () -> {
					X result = null;
					for(Entry<Supplier<X>> entry : handlers) {
						result = reducer.apply(result, entry.handler.get());
					}
					
					return result;
				}
			);
	}
}
