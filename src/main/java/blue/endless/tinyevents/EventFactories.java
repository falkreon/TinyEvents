/*
 * MIT License
 *
 * Copyright (c) 2021-2026 Falkreon (Isaac Ellingson)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package blue.endless.tinyevents;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import blue.endless.tinyevents.Event.Entry;
import blue.endless.tinyevents.function.BooleanBinaryOperator;
import blue.endless.tinyevents.function.IntBiConsumer;

public class EventFactories {
	
	/** A factory for simple events handled by Runnables */
	public static final EventFactory<Runnable> RUNNABLE = EventFactories::runnable;
	
	/** A factory producing events whose handlers receive one String value */
	public static final EventFactory<Consumer<String>> STRING_CONSUMER = () -> EventFactories.consumer();
	/** A factory producing events whose handlers receive one double value */
	public static final EventFactory<DoubleConsumer>   DOUBLE_CONSUMER = EventFactories::doubleConsumer;
	/** A factory producing events whose handlers receive one int value */
	public static final EventFactory<IntConsumer>      INT_CONSUMER    = EventFactories::intConsumer;
	/** A factory producing events whose handlers receive one long value */
	public static final EventFactory<LongConsumer>     LONG_CONSUMER   = EventFactories::longConsumer;
	
	/** A factory for String-Supplier events where handlers that are registered later override earlier ones. */
	public static final EventFactory<Supplier<String>> STRING_SUPPLIER      = EventFactories::supplier;
	/** A factory for boolean-Supplier events where all handlers must vote "true" to yield true. */
	public static final EventFactory<BooleanSupplier>  BOOLEAN_SUPPLIER_AND = () -> EventFactories.booleanSupplier(() -> false, BooleanBinaryOperator.AND);
	/** A factory for boolean-Supplier events where any handler voting "true" yields true. */
	public static final EventFactory<BooleanSupplier>  BOOLEAN_SUPPLIER_OR  = () -> EventFactories.booleanSupplier(() -> false, BooleanBinaryOperator.OR);
	
	/** A factory for double-Supplier events where handlers' results are summed to create the event-result */
	public static final EventFactory<DoubleSupplier>   DOUBLE_SUPPLIER_CUMULATIVE     = () -> EventFactories.doubleSupplier(0D, (DoubleBinaryOperator) ((a, b) -> a + b));
	/**
	 * A factory for double-Supplier events where handlers' results are multiplied to create the event-result.
	 */
	public static final EventFactory<DoubleSupplier>   DOUBLE_SUPPLIER_MULTIPLICATIVE = () -> EventFactories.doubleSupplier(1.0D, (DoubleBinaryOperator) ((a, b) -> a * b));
	
	public static final EventFactory<IntSupplier>      INT_SUPPLIER_CUMULATIVE        = () -> EventFactories.intSupplier((IntBinaryOperator) ((a, b) -> a + b));
	public static final EventFactory<LongSupplier>     LONG_SUPPLIER_CUMULATIVE       = () -> EventFactories.longSupplier((LongBinaryOperator) ((a, b) -> a + b));
	
	public static final EventFactory<DoublePredicate>  DOUBLE_PREDICATE_AND = () -> EventFactories.doublePredicate(BooleanBinaryOperator.AND);
	public static final EventFactory<DoublePredicate>  DOUBLE_PREDICATE_OR  = () -> EventFactories.doublePredicate(BooleanBinaryOperator.OR);
	
	
	
	/**
	 * Creates an event that Runnable event-handlers may be registered to.
	 * @return the new Event
	 */
	public static Event<Runnable> runnable() {
		return new Event<Runnable>(
			(handlers) -> () -> {
				for(Entry<Runnable> entry : handlers) {
					entry.handler().run();
				}
			}
		);
	}
	
	/**
	 * Creates an event that Consumer event-handlers may be registered to.
	 * @param <X> The type of data which event-handlers will receive (consume)
	 * @return the new Event
	 */
	public static <X> Event<Consumer<X>> consumer() {
		return new Event<>(
			(handlers) -> (X eventPayload) -> {
				for(Entry<Consumer<X>> entry : handlers) {
					entry.handler().accept(eventPayload);
				}
			}
		);
	}
	
	/**
	 * Creates an event that DoubleConsumer event-handlers may be registered to.
	 * @return the new Event
	 */
	public static Event<DoubleConsumer> doubleConsumer() {
		return new Event<>(
			(handlers) -> (double value) -> {
				for(Entry<DoubleConsumer> entry : handlers) {
					entry.handler().accept(value);
				}
			}
		);
	}
	
	/**
	 * Creates an event that IntConsumer event handlers may be registered to.
	 * @return the new Event
	 */
	public static Event<IntConsumer> intConsumer() {
		return new Event<>(
			(handlers) -> (int value) -> {
				for(Entry<IntConsumer> entry : handlers) {
					entry.handler().accept(value);
				}
			}
		);
	}
	
	/**
	 * Creates an event that LongConsumer event handlers my be registered to.
	 * @return the new Event
	 */
	public static Event<LongConsumer> longConsumer() {
		return new Event<>(
			(handlers) -> (long value) -> {
				for(Entry<LongConsumer> entry : handlers) {
					entry.handler().accept(value);
				}
			}
		);
	}
	
	/**
	 * Creates an event that Suppliers may respond to. Handlers which are registered later will override earlier ones.
	 * If no handlers are registered, null will be supplied.
	 * @param <X> The type of data that event-handlers will provide
	 * @return the new Event
	 */
	public static <X> Event<Supplier<X>> supplier() {
		return supplier(() -> null, (a,b) -> b);
	}
	
	/**
	 * Creates an event which when fired, starts with an initial value, and is then progressively combined with
	 * handler-results using a reducer function to create an event-result.
	 * @param <X> The type of object to return
	 * @param initialValue the initial value, which will be returned if no handlers are present.
	 * @param reducer a function to combine the initial value and the handler-results to produce an event-result.
	 * @return the new Event
	 */
	public static <X> Event<Supplier<X>> supplier(Supplier<X> initialValue, BinaryOperator<X> reducer) {
		return new Event<Supplier<X>>(
			(handlers) -> () -> {
				X result = initialValue.get();
				for(Entry<Supplier<X>> handler : handlers) {
					result = reducer.apply(result, handler.handler().get());
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event that handlers can use to vote on a single boolean-valued result. False is returned if no
	 * suppliers are registered.
	 * @param initialValue the initial value, which will be returned if no handlers are present.
	 * @param reducer a function to combine the initial value and the handler-results to produce an event-result.
	 * @return the new Event
	 */
	public static Event<BooleanSupplier> booleanSupplier(BooleanSupplier initialValue, BooleanBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> () -> {
				boolean result = initialValue.getAsBoolean();
				for(Entry<BooleanSupplier> handler : handlers) {
					reducer.apply(result, handler.handler().getAsBoolean());
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event that handlers can use to supply a double-valued result.
	 * @param sentinel A value that should be returned if no handlers are registered
	 * @param reducer A function that combines multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static Event<DoubleSupplier> doubleSupplier(double sentinel, DoubleBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> () -> {
				double result = sentinel;
				for(int i=0; i<handlers.size(); i++) {
					double cur = handlers.get(i).handler().getAsDouble();
					result = (i==0) ? cur : reducer.applyAsDouble(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that handlers can use to supply an int-valued result.
	 * @param reducer A function that combines multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static Event<IntSupplier> intSupplier(IntBinaryOperator reducer) {
		return new Event<>(
				(handlers) -> () -> {
					int result = 0;
					for(int i=0; i<handlers.size(); i++) {
						int cur = handlers.get(i).handler().getAsInt();
						result = (i==0) ? cur : reducer.applyAsInt(result, cur);
					}
					
					return result;
				}
			);
	}
	
	/**
	 * Creates an event that handlers can use to supply an int-valued result.
	 * @param reducer A function that combines multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static Event<LongSupplier> longSupplier(LongBinaryOperator reducer) {
		return new Event<>(
				(handlers) -> () -> {
					long result = 0;
					for(int i=0; i<handlers.size(); i++) {
						long cur = handlers.get(i).handler().getAsLong();
						result = (i==0) ? cur : reducer.applyAsLong(result, cur);
					}
					
					return result;
				}
			);
	}
	
	/**
	 * Creates an event that Functions may respond to. All the responders are turned into a single value by recursively
	 * applying the reducer function for every two values returned.
	 * @param <T> The type of data that event-handlers will receive
	 * @param <U> The type of data that event-handlers will provide
	 * @param reducer a function which will turn responses into a single value
	 * @return the new Event
	 */
	public static <T, U> Event<Function<T, U>> function(BinaryOperator<U> reducer) {
		return new Event<>(
			(handlers) -> (T t) -> {
				U result = null;
				for(int i=0; i<handlers.size(); i++) {
					U cur = handlers.get(i).handler().apply(t);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives a double value and produces a reference-typed result.
	 * @param <X> The "result type" for the Event to create
	 * @param reducer A function that turns multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static <X> Event<DoubleFunction<X>> doubleFunction(BinaryOperator<X> reducer) {
		return new Event<>(
			(handlers) -> (double value) -> {
				X result = null;
				for(int i=0; i<handlers.size(); i++) {
					X cur = handlers.get(i).handler().apply(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives an integer value and produces a reference-typed result.
	 * @param <X> The "result type" for the Event to create
	 * @param reducer A function that turns multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static <X> Event<IntFunction<X>> intFunction(BinaryOperator<X> reducer) {
		return new Event<>(
			(handlers) -> (int value) -> {
				X result = null;
				for(int i=0; i<handlers.size(); i++) {
					X cur = handlers.get(i).handler().apply(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives a long value and produces a reference-typed result.
	 * @param <X> The "result type" for the Event to create
	 * @param reducer A function that turns multiple handler-results into a single event-result
	 * @return the new Event
	 */
	public static <X> Event<LongFunction<X>> longFunction(BinaryOperator<X> reducer) {
		return new Event<>(
			(handlers) -> (long value) -> {
				X result = null;
				for(int i=0; i<handlers.size(); i++) {
					X cur = handlers.get(i).handler().apply(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives a reference-typed value and produces a boolean result
	 * @param <X> The input type for the Predicate event handlers
	 * @param reducer A function that turns multiple tests into a single event-result
	 * @return the new Event
	 */
	public static <X> Event<Predicate<X>> predicate(BooleanBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (X value) -> {
				boolean result = false;
				for(int i=0; i<handlers.size(); i++) {
					boolean cur = handlers.get(i).handler().test(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives a double value and produces a boolean result.
	 * @param reducer A function that turns multiple tests into a single event-result
	 * @return the new Event
	 */
	public static Event<DoublePredicate> doublePredicate(BooleanBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (double value) -> {
				boolean result = false;
				for(int i=0; i<handlers.size(); i++) {
					boolean cur = handlers.get(i).handler().test(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives an integer value and produces a boolean result.
	 * @param reducer A function that turns multiple tests into a single event-result
	 * @return the new Event
	 */
	public static Event<IntPredicate> intPredicate(BooleanBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (int value) -> {
				boolean result = false;
				for(int i=0; i<handlers.size(); i++) {
					boolean cur = handlers.get(i).handler().test(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an event that receives a long value and produces a boolean result.
	 * @param reducer A function that turns multiple tests into a single event-result
	 * @return the new Event
	 */
	public static Event<LongPredicate> longPredicate(BooleanBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (long value) -> {
				boolean result = false;
				for(int i=0; i<handlers.size(); i++) {
					boolean cur = handlers.get(i).handler().test(value);
					result = (i==0) ? cur : reducer.apply(result, cur);
				}
				
				return result;
			}
		);
	}
	
	
	
	
	
	
	
	public static Event<DoubleToIntFunction> doubleToIntFunction(IntBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (value) -> {
				boolean firstLoop = true;
				int result = 0;
				for(Entry<DoubleToIntFunction> entry : handlers) {
					int cur = entry.handler().applyAsInt(value);
					if (firstLoop) {
						result = cur;
						firstLoop = false;
					} else {
						result = reducer.applyAsInt(cur, result);
					}
				}
				
				return result;
			}
		);
	}
	
	public static Event<DoubleToLongFunction> doubleToLongFunction(LongBinaryOperator reducer) {
		return new Event<>(
			(handlers) -> (value) -> {
				boolean firstLoop = true;
				long result = 0;
				for(Entry<DoubleToLongFunction> entry : handlers) {
					long cur = entry.handler().applyAsLong(value);
					if (firstLoop) {
						result = cur;
						firstLoop = false;
					} else {
						result = reducer.applyAsLong(cur, result);
					}
				}
				
				return result;
			}
		);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static Event<IntBiConsumer> intBiConsumer() {
		return new Event<IntBiConsumer>(
				(handlers) -> (int x, int y) -> {
					for(Entry<IntBiConsumer> entry : handlers) {
						entry.handler().accept(x, y);
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
	public static <X, Y> Event<BiConsumer<X, Y>> biConsumer() {
		return new Event<>(
			(handlers) -> (X x, Y y) -> {
				for(Entry<BiConsumer<X, Y>> entry : handlers) {
					entry.handler().accept(x, y);
				}
			}
		);
	}
	
	/**
	 * Creates an event that Functions may respond to. All the responders are turned into a single value by recursively
	 * applying the reducer function for every two values returned.
	 * @param <T> The type of the first argument that event-handlers will receive
	 * @param <U> The type of the second argument that event-handlers will receive
	 * @param <V> The type of data that event-handlers will provide
	 * @param reducer a function which will turn responses into a single value
	 * @return the new Event
	 */
	public static <T, U, V> Event<BiFunction<T, U, V>> biFunction(BinaryOperator<V> reducer) {
		return new Event<>(
			(handlers) -> (T t, U u) -> {
				boolean firstLoop = true;
				V result = null;
				for(Entry<BiFunction<T, U, V>> entry : handlers) {
					V cur = entry.handler().apply(t, u);
					if (firstLoop) {
						result = cur;
						firstLoop = false;
					} else {
						reducer.apply(result, cur);
					}
				}
				
				return result;
			}
		);
	}
}
