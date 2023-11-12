package blue.endless.tinyevents;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import blue.endless.tinyevents.util.DirectExecutorService;

/**
 * Represents a fully asynchronous event which can receive one piece of information and yields a result. Because the
 * result of an event call may not be available immediately, firing the event returns a Future for the result.
 */
public class AsyncFunctionEvent<T, U> {
	public static record Entry<T, U>(Function<T, U> handler, Object key) {}
	
	private final ConcurrentLinkedQueue<Entry<T, U>> handlers = new ConcurrentLinkedQueue<>();
	private final BinaryOperator<U> reducer;
	private final ExecutorService executor;
	
	public AsyncFunctionEvent(BinaryOperator<U> reducer, ExecutorService executor) {
		this.reducer = reducer;
		this.executor = executor;
	}
	
	public AsyncFunctionEvent(BinaryOperator<U> reducer) {
		this(reducer, new DirectExecutorService());
	}
	
	public Future<U> fire(T value) {
		ArrayList<Future<U>> futures = new ArrayList<>();
		// Submit all the event handlers and collect their futures
		for(Entry<T, U> f : handlers) {
			Future<U> future = executor.submit(() -> f.handler.apply(value));
			futures.add(future);
		}
		
		// The last task submitted is the coalescer task to turn the futures into one value. This in turn gives us a
		// future for that one value.
		return executor.submit(() -> {
			U result = null;
			for(Future<U> f : futures) {
				U cur = f.get();
				if (result == null) {
					result = cur;
				} else {
					if (cur != null) {
						result = reducer.apply(result, cur);
					}
				}
			}
			
			return result;
		});
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. To unregister this handler, you will
	 * need the key object you provided to this method.
	 * @param handler an event-handler that will respond to this event
	 * @param key an object that can be used to refer to this event-handler when unregistering it. Often a String or other identifier
	 */
	public void register(Function<T, U> handler, Object key) {
		handlers.add(new Entry<>(handler, key));
	}
	
	/**
	 * Registers an event-handler that will be called when this event is fired. To unregister this handler, you need the
	 * exact object reference of the handler!
	 * @param handler an event-handler that will respond to this event
	 */
	public void register(Function<T, U> handler) {
		register(handler, handler);
	}
	
	/**
	 * Unregisters an event-handler. Note that because this class is *weakly-consistent*, handlers may fire for a
	 * very short amount of time after they are unregistered.
	 * @param key The key object that was supplied when registering the handler, or if no key was supplied, the exact
	 * object reference of the handler
	 */
	public void unregister(Object key) {
		handlers.removeIf((it) -> Objects.equals(it.key, key));
	}
	
	/**
	 * Creates an event where handlers which execute more quickly and respond earlier take precedence over handlers
	 * which respond later. All handlers will still be called.
	 * @param executor the ExecutorService that handlers will be dispatched on
	 * @param <T> The type of data passed to event-handlers
	 * @param <U> The type of data returned by event-handlers
	 * @return the new Event
	 */
	public static <T, U> AsyncFunctionEvent<T, U> firstTakesPrecedence(ExecutorService executor) {
		return new AsyncFunctionEvent<>((a, b) -> a, executor);
	}
}
