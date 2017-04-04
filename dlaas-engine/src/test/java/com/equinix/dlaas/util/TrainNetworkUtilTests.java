package com.equinix.dlaas.util;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainNetworkUtilTests {

    private String destination = "C:/Users/ransay";

    @Test
    public void unzip() throws IOException {
        TrainNetworkUtil.unzip(destination + "/" + "trainraw,txt.zip" , destination);
    }

    @Test
    public void formatRawDataList() {
        List<String> input = new ArrayList<>();
        input.add("datetime4;123;456");
        input.add("datetime5;222;333");
        input.add("datetime6;444;555");
        List<String> output = TrainNetworkUtil.formatRawData(input);
        System.out.println(output);
    }

    @Test
    public void formatRawData() throws IOException {
        TrainNetworkUtil.formatRawData(destination + "/" + "trainraw,txt.txt" , destination + "/" + "xx_train.txt");
    }

    @Test
    public void createIXPData() throws IOException {
        TrainNetworkUtil.createIXPData("C:/Users/ransay/Desktop/rawixpstuff.txt", "C:/Users/ransay/Desktop/ixpstuff.txt");
        TrainNetworkUtil.createIXPData("C:/Users/ransay/Desktop/rawixpstuff2.txt", "C:/Users/ransay/Desktop/ixpstuff2.txt");
    }


}