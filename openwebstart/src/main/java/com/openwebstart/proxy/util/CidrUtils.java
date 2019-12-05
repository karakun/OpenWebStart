package com.openwebstart.proxy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CidrUtils {

    private static final String IP_V4_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String CIDR_V4_ADDRESS = "(\\d{1,3})(?:\\.(\\d{1,3}))?(?:\\.(\\d{1,3}))?(?:\\.(\\d{1,3}))?/(\\d{1,2})";
    private static final long ALL_ONES = 0x0_FF_FF_FF_FFL;
    private static final int MASK_SIZE_MAX = 32;

    private static final Pattern ipv4Pattern = Pattern.compile(IP_V4_ADDRESS);
    private static final Pattern cidrv4Pattern = Pattern.compile(CIDR_V4_ADDRESS);

    public static boolean isCidrNotation(final String value) {
        final Matcher cidrMatcher = cidrv4Pattern.matcher(value);
        return cidrMatcher.matches();
    }

    public static boolean isInRange(final String cidrv4Notation, final String ipv4Notation) {
        final Matcher cidrMatcher = cidrv4Pattern.matcher(cidrv4Notation);
        if (cidrMatcher.matches()) {
            final Matcher ipMatcher = ipv4Pattern.matcher(ipv4Notation);
            if (ipMatcher.matches()) {
                final long ipAddress = ipv4AsLong(ipMatcher);
                if (ipAddress == 0) {
                    return false;
                }
                final long netmask = calcNetmask(cidrMatcher);
                final long ipNetwork = (ipAddress & netmask);
                final long cidrAddress = ipv4AsLong(cidrMatcher);
                final long cidrNetwork = (cidrAddress & netmask);

                return ipNetwork == cidrNetwork;
            } else {
                throw new IllegalArgumentException("Could not parse '" + ipv4Notation + "'");
            }
        } else {
            throw new IllegalArgumentException("Could not parse '" + cidrv4Notation + "'");
        }
    }

    private static long calcNetmask(final Matcher matcher) {
        final int lastGroup = matcher.groupCount();
        final int leadingOnes = Integer.parseInt(matcher.group(lastGroup));
        checkValueInRange(leadingOnes, MASK_SIZE_MAX);
        final int trailingZeroes = MASK_SIZE_MAX - leadingOnes;
        return (ALL_ONES << trailingZeroes) & ALL_ONES;
    }

    private static long ipv4AsLong(Matcher matcher) {
        final int missingGroups = 5 - Stream.of(1, 2, 3, 4).filter(i -> matcher.group(i) == null).findFirst().orElse(5);
        long addr = 0;
        for (int i = 1; i <= 4 - missingGroups; ++i) {
            final int n = Integer.parseInt(matcher.group(i));
            checkValueInRange(n, 255);
            addr |= ((long) n) << 8 * (4 - i);
        }
        return addr;
    }

    private static void checkValueInRange(int value, int end) {
        if (value >= 0 && value <= end) {
            return;
        }
        throw new IllegalArgumentException("Value '" + value + "' not in range 0 - " + end);
    }
}
