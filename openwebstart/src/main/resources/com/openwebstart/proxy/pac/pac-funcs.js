/*
 * These helper functions are required to be able to parse Proxy Auto Config
 * (PAC) files. PAC files will use these helper functions to decide the best
 * proxy for connecting to a host.
 *
 * This implementation is based on the description of the functions at:
 * http://web.archive.org/web/20060424005037/wp.netscape.com/eng/mozilla/2.0/relnotes/demo/proxy-live.html
 */

/**
 * Returns true if the host does not contain a domain (there are no dots)
 */
function isPlainHostName(host) {
    if (host.indexOf(".") === -1) {
        return true;
    } else {
        return false;
    }
}

/**
 * Returns true if the host is part of the domain (the host ends with domain)
 */
function dnsDomainIs(host, domain) {
    var loc = host.lastIndexOf(domain);
    if (loc === -1) {
        return false;
    }
    if (loc + domain.length === host.length) {
        // host ends with domain
        return true;
    }
    return false;
}

/**
 * Returns true if the host is an exact match of hostdom or if host is not a
 * fully qualified name but has the same hostname as hostdom
 */
function localHostOrDomainIs(host, hostdom) {
    if (host === hostdom) {
        // exact match
        return true;
    }
    var firstdot = hostdom.indexOf(".");
    if (firstdot === -1) {
        // hostdom has no dots
        return false;
    }
    if (host === hostdom.substring(0, firstdot)) {
        // hostname matches
        return true;
    }
    return false;
}

/**
 * Returns true if the host name can be resolved.
 */
function isResolvable(host) {
    try {
        java.net.InetAddress.getByName(host);
        return true;
    } catch (e) {
        //if (e.javaException instanceof java.net.UnknownHostException) {
        return false;
        //} else {
        // 	throw e;
        //}
    }
}

/**
 * Return true if the ip address of the host matches the pattern given the mask.
 */
function isInNet(host, pattern, mask) {
    if (!isResolvable(host)) {
        return false;
    }

    var hostIp = dnsResolve(host);
    var hostParts = hostIp.split(".");
    var patternParts = pattern.split(".");
    var maskParts = mask.split(".");

    if (hostParts.length !== 4 ||
        patternParts.length !== hostParts.length ||
        maskParts.length !== hostParts.length) {
        return false;
    }

    var matched = true;
    for (var i = 0; i < hostParts.length; i++) {
        var partMatches = (hostParts[i] & maskParts[i]) === (patternParts[i] & maskParts[i]);
        matched = matched && partMatches;
    }

    return matched;
}

/**
 * Returns the IP address of the host as a string
 */
function dnsResolve(host) {
    return java.net.InetAddress.getByName(host).getHostAddress() + "";
}

/**
 * Returns the local IP address
 */
function myIpAddress() {
    return java.net.InetAddress.getLocalHost().getHostAddress() + "";
}

/**
 * Returns the number of domains in a hostname
 */
function dnsDomainLevels(host) {
    var levels = 0;
    for (var i = 0; i < host.length; i++) {
        if (host[i] === '.') {
            levels++;
        }
    }
    return levels;
}

/**
 * Returns true if the shell expression matches the given input string
 */
function shExpMatch(str, shExp) {

    // TODO support all special characters
    // right now we support only * and ?


    try {

        // turn shExp into a regular expression

        var work = "";

        // escape characters
        for (var i = 0; i < shExp.length; i++) {
            var ch = shExp[i];
            switch (ch) {
                case "\\": work = work + "\\\\"; break;
                case "^": work = work + "\\^"; break;
                case "$": work = work + "\\$"; break;
                case "+": work = work + "\\+"; break;
                case ".": work = work + "\\."; break;
                case "(": work = work + "\\("; break;
                case ")": work = work + "\\)"; break;
                case "{": work = work + "\\{"; break;
                case "}": work = work + "\\}"; break;
                case "[": work = work + "\\["; break;
                case "]": work = work + "\\]"; break;

                case "?": work = work + ".{1}"; break;
                case "*": work = work + ".*"; break;

                default:
                    work = work + ch;
            }

        }

        var regExp = "^" +  work + "$";

        // match
        //java.lang.System.out.println("")
        //java.lang.System.out.println("Input String  : " + str);
        //java.lang.System.out.println("Input Pattern : " + shExp);
        //java.lang.System.out.println("RegExp        : " + regExp.toString());
        var match = str.match(regExp);

        if (match === null) {
            return false;
        } else {
            return true;
        }


    } catch (e) {
        return false;
    }

}


