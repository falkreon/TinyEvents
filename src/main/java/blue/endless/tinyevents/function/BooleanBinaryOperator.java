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
