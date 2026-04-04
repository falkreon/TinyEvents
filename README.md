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


## Contributing

Pull requests are encouraged! Feel free to ping me in the Jankson discord and we can match your goals against the library's roadmap, and discuss a plan of action so your PR can be set up for success.

# Code Style

Generally, code in TinyEvents conforms to the Google Code Style, but with the following notable changes:

- Tabs are used for indentation, with spaces optionally used to align items within the same indent level. As a rule of thumb, if you change the tab width and some alignment breaks, you did it wrong. Space-based alginment is governed entirely by Google's style rules.
- Whitespace occurs on blank lines in the code that conforms to the indent level where the blank line is. It is not required to *create* such whitespace, but removing it is forbidden in PRs, except when those PRs are exclusively about formatting (and even those are likely to fail!).
- Use of the `var` keyword is forbidden except in test code. `var` can be a very useful tool for readability, especially when complex generics really pile up, but mixing var and non-var code can also cause a readability disaster. Var works best when you go all-in on it, and I'm not that sort of programmer.
- Star imports are forbidden, except for module imports. Star imports have a high risk of foreign code unintentionally changing how this code compiles.
- When you contribute code to TinyEvents, you permit your code to be provided under the MIT License just like everything else here.

# AI Policy

AI code contribution of any kind is strictly forbidden. If AI usage is suspected in a PR's code or comments it will be closed immediately at the sole discretion of the maintainer(s).

Copyright claims are a fundamental part of FOSS licenses. As AI output cannot be copyrighted, my view is that it is fundamentally incompatible with the MIT License. Additionally, even if you altered the AI output in such a way that the combined work product is copyrightable, you are required to disclaim the AI-generated portion. This creates a licensing minefield where it's difficult to reason about what parts of the work are copyrighted - and therefore FOSS - and what parts are not.

Additionally, programming models are trained unethically on stolen code, their base model training is environmentally unfriendly, and the output, so far, is extremely substandard. So even if the licensing problems were overcome, for ethical and practical reasons AI will still be forbidden here.

I shouldn't really need to say it, but permission is EXPRESSLY DENIED to use this code for AI training. This usage is NOT permitted by the MIT License, because you do not include the MIT copyright and permission notice in copies of the Software, and when the model recreates substantial portions of the Software (which it can), it does not recreate the notice.

# Conduct

Please refer to the [Contributor Covenant 3.0 Code of Conduct](https://www.contributor-covenant.org/version/3/0/code_of_conduct/).

We're all here to make, improve, and use good software. For that we need safety and mutual respect. Be kind to each other. Use peoples' preferred pronouns. Participate in good faith. Again, I shouldn't need to say any of this.
