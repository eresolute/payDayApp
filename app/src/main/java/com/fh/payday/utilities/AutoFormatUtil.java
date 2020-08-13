package com.fh.payday.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

class AutoFormatUtil {

    private static final String FORMAT_NO_DECIMAL = "###,###";

    private static final String FORMAT_WITH_DECIMAL = "###,###.###";

    static int getCharOccurance(String input, char c) {
        int occurance = 0;
        char[] chars = input.toCharArray();
        for (char thisChar : chars) {
            if (thisChar == c) {
                occurance++;
            }
        }
        return occurance;
    }

    static String extractDigits(String input) {
        return input.replaceAll("\\D+", "");
    }

    private static String formatToStringWithoutDecimal(double value) {
        NumberFormat formatter = new DecimalFormat(FORMAT_NO_DECIMAL, DecimalFormatSymbols.getInstance(new Locale("en")));
        return formatter.format(value);
    }

    static String formatToStringWithoutDecimal(String value) {
        return formatToStringWithoutDecimal(Double.parseDouble(value));
    }

    static String formatWithDecimal(String price) {
        return formatWithDecimal(Double.parseDouble(price));
    }

    private static String formatWithDecimal(double price) {
        NumberFormat formatter = new DecimalFormat(FORMAT_WITH_DECIMAL, DecimalFormatSymbols.getInstance(new Locale("en")));
        return formatter.format(price);
    }
}