/**
 * Returns true if the current weekday matches the desired weekday(s)
 *
 * Possible ways of calling:
 *   weekdayRange(wd1);
 *   weekdayRange(wd1, "GMT");
 *   weekdayRange(wd1, wd2);
 *   weekdayRange(wd1, wd2, "GMT");
 *
 * Where wd1 and wd2 are one of "SUN", "MON", "TUE", "WED", "THU", "FRI" and
 * "SAT"
 *
 * The argument "GMT", if present, is always the last argument
 */
function weekdayRange() {
    var wd1;
    var wd2;
    var gmt = false;

    function isWeekDay(day) {
        if (day === "SUN" || day === "MON" || day === "TUE" || day === "WED" ||
            day === "THU" || day === "FRI" || day === "SAT") {
            return true;
        }
        return false;
    }

    function strToWeekDay(str) {
        switch (str) {
            case "SUN": return 0;
            case "MON": return 1;
            case "TUE": return 2;
            case "WED": return 3;
            case "THU": return 4;
            case "FRI": return 5;
            case "SAT": return 6;
            default: return 0;
        }
    }

    if (arguments.length > 1) {
        if (arguments[arguments.length-1] === "GMT") {
            gmt = true;
            arguments.splice(0,arguments.length-1);
        }
    }

    if (arguments.length === 0) { return false; }

    wd1 = arguments[0];

    if (!isWeekDay(wd1)) { return false; }

    var today = new Date().getDay();
    if (arguments.length >= 2) {
        // return true if current weekday is between wd1 and wd2 (inclusive)
        wd2 = arguments[1];
        if (!isWeekDay(wd2)) { return false; }

        var day1 = strToWeekDay(wd1);
        var day2 = strToWeekDay(wd2);

        if (day1 <= day2) {
            if ( day1 <= today && today <= day2) {
                return true;
            }
            return false;
        } else {
            if (day1 <= today || today <= day2) {
                return true;
            }
            return false;
        }
    } else {
        // return true if the current weekday is wd1
        if (strToWeekDay(wd1) === today) {
            return true;
        }
        return false;
    }
}

/**
 * Returns true if the current date matches the given date(s)
 *
 * Possible ways of calling:
 *   dateRange(day)
 *   dateRange(day1, day2)
 *   dateRange(month)
 *   dateRange(month1, month2)
 *   dateRange(year)
 *   dateRange(year1, year2)
 *   dateRange(day1, month1, day2, month2)
 *   dateRange(month1, year1, month2, year2)
 *   dateRange(day1, month1, year1, day2, month2, year2)
 *
 * The parameter "GMT" may additionally be passed as the last argument in any
 * of the above ways of calling.
 */
function dateRange() {
    switch (arguments.length) {
        case 1: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0]);
        case 2: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1]);
        case 3: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1],arguments[2]);
        case 4: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1],arguments[2],arguments[3]);
        case 5: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1],arguments[2],arguments[3],arguments[4]);
        case 6: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5]);
        case 7: return isDateInRange_internallForIcedTeaWebTesting(new Date(),arguments[0],arguments[1],arguments[2],arguments[3],arguments[4],arguments[5],arguments[6]); //GMT
        default:
            return false;
    }
}

