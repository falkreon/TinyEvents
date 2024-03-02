# TinyEvents
Simple, high-performance events

## Importing
TinyEvents is now on Sonatype / Maven Central!

Make sure you have maven central declared in your repositories:

```groovy
repositories {
    mavenCentral()
}
```

and declare TinyEvents in your dependencies:

```groovy
dependencies {
    implementation group: 'blue.endless', name: 'TinyEvents', version: '2.0.0'
}
```

Refresh your gradle project and you should see TinyEvents pop up.

## Usage

In TinyEvents, an Event object is kind of like a delegate method holder. You fire events from that delegate, which results in all
listeners being called (the method call is "delegated" to them). This winds up being extremely fast in Java because no objects are created for a fired event, and value boxing can almost always be avoided. The Swing event model, by contrast, requires object creations, and can't keep up with the high event frequencies that TinyEvents can handle.


TinyEvents is also very simple to use. If you've used Fabric events, you probably already have a pretty good working understanding of everything you're going to see here.


Event has a generic type argument. This type argument is both the type of event-handlers, and also the type of the invoker used to fire events. This single invoker "stands in" for either no event handlers, one handler, or multiple handlers, and can just be invoked as if you were calling a handler directly.


For example, if you have an `Event<Consumer<String>>`, you can register a `Consumer<String>` as an event handler, and the invoker will also be a `Consumer<String>`.
A no-op event handler could be registered like:
```java
event.register((it) -> {});
```

And the event can be fired like:
```java
event.invoker().accept("stuff");
```


Registrations can also have "key objects" so that the event can later be unregistered. This is important because creating the exact same lambda at two different call sites will likely result in a different object. To unregister an event, you need to use the exact reference that you registered. If you register in the "simple" way, with a single handler object, but save that object reference, it can still be used as the key to unregister it.


There are many, many kinds of Events. A good place to start is to browse the factories in `EventFactories` and `ChainEventFactories`. Or you can create a new kind of event by supplying it with an invoker-factory - a lambda that takes in a list of handler entries and produces a method that can call them.
An invokerFactory for `Consumer<X>` might look like:
```java
(handlers) -> (X value) -> {
	for(Entry<Consumer<X>> entry : handlers) {
		entry.handler().accept(value);
	}
}
```


If this looks too complicated, that's because it is. Unfortunately, I can't auto-generate an invoker without resorting to the reflection and boxing that I'm trying to avoid. Feel free to use the builtins, or ask for help on the [Jankson discord](https://discord.gg/tV6FYXE8QH)
