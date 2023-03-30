/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Falkreon (Isaac Ellingson)
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
import java.util.function.BiConsumer;

import blue.endless.tinyevents.BiConsumerEvent;

public class BiConsumerEventListImpl<T, U> implements BiConsumerEvent<T, U> {

	private record Entry<T, U>(BiConsumer<T, U> handler, Object key) {};
	private record ExecutorEntry<T, U>(BiConsumer<T, U> handler, Executor executor, Object key) {};
	
	private List<Entry<T, U>> entries = new ArrayList<>();
	private List<ExecutorEntry<T, U>> executorEntries = new ArrayList<>();
	
	private BiConsumer<T, U>[] bakedEntries = null;
	
	@Override
	public void fire(T t, U u) {
		if (bakedEntries!=null) {
			for(int i=0; i<bakedEntries.length; i++) {
				bakedEntries[i].accept(t, u);
			}
		}
		
		if (executorEntries.size()>0) for(ExecutorEntry<T, U> entry : executorEntries) {
			entry.executor().execute( ()->entry.handler.accept(t, u));
		}
	}

	@Override
	public void register(BiConsumer<T, U> handler) {
		register(handler, handler);
	}

	@Override
	public void register(BiConsumer<T, U> handler, Object key) {
		entries.add(new Entry<>(handler, key));
		bake();
	}

	@Override
	public void register(BiConsumer<T, U> handler, Executor executor) {
		register(handler, executor, handler);
	}

	@Override
	public void register(BiConsumer<T, U> handler, Executor executor, Object key) {
		executorEntries.add(new ExecutorEntry<T, U>(handler, executor, key));
		
	}

	@Override
	public void unregister(Object key) {
		Iterator<Entry<T, U>> i = entries.iterator();
		boolean rebake = false;
		while(i.hasNext()) {
			Entry<T, U> entry = i.next();
			if (entry.key()==key) {
				i.remove();
				rebake = true;
			}
		}
		
		Iterator<ExecutorEntry<T, U>> j = executorEntries.iterator();
		while(j.hasNext()) {
			ExecutorEntry<T, U> entry = j.next();
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
			bakedEntries = new BiConsumer[entries.size()]; //Cannot make a generic array but that's never stopped me before
			for(int i=0; i<entries.size(); i++) {
				bakedEntries[i] = entries.get(i).handler();
			}
		}
	}

	@Override
	public void clear() {
		entries.clear();
		executorEntries.clear();
		bakedEntries = null;
	}

}
