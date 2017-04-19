package com.equinix.dlaas.util;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by ransay on 4/19/2017.
 */
public class FormatDCIMDataUtil {

    public static void initDCIMFile(String filePath) throws IOException {
        Calendar calendarStart = new GregorianCalendar(2017,2-1,1,0,0,0);
        Calendar calendarEnd = new GregorianCalendar(2017,3-1,1,0,0,0);
        try (FileWriter fw = new FileWriter(filePath)) {
            while(!calendarStart.equals(calendarEnd)) {
                fw.write("" + calendarStart.get(Calendar.YEAR));
                if (calendarStart.get(Calendar.MONTH) - 1 < 10) {
                    fw.write("0");
                }
                fw.write("" + (calendarStart.get(Calendar.MONTH) - 1));
                if (calendarStart.get(Calendar.DAY_OF_MONTH) < 10) {
                    fw.write("0");
                }
                fw.write("" + calendarStart.get(Calendar.DAY_OF_MONTH));
                if (calendarStart.get(Calendar.HOUR_OF_DAY) < 10) {
                    fw.write("0");
                }
                fw.write("" + calendarStart.get(Calendar.HOUR_OF_DAY));
                if (calendarStart.get(Calendar.MINUTE) < 10) {
                    fw.write("0");
                }
                fw.write("" + calendarStart.get(Calendar.MINUTE));
                if (calendarStart.get(Calendar.SECOND) < 10) {
                    fw.write("0");
                }
                fw.write("" + calendarStart.get(Calendar.SECOND));
                fw.write(";\n");

                calendarStart.add(Calendar.MINUTE, 15);
            }
        }
    }

    public static void main(String[] args) throws IOException {

    }

    public static int countStatus(Calendar compareCalendar, String directory) throws IOException {
        File[] files = new File(directory).listFiles();
        int count = 0;
        for (File file: files) {
            int res = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] s = line.split(";");
                    if (s.length > 3) {
                        Calendar currentCalendar = new GregorianCalendar(
                                Integer.valueOf(s[1]),
                                Integer.valueOf(s[2]) - 1,
                                Integer.valueOf(s[3]),
                                Integer.valueOf(s[4]),
                                Integer.valueOf(s[5]),
                                Integer.valueOf(s[6]));
                        if (compareCalendar.equals(currentCalendar) || compareCalendar.after(currentCalendar)) {
                            res = Integer.valueOf(s[7]);
                        } else {
                            break;
                        }
                    }
                }
            }
            if (res == 1)
                count++;
        }
        return count;
    }

    public static BigDecimal computeAverage(Calendar minCalendar, Calendar maxCalendar,
                                            String directory) throws IOException {
        System.out.println(minCalendar.getTime());
        System.out.println(maxCalendar.getTime());

        File[] files = new File(directory).listFiles();
        BigDecimal sum = BigDecimal.ZERO;
        int total = 0;
        for (File file: files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] s = line.split(";");
                    if (s.length > 3) {
                        Calendar currentCalendar = new GregorianCalendar(
                                Integer.valueOf(s[1]),
                                Integer.valueOf(s[2]) - 1,
                                Integer.valueOf(s[3]),
                                Integer.valueOf(s[4]),
                                Integer.valueOf(s[5]),
                                Integer.valueOf(s[6]));
                        if (minCalendar.before(currentCalendar) &&
                                (maxCalendar.equals(currentCalendar) || maxCalendar.after(currentCalendar))) {
                            BigDecimal res = new BigDecimal(s[7]);
                            sum = sum.add(res);
                            total++;
                            System.out.println(currentCalendar.getTime());
                            System.out.println(res);
                        } else if (maxCalendar.before(currentCalendar)) {
                            break;
                        }
                    }
                }
            }
        }
        if (total > 0) {
            return sum.divide(new BigDecimal(total), new MathContext(8,RoundingMode.HALF_UP));
        }
        return sum;
    }
}
