package com.openwebstart.proxy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CidrUtils {

    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,2})";
    private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;
    private static final int MASK_SIZE_MAX = 32;

    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);

    public static boolean isInRange(final String cidrNotation, final String ipAddress) {
        final String[] split = cidrNotation.split(Pattern.quote("/"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Bad cidr notation: '" + cidrNotation + "'");
        }
        final String addressPart = split[0];
        final String[] addressSplit = addressPart.split(Pattern.quote("."));
        if(addressSplit.length == 1) {
            return isInRangeCorrectNotation(addressPart + ".0.0.0" + "/" + split[2], ipAddress);
        } else if(addressSplit.length == 2) {
            return isInRangeCorrectNotation(addressPart + ".0.0" + "/" + split[2], ipAddress);
        } else if(addressSplit.length == 3) {
            return isInRangeCorrectNotation(addressPart + ".0" + "/" + split[2], ipAddress);
        } else if(addressSplit.length == 4) {
            return isInRangeCorrectNotation(cidrNotation, ipAddress);
        } else {
            throw new IllegalArgumentException("Bad cidr notation: '" + cidrNotation + "'");
        }
    }

    private static boolean isInRangeCorrectNotation(final String cidrNotation, final String ipAddress) {
        final Matcher matcher = cidrPattern.matcher(cidrNotation);
        if (matcher.matches()) {
            final long address = matchAddress(matcher);
            final long netmask = calcNetmask(matcher);
            final long network = (address & netmask);
            final long broadcast = network | ~(netmask);

            final Matcher ipAdressMatcher = addressPattern.matcher(ipAddress);
            if(ipAdressMatcher.matches()) {
                final long ipAddressAsInt = matchAddress(ipAdressMatcher);
                if (ipAddressAsInt == 0) {
                    return false;
                }
                final long low = (broadcast & UNSIGNED_INT_MASK) - (network & UNSIGNED_INT_MASK) > 1 ? network + 1 : 0;
                final long high = (broadcast & UNSIGNED_INT_MASK) - (network & UNSIGNED_INT_MASK) > 1 ? broadcast - 1 : 0;
                final long addLong = address & UNSIGNED_INT_MASK;
                final long lowLong = low & UNSIGNED_INT_MASK;
                final long highLong = high & UNSIGNED_INT_MASK;
                return addLong >= lowLong && addLong <= highLong;
            } else {
                throw new IllegalArgumentException("Could not parse '" + ipAddress + "'");
            }
        } else {
            throw new IllegalArgumentException("Could not parse '" + cidrNotation + "'");
        }
    }

    private static long calcNetmask(final Matcher matcher) {
        final int maskSize = Integer.parseInt(matcher.group(5));
        checkValueInRange(maskSize, MASK_SIZE_MAX);
        final int trailingZeroes = MASK_SIZE_MAX - maskSize;
        return UNSIGNED_INT_MASK << trailingZeroes;
    }

    private static long matchAddress(Matcher matcher) {
        long addr = 0;
        for (int i = 1; i <= 4; ++i) {
            final int n = Integer.parseInt(matcher.group(i));
            checkValueInRange(n, 255);
            addr |= n << 8 * (4 - i);
        }
        return addr;
    }

    private static void checkValueInRange(int value, int end) {
        if (value >= 0 && value <= end) {
            return;
        }
        throw new IllegalArgumentException("Value [" + value + "] not in range [" + 0 + "," + end + "]");
    }
}
