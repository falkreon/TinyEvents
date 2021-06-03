/*
 * MIT License
 *
 * Copyright (c) 2021 Falkreon (Isaac Ellingson)
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

package blue.endless.tinyevents.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ConsumerEventListImpl<T> implements ConsumerEvent<T> {

	private record Entry<T>(Consumer<T> handler, Object key) {};
	private record ExecutorEntry<T>(Consumer<T> handler, Executor executor, Object key) {};
	
	private List<Entry<T>> entries = new ArrayList<>();
	private List<ExecutorEntry<T>> executorEntries = new ArrayList<>();
	
	private Consumer<T>[] bakedEntries = null;
	
	@Override
	public void fire(T t) {
		if (bakedEntries!=null) {
			for(int i=0; i<bakedEntries.length; i++) {
				bakedEntries[i].accept(t);
			}
		}
		
		if (executorEntries.size()>0) for(ExecutorEntry<T> entry : executorEntries) {
			entry.executor().execute( ()->entry.handler.accept(t));
		}
	}

	@Override
	public void register(Consumer<T> handler) {
		register(handler, handler);
	}

	@Override
	public void register(Consumer<T> handler, Object key) {
		entries.add(new Entry<>(handler, key));
		bake();
	}

	@Override
	public void register(Consumer<T> handler, Executor executor) {
		register(handler, executor, handler);
	}

	@Override
	public void register(Consumer<T> handler, Executor executor, Object key) {
		executorEntries.add(new ExecutorEntry<T>(handler, executor, key));
	}

	@Override
	public void unregister(Object key) {
		Iterator<Entry<T>> i = entries.iterator();
		boolean rebake = false;
		while(i.hasNext()) {
			Entry<T> entry = i.next();
			if (entry.key()==key) {
				i.remove();
				rebake = true;
			}
		}
		
		Iterator<ExecutorEntry<T>> j = executorEntries.iterator();
		while(j.hasNext()) {
			ExecutorEntry<T> entry = j.next();
			if (entry.key()==key) {
				j.remove();
			}
		}
		
		if (rebake) bake();
	}
	
	@SuppressWarnings("unchecked")
	private void bake() {
		if (entries.size()==0) {
			bakedEntries = null;
		} else {
			bakedEntries = new Consumer[entries.size()]; //Cannot make a generic array but that's never stopped me before
			for(int i=0; i<entries.size(); i++) {
				bakedEntries[i] = entries.get(i).handler();
			}
		}
	}

}
