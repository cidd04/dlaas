package com.equinix.dlaas.domain;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

/**
 * Created by ransay on 3/23/2017.
 */
public class SimpleRecord {

    public SimpleRecord() {}

    private SimpleRecord(SimpleRecordBuilder builder) {
        this.id = builder.id;
        this.trainFilePath = builder.trainFilePath;
        this.testFilePath = builder.testFilePath;
    }

    private String id;
    private String trainFilePath;
    private String testFilePath;
    private MultiLayerNetwork net;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrainFilePath() {
        return trainFilePath;
    }

    public void setTrainFilePath(String trainFilePath) {
        this.trainFilePath = trainFilePath;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public void setTestFilePath(String testFilePath) {
        this.testFilePath = testFilePath;
    }

    public MultiLayerNetwork getNet() {
        return net;
    }

    public void setNet(MultiLayerNetwork net) {
        this.net = net;
    }

    public static class SimpleRecordBuilder {

        private String id;
        private String trainFilePath;
        private String testFilePath;

        public SimpleRecordBuilder() {

        }

        public SimpleRecord.SimpleRecordBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SimpleRecord.SimpleRecordBuilder trainFilePath(String trainFilePath) {
            this.trainFilePath = trainFilePath;
            return this;
        }

        public SimpleRecord.SimpleRecordBuilder testFilePath(String testFilePath) {
            this.testFilePath = testFilePath;
            return this;
        }

        public SimpleRecord build() {
            SimpleRecord SimpleRecord =  new SimpleRecord(this);
            return SimpleRecord;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleRecord that = (SimpleRecord) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
