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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a high-performance event which cannot be safely accessed off-thread.
 */
public class Event<T> {
	public static record Entry<T>(T handler, Object key) {}
	
	private final T invoker;
	protected final List<Entry<T>> handlers = new ArrayList<>();
	
	/**
	 * Creates a new event using the specified invoker factory.
	 * @param invokerFactory A function which will be called to turn a list of handlers into an invoker object.
	 */
	public Event(Function<List<Entry<T>>, T> invokerFactory) {
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
}
