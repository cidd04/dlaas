package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ransay on 3/28/2017.
 */

@Component
public class NetworkService {

    @Autowired
    private NetworkConfig config;

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    public List<String> predict(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                List<String> lastValue, int count) {
        DataSet dataSet = initializeDataSet(lastValue);
        normalizeDataset(normalizer, false, dataSet);
        List<INDArray> predictedList = new ArrayList<>();
        INDArray input = dataSet.getFeatures();
        for (int i = 0; i < count; i++) {
            INDArray predicted = net.rnnTimeStep(input);
            predictedList.add(predicted);
            input = predicted;
        }
        List<String> output = new ArrayList<>();
        for (INDArray predicted : predictedList) {
            normalizer.revertLabels(predicted);
            //output.add(String.valueOf(predicted.getDouble(0)));
            output.add(String.valueOf(predicted.getDouble(1)) + ";" + String.valueOf(predicted.getDouble(1)));
        }
        return output;
    }

    public MultiLayerNetwork updateNetwork(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                           List<String> payload) {
        DataSet dataSet = initializeDataSet(payload);
        normalizeDataset(normalizer, true, dataSet);
        net.fit(dataSet);
        return net;
    }

    public MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath, int columnCount)
            throws IOException, InterruptedException {
        return createNetwork(normalizer, trainFilePath, null, columnCount);
    }

    public MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath, String testFilePath,
                                           int columnCount)
            throws IOException, InterruptedException {
        DataSet trainData = initializeDataSet(trainFilePath);
        DataSet testData = null;
        if (testFilePath != null)
            testData = initializeDataSet(testFilePath);
        normalizeDataset(normalizer, true, trainData, testData);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(config.getSeed())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(config.getIterations())
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .learningRate(config.getLearningRate())
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(columnCount).nOut(config.getHidden())
                        .build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(config.getHidden()).nOut(columnCount).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(20));
        for (int i = 0; i < config.getnEpochs(); i++) {
            net.fit(trainData);
            logger.info("Epoch " + i + " complete. Time series evaluation:");
            if (testFilePath != null) {
                RegressionEvaluation evaluation = new RegressionEvaluation(2);
                INDArray predicted = net.output(testData.getFeatureMatrix(), false);
                evaluation.evalTimeSeries(testData.getLabels(), predicted);
                System.out.println(evaluation.stats());
            }
        }
        return net;
    }

    public NormalizerMinMaxScaler createNormalizer() {
        return new NormalizerMinMaxScaler(0, 1);
    }

    private void normalizeDataset(NormalizerMinMaxScaler normalizer, boolean fitData, DataSet trainData) {
        normalizeDataset(normalizer, fitData, trainData, null);
    }

    private void normalizeDataset(NormalizerMinMaxScaler normalizer, boolean fitData, DataSet trainData,
                                  DataSet testData) {
        if (fitData)
            normalizer.fit(trainData);
        normalizer.transform(trainData);
        if (testData != null)
            normalizer.transform(testData);
    }

    private DataSet initializeDataSet(String filePath) throws IOException, InterruptedException {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initialize(new NumberedFileInputSplit(filePath, 0, 0));
        return initializeDataSet(reader);
    }

    private DataSet initializeDataSet(List<String> payload) {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(payload);
        return initializeDataSet(reader);
    }

    private DataSet initializeDataSet(MultiFeatureSequenceRecordReader reader) {
        DataSetIterator iter = new SequenceRecordReaderDataSetIterator(
                reader, config.getMiniBatchSize(), -1, 1, true);
        return iter.next();
    }

}
