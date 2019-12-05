package com.openwebstart.proxy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubnetUtils {

    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,2})"; // 0 -> 32
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
    private static final int NBITS = 32;
    private final int network;
    private final int broadcast;

    /**
     * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
     *
     * @param cidrNotation A CIDR-notation string, e.g. "192.168.0.1/16"
     * @throws IllegalArgumentException if the parameter is invalid,
     *                                  i.e. does not match n.n.n.n/m where n=1-3 decimal digits, m = 1-2 decimal digits in range 0-32
     */
    public SubnetUtils(String cidrNotation) {
        Matcher matcher = cidrPattern.matcher(cidrNotation);
        if (matcher.matches()) {
            final int address = matchAddress(matcher);

            /* Create a binary netmask from the number of bits specification /x */
            int trailingZeroes = NBITS - rangeCheck(Integer.parseInt(matcher.group(5)), NBITS);

            /*
             * An IPv4 netmask consists of 32 bits, a contiguous sequence
             * of the specified number of ones followed by all zeros.
             * So, it can be obtained by shifting an unsigned integer (32 bits) to the left by
             * the number of trailing zeros which is (32 - the # bits specification).
             * Note that there is no unsigned left shift operator, so we have to use
             * a long to ensure that the left-most bit is shifted out correctly.
             */
            final int netmask = (int) (0x0FFFFFFFFL << trailingZeroes);

            /* Calculate base network address */
            this.network = (address & netmask);

            /* Calculate broadcast address */
            this.broadcast = network | ~(netmask);
        } else {
            throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
        }
    }

    /**
     * Convenience container for subnet summary information.
     */
    public final class SubnetInfo {

        /* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
        private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;

        private SubnetInfo() {
        }

        // long versions of the values (as unsigned int) which are more suitable for range checking
        private long networkLong() {
            return network & UNSIGNED_INT_MASK;
        }

        private long broadcastLong() {
            return broadcast & UNSIGNED_INT_MASK;
        }

        private int low() {
            return broadcastLong() - networkLong() > 1 ? network + 1 : 0;
        }

        private int high() {
            return broadcastLong() - networkLong() > 1 ? broadcast - 1 : 0;
        }

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses.
         *
         * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1"
         * @return True if in range, false otherwise
         */
        public boolean isInRange(String address) {
            return isInRange(toInteger(address));
        }

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses.
         *
         * @param address the address to check
         * @return true if it is in range
         * @since 3.4 (made public)
         */
        private boolean isInRange(int address) {
            if (address == 0) { // cannot ever be in range; rejecting now avoids problems with CIDR/31,32
                return false;
            }
            long addLong = address & UNSIGNED_INT_MASK;
            long lowLong = low() & UNSIGNED_INT_MASK;
            long highLong = high() & UNSIGNED_INT_MASK;
            return addLong >= lowLong && addLong <= highLong;
        }

    }

    /**
     * Return a {@link SubnetInfo} instance that contains subnet-specific statistics
     *
     * @return new instance
     */
    public final SubnetInfo getInfo() {
        return new SubnetInfo();
    }

    /*
     * Convert a dotted decimal format address to a packed integer format
     */
    private static int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        } else {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }
    }

    /*
     * Convenience method to extract the components of a dotted decimal address and
     * pack into an integer using a regex match
     */
    private static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; ++i) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 255));
            addr |= ((n & 0xff) << 8 * (4 - i));
        }
        return addr;
    }

    /*
     * Convenience function to check integer boundaries.
     * Checks if a value x is in the range [begin,end].
     * Returns x if it is in range, throws an exception otherwise.
     */
    private static int rangeCheck(int value, int end) {
        if (value >= 0 && value <= end) { // (begin,end]
            return value;
        }
        throw new IllegalArgumentException("Value [" + value + "] not in range [" + 0 + "," + end + "]");
    }
}
