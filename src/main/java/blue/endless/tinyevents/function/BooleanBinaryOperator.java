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

package blue.endless.tinyevents.function;

/**
 * Primitive specialization of {@code BinaryOperator<Boolean>}. Can also be treated as a primitive specialization of
 * {@code BiPredicate<Boolean, Boolean>} or {@code Function<Boolean, Boolean, Boolean>}.
 */
@FunctionalInterface
public interface BooleanBinaryOperator {
	public boolean apply(boolean a, boolean b);
	
	
	public static final BooleanBinaryOperator AND = (a, b) -> a && b;
	public static final BooleanBinaryOperator OR  = (a, b) -> a || b;
	public static final BooleanBinaryOperator XOR = (a, b) -> a  ^ b;
}