function isDateInRange_internallForIcedTeaWebTesting() {

    function isDate(date) {
        if (typeof(date) === 'number' && (date <= 31 && date >= 1)) {
            return true;
        }
        return false;
    }

    function strToMonth(month) {
        switch (month) {
            case "JAN": return 0;
            case "FEB": return 1;
            case "MAR": return 2;
            case "APR": return 3;
            case "MAY": return 4;
            case "JUN": return 5;
            case "JUL": return 6;
            case "AUG": return 7;
            case "SEP": return 8;
            case "OCT": return 9;
            case "NOV": return 10;
            case "DEC": return 11;
            default: return 0;
        }
    }

    function isMonth(month) {
        if (month === "JAN" || month === "FEB" || month === "MAR" ||
            month === "APR" || month === "MAY" || month === "JUN" ||
            month === "JUL" || month === "AUG" || month === "SEP" ||
            month === "OCT" || month === "NOV" || month === "DEC") {
            return true;
        }
        return false;
    }

    function isYear(year) {
        if (typeof(year) === 'number') {
            return true;
        }
        return false;
    }

    function inDateRange(today, date1, date2) {
        if (date1 <= date2) {
            if (date1 <= today.getDate() && today.getDate() <= date2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (date1 <= today.getDate() || today.getDate() <= date2) {
                return true;
            } else {
                return false;
            }
        }
    }

    function inMonthRange(today, month1, month2) {
        if (month1 <= month2) {
            if (month1 <= today.getMonth() && today.getMonth() <= month2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (month1 <= today.getMonth() || today.getMonth() <= month2) {
                return true;
            } else {
                return false;
            }
        }
    }

    function inYearRange(today, year1, year2) {
        if (year1 <= today.getFullYear() && today.getFullYear() <= year2) {
            return true;
        } else {
            return false;
        }
    }

    function inMonthDateRange(today, date1, month1, date2, month2) {
        if (month1 === month2) {
            if (today.getMonth() === month1) {
                if (date1 <= today.getDate() && today.getDate() <= date2) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (date1 <= date2) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if (month1 < month2) {
            if (month1 <= today.getMonth() && today.getMonth() <= month2) {
                if (today.getMonth() === month1) {
                    if (today.getDate() >= date1) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (today.getMonth() === month2) {
                    if (today.getDate() <= date2) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            if (month1 <= today.getMonth() || today.getMonth() <= month2) {
                if (today.getMonth() === month1) {
                    if (today.getDate() >= date1) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (today.getMonth() === month2) {
                    if (today.getDate() <= date2) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    function inYearMonthRange(today, month1, year1, month2, year2) {
        if (year1 === year2) {
            if (today.getFullYear() === year1) {
                if (month1 <= today.getMonth() && today.getMonth() <= month2) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (year1 < year2) {
            if (year1 <= today.getFullYear() && today.getFullYear() <= year2) {
                if (today.getFullYear() === year1) {
                    if (today.getMonth() >= month1) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (today.getFullYear() === year2) {
                    if (today.getMonth() <= month2) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    function inYearMonthDateRange(today, date1, month1, year1, date2, month2, year2) {
        if (year1 === year2) {
            if (year1 === today.getFullYear()) {
                if ((month1 <= today.getMonth()) && (today.getMonth() <= month2)) {
                    if (month1 === month2) {
                        if (date1 <= today.getDate() && today.getDate() <= date2) {
                            return true;
                        } else  {
                            return false;
                        }
                    } else if (today.getMonth() === month1) {
                        if (today.getDate() >= date1) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (today.getMonth() === month2) {
                        if (today.getDate() <= date2) {
                            return true;
                        } else {
                            return false;
                        }
                    } else  {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (year1 < year2) {
            if (year1 <= today.getFullYear() && today.getFullYear() <= year2) {
                if (today.getFullYear() === year1) {
                    if (today.getMonth() === month1) {
                        if (today.getDate() >= date1) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (today.getMonth() > month1) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (today.getFullYear() === year2) {
                    if (today.getMonth() === month2) {
                        if (today.getDate() <= date1) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (today.getMonth() < month2) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // note: watch out for wrapping around of dates. date ranges, like
    // month=9 to month=8, wrap around and cover the entire year. this
    // makes everything more interesting

    var gmt;
    if (arguments.length > 2) {
        if (arguments[arguments.length-1] === "GMT") {
            gmt = true;
            arguments.splice(0,arguments.length-1);
        }
    }
    // TODO: change date to gmt, whatever
    var today = arguments[0]

    var arg1;
    var arg2;
    var arg3;
    var arg4;
    var arg5;
    var arg6;

    switch (arguments.length-1) {
        case 1:
            var arg = arguments[1];
            if (isDate(arg)) {
                if (today.getDate() === arg) {
                    return true;
                } else {
                    return false;
                }
            } else if (isMonth(arg)) {
                if (strToMonth(arg) === today.getMonth()) {
                    return true;
                } else {
                    return false;
                }
            } else { // year
                if (today.getFullYear() === arg) {
                    return true;
                } else {
                    return false;
                }
            }
        case 2:
            arg1 = arguments[1];
            arg2 = arguments[2];
            if (isDate(arg1) && isDate(arg2)) {
                var date1 = arg1;
                var date2 = arg2;

                return inDateRange(today, date1, date2);

            } else if (isMonth(arg1) && isMonth(arg2)) {
                var month1 = strToMonth(arg1);
                var month2 = strToMonth(arg2);

                return inMonthRange(today, month1, month2);

            } else if (isYear(arg1) && isYear(arg2)) {
                var year1 = arg1;
                var year2 = arg2;

                return inYearRange(today, year1, year2);
            } else {
                return false;
            }
        case 4:
            arg1 = arguments[1];
            arg2 = arguments[2];
            arg3 = arguments[3];
            arg4 = arguments[4];

            if (isDate(arg1) && isMonth(arg2) && isDate(arg3) && isMonth(arg4)) {
                var date1 = arg1;
                var month1 = strToMonth(arg2);
                var date2 = arg3;
                var month2 = strToMonth(arg4);

                return inMonthDateRange(today, date1, month1, date2, month2);

            } else if (isMonth(arg1) && isYear(arg2) && isMonth(arg3) && isYear(arg4)) {
                var month1 = strToMonth(arg1);
                var year1 = arg2;
                var month2 = strToMonth(arg3);
                var year2 = arg4;

                return inYearMonthRange(today, month1, year1, month2, year2);
            } else {
                return false;
            }
        case 6:
            arg1 = arguments[1];
            arg2 = arguments[2];
            arg3 = arguments[3];
            arg4 = arguments[4];
            arg5 = arguments[5];
            arg6 = arguments[6];
            if (isDate(arg1) && isMonth(arg2) && isYear(arg3) &&
                isDate(arg4) && isMonth(arg5) && isYear(arg6)) {
                var day1 = arg1;
                var month1 = strToMonth(arg2);
                var year1 = arg3;
                var day2 = arg4;
                var month2 = strToMonth(arg5);
                var year2 = arg6;

                return inYearMonthDateRange(today, day1, month1, year1, day2, month2, year2);
            } else {
                return false;
            }
        default:
            return false;
    }

}

/**
 * Returns true if the current time matches the range given
 *
 * timeRange(hour)
 * timeRange(hour1, hour2)
 * timeRange(hour1, min1, hour2, min2)
 * timeRange(hour1, min1, sec1, hour2, min2, sec2)
 *
 * The string "GMT" can be used as the last additional parameter in addition to
 * the methods listed above.
 */
function timeRange() {

    // watch out for wrap around of times

    var gmt;
    if (arguments.length > 1) {
        if (arguments[arguments.length-1] === "GMT") {
            gmt = true;
            arguments.splice(0,arguments.length-1);
        }
    }

    function isHour(hour) {
        if (typeof(hour) === "number" && ( 0 <= hour && hour <= 23)) {
            return true;
        } else {
            return false;
        }
    }

    function isMin(minute) {
        if (typeof(minute) === "number" && (0 <= minute && minute <= 59)) {
            return true;
        } else {
            return false;
        }
    }

    function inHourRange(now, hour1, hour2) {
        if (hour1 === hour2) {
            if (now.getHours() === hour1) {
                return true;
            } else {
                return false;
            }
        } else if (hour1 < hour2) {
            if (hour1 <= now.getHours() && now.getHours() <= hour2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (hour1 <= now.getHours() || now.getHours() <= hour2) {
                return true;
            } else {
                return false;
            }
        }
    }

    function inHourMinuteRange(now, hour1, min1, hour2, min2) {
        if (hour1 == hour2) {
            if (now.getHours() == hour1) {
                if (min1 <= min2) {
                    if (min1 <= now.getMinutes() && now.getMinutes() <= min2) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (min1 <= now.getMinutes() || now.getMinutes() <= min2) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                if (min1 <= min2) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if (hour1 < hour2) {
            if (hour1 <= now.getHours() && now.getHours() <= hour2) {
                return true;
            } else {
                return false;
            }
        } else {
            if (hour1 <= now.getHours() || now.getHours() <= hour2) {
                return true;
            } else {
                return false;
            }
        }
    }

    var today = new Date();

    switch (arguments.length) {
        case 1:
            var hour = arguments[0];
            if (today.getHours() === hour) {
                return true;
            } else {
                return false;
            }
        case 2:
            var hour1 = arguments[0];
            var hour2 = arguments[1];
            if (isHour(hour1) && isHour(hour2)) {
                return inHourRange(today, hour1, hour2);
            } else {
                return false;
            }
        case 4:
            var hour1 = arguments[0];
            var min1 = arguments[1];
            var hour2 = arguments[2];
            var min2 = arguments[3];

            if (isHour(hour1) && isMin(min1) && isHour(hour2) && isMin(min2)) {
                return inHourMinuteRange(today, hour1, min1, hour2, min2);
            } else {
                return false;
            }

        case 6:
            var hour1 = arguments[0];
            var min1 = arguments[1];
            var sec1 = arguments[2];
            var hour2 = arguments[3];
            var min2 = arguments[4];
            var sec2 = arguments[5];

            // TODO handle seconds properly

            return inHourMinuteRange(today, hour1, min1, hour2, min2);
        default:
            return false;
    }
}

