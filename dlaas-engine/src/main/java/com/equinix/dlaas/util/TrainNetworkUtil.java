package com.equinix.dlaas.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by ransay on 3/23/2017.
 */
public class TrainNetworkUtil {

    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + "/" + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * Completely shit code
     * @param rawDataFilePath
     * @param saveFilePath
     * @return int
     * @throws IOException
     */
    public static int formatRawData(String rawDataFilePath, String saveFilePath) throws IOException {
        String line;
        String[] s = null;
        boolean first = true;
        int output;
        try (BufferedReader br = new BufferedReader(new FileReader(rawDataFilePath));
             FileWriter fw = new FileWriter(saveFilePath)) {
            while ((line = br.readLine()) != null) {
                s = line.split(";");
                if (!first) {
                    for (int i = 1; i < s.length; i++) {
                        fw.write(s[i]);
                        if (i != s.length - 1)
                            fw.write(";");
                    }
                    fw.write("\n");
                }
                for (int i = 1; i < s.length; i++) {
                    fw.write(s[i]);
                    fw.write(";");
                }
                first = false;
            }
            for (int i = 1; i < s.length; i++) {
                fw.write(s[i]);
                if (i != s.length - 1)
                    fw.write(";");
            }
            fw.write("\n");
            output = s.length / 2;
        }
        return output;
    }

    public static List<String> formatRawData(List<String> source) {
        List<String> destination = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String[] s = null;
        boolean first = true;
        for (String line : source) {
            s = line.split(";");
            if (!first) {
                for (int i = 1; i < s.length; i++) {
                    sb.append(s[i]);
                    if (i != s.length - 1)
                        sb.append(";");
                }
                destination.add(sb.toString());
                sb.setLength(0);
            }
            for (int i = 1; i < s.length; i++) {
                sb.append(s[i]);
                sb.append(";");
            }
            first = false;
        }
        for (int i = 1; i < s.length; i++) {
            sb.append(s[i]);
            if (i != s.length - 1)
                sb.append(";");
        }
        destination.add(sb.toString());
        return destination;
    }

    public static List<String> getLastValue(String filePath) throws IOException {
        List<String> lastValueList = new ArrayList<>();
        String line;
        String output = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotEmpty(line.trim())) output = line;
            }
            lastValueList.add(output);
        }
        return lastValueList;
    }
}
