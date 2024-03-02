package blue.endless.tinyevents;

@FunctionalInterface
public interface EventFactory<T> {
	public Event<T> create();
}
