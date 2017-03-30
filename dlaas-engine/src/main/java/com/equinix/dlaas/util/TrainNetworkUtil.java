package com.equinix.dlaas.util;

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

    public static void formatRawData(String rawDataFilePath, String saveFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rawDataFilePath));
             FileWriter fw = new FileWriter(saveFilePath)) {
            String line;
            String[] s = null;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                s = line.split(";");
                if (!first)
                    fw.write(s[1] + "\n");
                for (int i = 1; i < s.length; i++) {
                    fw.write(s[i] + ";");
                }
                first = false;
            }
            fw.write(s[1] + "\n");
        }
    }

    public static List<String> formatRawData(List<String> source) {
        List<String> destination = new ArrayList<>();
        String value = "";
        String[] s = null;
        boolean first = true;
        for (String line : source) {
            s = line.split(";");
            if (!first) {
                value += s[1];
                destination.add(value);
                value = "";
            }
            for (int i = 1; i < s.length; i++) {
                value += s[i] + ";";
            }
            first = false;
        }
        value += s[1];
        destination.add(value);
        return destination;
    }
}
