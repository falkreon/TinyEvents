/*
 * MIT License
 *
 * Copyright (c) 2021-2024 Falkreon (Isaac Ellingson)
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

import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

import blue.endless.tinyevents.Event.Entry;
import blue.endless.tinyevents.function.BooleanUnaryOperator;

/**
 * Contains EventFactories and static factory methods for making Events which have the same inputs and outputs, and
 * whose handlers "progressively modify" a value to create a result. There is no priority system for these events, and
 * no handler can count on having the final say in the result.
 * 
 * <p><b>Example:</b><br>
 * A jump-strength-event. Let's say the event is an {@code Event<IntUnaryOperator>}, and that there are
 * two handlers registered:
 * 
 * <ul>
 *   <p>{@code (x) -> x + 1}
 *   <p>{@code (x) -> x / 2}
 * </ul>
 * 
 * The first handler receives the value 5, and adds one, yielding 6. The second one receives 6,
 * and halves the value, yielding 3. The event's invoker returns with the value 3. Each handler receives the value "so
 * far", and modifies it.
 */
public class ChainEventFactories {
	public static final EventFactory<IntUnaryOperator>      MODIFY_INT    = ChainEventFactories::intUnaryOperator;
	public static final EventFactory<LongUnaryOperator>     MODIFY_LONG   = ChainEventFactories::longUnaryOperator;
	public static final EventFactory<DoubleUnaryOperator>   MODIFY_DOUBLE = ChainEventFactories::doubleUnaryOperator;
	public static final EventFactory<BooleanUnaryOperator>  MODIFY_BOOLEAN= ChainEventFactories::booleanUnaryOperator;
	public static final EventFactory<UnaryOperator<String>> MODIFY_STRING = () -> unaryOperator();
	
	/**
	 * Creates an Event which takes in a single value and modifies it.
	 * @param <T> The type of data that will be modified
	 * @return the new Event
	 */
	public static <T> Event<UnaryOperator<T>> unaryOperator() {
		return new Event<UnaryOperator<T>>(
			(handlers) -> (T t) -> {
				T result = t;
				
				for(Entry<UnaryOperator<T>> entry : handlers) {
					result = entry.handler().apply(result);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event which takes in an int value and modifies it.
	 * @return the new Event
	 */
	public static Event<IntUnaryOperator> intUnaryOperator() {
		return new Event<IntUnaryOperator>(
			(handlers) -> (int value) -> {
				int result = value;
				
				for(Entry<IntUnaryOperator> entry : handlers) {
					result = entry.handler().applyAsInt(result);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event which takes in a long value and modifies it.
	 * @return the new Event
	 */
	public static Event<LongUnaryOperator> longUnaryOperator() {
		return new Event<LongUnaryOperator>(
			(handlers) -> (long value) -> {
				long result = value;
				
				for(Entry<LongUnaryOperator> entry : handlers) {
					result = entry.handler().applyAsLong(result);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event which takes in a double value and modifies it.
	 * @return the new Event
	 */
	public static Event<DoubleUnaryOperator> doubleUnaryOperator() {
		return new Event<DoubleUnaryOperator>(
			(handlers) -> (double value) -> {
				double result = value;
				
				for(Entry<DoubleUnaryOperator> entry : handlers) {
					result = entry.handler().applyAsDouble(result);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event which takes in a boolean value and modifies it.
	 * @return the new Event
	 */
	public static Event<BooleanUnaryOperator> booleanUnaryOperator() {
		return new Event<BooleanUnaryOperator>(
			(handlers) -> (boolean value) -> {
				boolean result = value;
				
				for(Entry<BooleanUnaryOperator> entry : handlers) {
					result = entry.handler().applyAsBoolean(result);
				}
				
				return result;
			}
		);
	}
	
	/**
	 * Creates an Event which takes in two values and modifies the first one.
	 * @param <T> The type of data that will be modified
	 * @param <U> The type of the second function argument that will NOT be replaced/modified
	 * @return The fully-modified value.
	 */
	public static <T, U> Event<BiFunction<T, U, T>> biFunction() {
		return new Event<BiFunction<T, U, T>>(
			(handlers) -> (T t, U u) -> {
				T result = t;
				
				for(Entry<BiFunction<T, U, T>> entry : handlers) {
					result = entry.handler().apply(result, u);
				}
				
				return result;
			}
		);
	}
}
