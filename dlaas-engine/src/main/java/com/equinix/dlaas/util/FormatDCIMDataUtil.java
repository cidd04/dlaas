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

    private static final String home = "C:/Users/ransay/dcimdata";

    public static void main(String[] args) throws IOException {
        buildDCIMFile(home + "/dcimfile.txt");
    }

    public static void buildDCIMFile(String filePath) throws IOException {

        Calendar calendarStart = new GregorianCalendar(2017,2-1,1,0,0,0);
        Calendar calendarEnd = new GregorianCalendar(2017,3-1,1,0,0,0);

        BigDecimal prevCondrefrigeranttemperature = BigDecimal.ZERO;
        int prevChillerstatus = -1;
        BigDecimal prevLabel = BigDecimal.ZERO;

        try (FileWriter fw = new FileWriter(filePath)) {
            while(!calendarStart.equals(calendarEnd)) {
//                fw.write("" + calendarStart.get(Calendar.YEAR));
//                if (calendarStart.get(Calendar.MONTH) - 1 < 10) {
//                    fw.write("0");
//                }
//                fw.write("" + (calendarStart.get(Calendar.MONTH) - 1));
//                if (calendarStart.get(Calendar.DAY_OF_MONTH) < 10) {
//                    fw.write("0");
//                }
//                fw.write("" + calendarStart.get(Calendar.DAY_OF_MONTH));
//                if (calendarStart.get(Calendar.HOUR_OF_DAY) < 10) {
//                    fw.write("0");
//                }
//                fw.write("" + calendarStart.get(Calendar.HOUR_OF_DAY));
//                if (calendarStart.get(Calendar.MINUTE) < 10) {
//                    fw.write("0");
//                }
//                fw.write("" + calendarStart.get(Calendar.MINUTE));
//                if (calendarStart.get(Calendar.SECOND) < 10) {
//                    fw.write("0");
//                }
//                fw.write("" + calendarStart.get(Calendar.SECOND));
//                fw.write(   ";");

                Calendar minCalendar = (Calendar) calendarStart.clone();
                minCalendar.add(Calendar.MINUTE, -5);

                BigDecimal condrefrigeranttemperature =
                        computeAverage(minCalendar, calendarStart, home + "/condrefrigeranttemperature");
                if (condrefrigeranttemperature.compareTo(BigDecimal.ZERO) != 0) {
                    prevCondrefrigeranttemperature = condrefrigeranttemperature;
                }
                fw.write(prevCondrefrigeranttemperature.toString() + ";");

                int chillerstatus = countStatus(calendarStart, home + "/chillerstatus");
                if (chillerstatus != -1) {
                    prevChillerstatus = chillerstatus;
                }
                fw.write(prevChillerstatus + ";");

                BigDecimal l = getLabel(calendarStart, home + "/pue");
                if (l.compareTo(BigDecimal.ZERO) != 0) {
                    prevLabel = l;
                }
                fw.write(prevLabel.toString());

                fw.write("\n");
                System.out.println(calendarStart.getTime());
                calendarStart.add(Calendar.MINUTE, 5);
            }
        }
    }

    public static BigDecimal getLabel(Calendar compareCalendar, String directory) throws IOException {
        File[] files = new File(directory).listFiles();
        try (BufferedReader br = new BufferedReader(new FileReader(files[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(";");
                if (s.length > 3) {
                    Calendar currentCalendar = new GregorianCalendar(
                            Integer.valueOf(s[1]),
                            Integer.valueOf(s[2]) - 1,
                            Integer.valueOf(s[3]),
                            Integer.valueOf(s[4]),
                            Integer.valueOf(s[5]));
                    if (compareCalendar.equals(currentCalendar)) {
                        return new BigDecimal(s[6]);
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public static int countStatus(Calendar compareCalendar, String directory) throws IOException {
        File[] files = new File(directory).listFiles();
        int count = 0;
        boolean empty = true;
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
                            empty = false;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (res == 1)
                count++;
        }
        if (empty) {
            return -1;
        }
        return count;
    }

    public static BigDecimal computeAverage(Calendar minCalendar, Calendar maxCalendar,
                                            String directory) throws IOException {
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
