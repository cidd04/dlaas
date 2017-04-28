package com.equinix.dlaas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by ransay on 3/24/2017.
 */

@ConfigurationProperties(prefix = "networkConfig")
@Component
public class NetworkConfig {

    private int miniBatchSize;
    private int seed;
    private int iterations;
    private double learningRate;
    private int nEpochs;
    private int hidden;
    private int columnCount;

    public int getMiniBatchSize() {
        return miniBatchSize;
    }

    public void setMiniBatchSize(int miniBatchSize) {
        this.miniBatchSize = miniBatchSize;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getnEpochs() {
        return nEpochs;
    }

    public void setnEpochs(int nEpochs) {
        this.nEpochs = nEpochs;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

}
