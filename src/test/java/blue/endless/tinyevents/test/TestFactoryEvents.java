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

package blue.endless.tinyevents.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import blue.endless.tinyevents.EventFactories;

public class TestFactoryEvents {
	public static final double MIN_ACCURACY = 0.000001D;
	
	/**
	 * Tests basic supplier functionality, plus a few corner cases in supplier events.
	 * 
	 * <ul>
	 *   <li>The FAVOR_TRUE BooleanSupplier should OR the results together, resulting in true if ANY handler returns true.
	 *   <li>The default object supplier specifies that handlers that are registered later will override earlier handlers.
	 * </ul>
	 */
	@Test
	public void testSupplierOverrideBehavior() {
		var booleanSupplier = EventFactories.BOOLEAN_SUPPLIER_OR.create();
		booleanSupplier.register(() -> true);
		booleanSupplier.register(() -> false);
		Assertions.assertEquals(true, booleanSupplier.invoker().getAsBoolean());
		
		var stringSupplier = EventFactories.STRING_SUPPLIER.create();
		stringSupplier.register(() -> "BAD");
		stringSupplier.register(() -> "OK");
		Assertions.assertEquals("OK", stringSupplier.invoker().get());
		
		var doubleSupplier = EventFactories.DOUBLE_SUPPLIER_MULTIPLICATIVE.create();
		doubleSupplier.register(() -> 2.0D);
		doubleSupplier.register(() -> 3.0D);
		Assertions.assertEquals(6.0D, doubleSupplier.invoker().getAsDouble(), MIN_ACCURACY);
		
		doubleSupplier = EventFactories.DOUBLE_SUPPLIER_CUMULATIVE.create();
		doubleSupplier.register(() -> 2.0D);
		doubleSupplier.register(() -> 3.0D);
		Assertions.assertEquals(5.0D, doubleSupplier.invoker().getAsDouble(), MIN_ACCURACY);
	}
	
	/**
	 * Makes sure simple primitive consumers and object consumers properly invoke their handlers by testing them
	 * end-to-end.
	 */
	@Test
	public void testConsumers() {
		String[] s = { "BAD" };
		var stringConsumer = EventFactories.STRING_CONSUMER.create();
		stringConsumer.register((it) -> s[0] = it);
		stringConsumer.invoker().accept("OK");
		Assertions.assertEquals("OK", s[0]);
		
		double[] d = { -1D };
		var doubleConsumer = EventFactories.DOUBLE_CONSUMER.create();
		doubleConsumer.register((it) -> d[0] = it);
		doubleConsumer.invoker().accept(3.14D);
		Assertions.assertEquals(3.14D, d[0], MIN_ACCURACY);
		
		int[] i = { -1 };
		var intConsumer = EventFactories.INT_CONSUMER.create();
		intConsumer.register((it) -> i[0] = it);
		intConsumer.invoker().accept(23);
		Assertions.assertEquals(23, i[0]);
		
		long[] l = { -1L };
		var longConsumer = EventFactories.LONG_CONSUMER.create();
		longConsumer.register((it) -> l[0] = it);
		longConsumer.invoker().accept(23L);
		Assertions.assertEquals(23L, l[0]);
	}
}
