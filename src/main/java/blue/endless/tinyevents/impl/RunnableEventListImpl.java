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

import blue.endless.tinyevents.RunnableEvent;

public final class RunnableEventListImpl implements RunnableEvent {
	
	private static record Entry(Runnable handler, Object key) {};
	private static record ExecutorEntry(Runnable handler, Executor executor, Object key) {};
	
	private List<Entry> entries = new ArrayList<>();
	private List<ExecutorEntry> executorEntries = new ArrayList<>();
	
	private Runnable[] bakedEntries = null;
	
	@Override
	public void fire() {
		if (bakedEntries!=null) {
			for(int i=0; i<bakedEntries.length; i++) {
				bakedEntries[i].run();
			}
		}
		
		if (executorEntries.size()>0) for(ExecutorEntry entry : executorEntries) {
			entry.executor().execute(entry.handler());
		}
		
	}

	@Override
	public void register(Runnable handler) {
		register(handler, handler);
	}

	@Override
	public void register(Runnable handler, Object key) {
		entries.add(new Entry(handler, key));
		bake();
	}

	@Override
	public void register(Runnable handler, Executor executor) {
		register(handler, executor, handler);
	}

	@Override
	public void register(Runnable handler, Executor executor, Object key) {
		executorEntries.add(new ExecutorEntry(handler, executor, key));
	}

	@Override
	public void unregister(Object key) {
		Iterator<Entry> i = entries.iterator();
		boolean rebake = false;
		while(i.hasNext()) {
			Entry entry = i.next();
			if (entry.key()==key) {
				i.remove();
				rebake = true;
			}
		}
		
		Iterator<ExecutorEntry> j = executorEntries.iterator();
		while(j.hasNext()) {
			ExecutorEntry entry = j.next();
			if (entry.key()==key) {
				j.remove();
			}
		}
		
		if (rebake) bake();
	}
	
	private void bake() {
		if (entries.size()==0) {
			bakedEntries = null;
		} else {
			bakedEntries = new Runnable[entries.size()];
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
