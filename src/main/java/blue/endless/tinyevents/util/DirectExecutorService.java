package blue.endless.tinyevents.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DirectExecutorService implements ExecutorService {
	
	private boolean closed = false;
	
	@Override
	public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
		return closed;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
		List<Future<T>> futures = new ArrayList<>();
		for(Callable<T> t : callables) {
			try {
				futures.add(CompletableFuture.completedFuture(t.call()));
			} catch (Exception ex) {
				futures.add(CompletableFuture.failedFuture(ex));
			}
		}
		
		return futures;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long timeout, TimeUnit timeUnit) throws InterruptedException {
		return invokeAll(callables);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws InterruptedException, ExecutionException {
		if (callables.isEmpty()) throw new ExecutionException("Cannot return result of a zero-length list", new ArrayIndexOutOfBoundsException());
		try {
			return (T) callables.stream().findFirst().get();
		} catch (Exception ex) {
			throw new ExecutionException("There was an error calling the handler", ex);
		}
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> callables, long arg1, TimeUnit arg2) throws InterruptedException, ExecutionException, TimeoutException {
		return invokeAny(callables);
	}

	@Override
	public boolean isShutdown() {
		return closed;
	}

	@Override
	public boolean isTerminated() {
		return closed;
	}

	@Override
	public void shutdown() {
		closed = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		closed = true;
		return new ArrayList<>();
	}

	@Override
	public <T> Future<T> submit(Callable<T> c) {
		try {
			return CompletableFuture.completedFuture(c.call());
		} catch (Exception ex) {
			return CompletableFuture.failedFuture(ex);
		}
	}

	@Override
	public Future<Void> submit(Runnable r) {
		r.run();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public <T> Future<T> submit(Runnable r, T t) {
		r.run();
		return CompletableFuture.completedFuture(t);
	}

	@Override
	public void execute(Runnable r) {
		r.run();
	}

}
