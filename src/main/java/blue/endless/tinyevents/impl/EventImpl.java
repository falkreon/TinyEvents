package blue.endless.tinyevents.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import blue.endless.tinyevents.Event;

public class EventImpl<T> implements Event<T> {
	private static final Executor DIRECT_EXECUTOR = (it) -> it.run();
	
	public static record Entry<T>(T handler, Object key, Executor executor) {}
	
	private final T invoker;
	
	protected EventImpl(Function<List<Entry<T>>, T> invokerFactory) {
		this.invoker = invokerFactory.apply(handlers);
	}
	
	protected List<Entry<T>> handlers = new ArrayList<>();

	@Override
	public T invoker() {
		return invoker;
	}

	@Override
	public void register(T handler) {
		register(handler, handler, DIRECT_EXECUTOR);
	}

	@Override
	public void register(T handler, Object key) {
		register(handler, key, DIRECT_EXECUTOR);
	}

	@Override
	public void register(T handler, Object key, Executor executor) {
		handlers.add(new Entry<T>(handler, key, executor));
	}
	
	@Override
	public void unregister(Object key) {
		handlers.removeIf(it -> Objects.equals(key, it.key));
	}
	
	public static Event<Runnable> runnable() {
		return new EventImpl<Runnable>(
				(handlers) -> () -> {
					for(Entry<Runnable> entry : handlers) {
						entry.executor.execute(entry.handler);
					}
				}
			);
	}
	
	public static <X> Event<Consumer<X>> consumer() {
		return new EventImpl<Consumer<X>>(
				(handlers) -> (X eventPayload) -> {
					for(Entry<Consumer<X>> entry : handlers) {
						entry.executor.execute(() -> {
							entry.handler.accept(eventPayload);
						});
					}
				}
			);
	}
	
	public static <X, Y> Event<BiConsumer<X, Y>> biConsumer() {
		return new EventImpl<BiConsumer<X, Y>>(
				(handlers) -> (X x, Y y) -> {
					for(Entry<BiConsumer<X, Y>> entry : handlers) {
						entry.executor.execute(() -> {
							entry.handler.accept(x, y);
						});
					}
				}
			);
	}
	
	/**
	 * Ignores any executors provided!!!
	 * @param <X>
	 * @return
	 */
	public static <X> Event<Supplier<X>> synchronousSupplier() {
		return synchronousSupplier((a,b) -> b);
	}
	
	public static <X> Event<Supplier<X>> synchronousSupplier(BinaryOperator<X> reducer) {
		return new EventImpl<Supplier<X>>(
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
