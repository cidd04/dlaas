package com.equinix.dlaas.util;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by ransay on 4/19/2017.
 */
public class FormatDCIMDataUtilTests {

    @Test
    public void formatRawDataList() throws IOException {
        //initDCIMFile("C:/Users/ransay/initDCIMFile.txt");
        //System.out.println("20170228190000".compareTo("20170228191500"));
        Calendar calendarEnd = new GregorianCalendar(2017,2-1,1,0,0,0);
        Calendar calendarStart = new GregorianCalendar(2017,1-1,31,23,45,0);
        //int i = countStatus(calendarEnd, "C:/Users/ransay/chillerstatus");
        BigDecimal i = FormatDCIMDataUtil.computeAverage(
                calendarStart,
                calendarEnd,
                "C:/Users/ransay/condrefrigeranttemperature");
        System.out.println(i);
    }
}
