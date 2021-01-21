import java.math.BigInteger;

public class FuelInjection {
    public static final BigInteger TWO = new BigInteger("2");
    public static final BigInteger ONE = new BigInteger("1");

    // Accepts a string representing an extremely large number and returns the
    // smallest number of valid operations required to reduce it to 1. The
    // only valid operations are ADDING/SUBTRACTING 1 or DIVIDING an
    // even number by 2.
    public static int solution(String x) {
        BigInteger bigX = new BigInteger(x);
        BigInteger counter = BigInteger.ZERO;
        // While bigX is not a power of 2.
        while (bigX.bitCount() != 1) {
            // If bigX is odd figure out if it is best to add or subtract 1.
            // This is determined by whether or not the respective operation
            // followed by a divide-by-2 will result in an even number.
            // If bigX is even just go ahead and divide by 2.
            if (bigX.testBit(0)) {
                if (!bigX.testBit(1) || (bigX.bitLength() == 2 && bigX.testBit(0))) {
                    bigX = bigX.subtract(ONE);
                } else {
                    bigX = bigX.add(ONE);
                }
		counter = counter.add(ONE);
            }
	    bigX = bigX.shiftRight(1);
            counter = counter.add(ONE);
        }
        // We have reached a power of 2. Add the number of zeros in the binary
        // representation of bigX to the counter as this is indicative of how
        // many divide-by-2's are required to reach 1.
        counter = counter.add(new BigInteger(Integer.toString(bigX.bitLength() - 1)));
        return counter.intValue();
    }
}