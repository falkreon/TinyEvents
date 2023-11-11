package blue.endless.tinyevents;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import blue.endless.tinyevents.impl.EventImpl;

public interface Event<T> {
	T invoker();
	
	void register(T handler);
	void register(T handler, Object key);
	void register(T handler, Object key, Executor executor);
	
	void unregister(Object key);
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to.
	 * @return the new Event
	 */
	public static Event<Runnable> runnable() {
		return EventImpl.runnable();
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to.
	 * @param <T> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <T> Event<Consumer<T>> consumer() {
		return EventImpl.consumer();
	}
	
	/**
	 * Creates an event that BiConsumer event-handlers may be registered to.
	 * @param <T> The type of the first parameter that event-handlers will receive
	 * @param <U> The type of the second parameter that event-handlers will receive
	 * @return the new Event
	 */
	public static <T, U> Event<BiConsumer<T, U>> biConsumer() {
		return EventImpl.biConsumer();
	}
	
	/**
	 * Creates an event that Suppliers may respond to. Responders which are registered later will override
	 * earlier ones. This event fires synchronously only; if a handler is registered with an executor, the executor will
	 * be ignored.
	 * @param <X> The type of data that event-handlers will provide
	 * @return the new Event
	 */
	public static <X> Event<Supplier<X>> synchronousSupplier() {
		return EventImpl.synchronousSupplier();
	}
	
	/**
	 * Creates an event that Suppliers may respond to. All the responders are coalesced with the reducer function. For
	 * instance, if responders reply with `[ 1, 2, 3, 4 ]` with reducer function `(a, b) -&gt; a + b`, the operations
	 * performed will be `((1 + 2) + 3) + 4`.
	 * @param <X> The type of data that event-handlers will provide
	 * @param reducer a function which will be used to coalesce responses into a single value
	 * @return the new Event
	 */
	public static <X> Event<Supplier<X>> synchronousSupplier(BinaryOperator<X> reducer) {
		return EventImpl.synchronousSupplier(reducer);
	}
}
