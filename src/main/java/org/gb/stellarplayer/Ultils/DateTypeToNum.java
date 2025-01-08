package org.gb.stellarplayer.Ultils;

import org.gb.stellarplayer.Model.Enum.DateType;

public class DateTypeToNum {
    public static int convert(DateType dateType) {
        switch (dateType) {
            case DAILY:
                return 1;
            case MONTHLY:
                return 30;
            case HALF_YEAR:
                return 182;
            case YEARLY:
                return 365;
            default:
                throw new IllegalArgumentException("Unknown DateType: " + dateType);
        }
    }
}
