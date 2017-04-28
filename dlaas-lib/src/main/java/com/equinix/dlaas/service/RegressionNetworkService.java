package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ransay on 4/28/2017.
 */

@Component("REGRESSION")
public class RegressionNetworkService implements NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    public List<String> predict(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                List<String> lastValue, int count,
                                NetworkConfig config) {
        return null;
    }

    public String output(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                               String lastValue, NetworkConfig config) {
        DataSet dataSet = null;//initializeDataSet(lastValue, config);
        normalizeDataset(normalizer, false, dataSet);
        INDArray input = dataSet.getFeatures();
        INDArray predicted = net.output(input);
        String output = String.valueOf(predicted.getDouble(1));
        return output;
    }

    public MultiLayerNetwork updateNetwork(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                           List<String> payload, NetworkConfig config) {
        DataSet dataSet = initializeDataSet(payload, config);
        normalizeDataset(normalizer, true, dataSet);
        net.fit(dataSet);
        return net;
    }

    public MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath,
                                           NetworkConfig config)
            throws IOException, InterruptedException {
        return createNetwork(normalizer, trainFilePath, null, config);
    }

    public MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath, String testFilePath,
                                           NetworkConfig config)
            throws IOException, InterruptedException {
        DataSet trainData = initializeDataSet(trainFilePath, config);
        DataSet testData = null;
        if (testFilePath != null)
            testData = initializeDataSet(testFilePath, config);
        normalizeDataset(normalizer, true, trainData, testData);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(.01)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(2).nOut(20)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .nIn(20).nOut(1).build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));
        for (int i = 0; i < config.getnEpochs(); i++) {
            net.fit(trainData);
            logger.info("Epoch " + i + " complete. Time series evaluation:");
            if (testFilePath != null) {
                RegressionEvaluation evaluation = new RegressionEvaluation(1);
                INDArray predicted = net.output(testData.getFeatureMatrix(), false);
                evaluation.eval(testData.getLabels(), predicted);
                System.out.println(evaluation.stats());
            }
        }
        return net;
    }

    public NormalizerMinMaxScaler createNormalizer() {
        return new NormalizerMinMaxScaler(-1, 1);
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

    private DataSet initializeDataSet(String filePath, NetworkConfig config) throws IOException, InterruptedException {
        RecordReader reader = new CSVRecordReader(0, ";");
        reader.initialize(new FileSplit(new File(filePath)));
        return initializeDataSet(reader, config);
    }

    private DataSet initializeDataSet(List<String> payload, NetworkConfig config) {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(payload);
        return initializeDataSet(reader, config);
    }

    private DataSet initializeDataSet(RecordReader reader, NetworkConfig config) {
        DataSetIterator iter = new RecordReaderDataSetIterator(reader, config.getMiniBatchSize(), 2, 2, true);
        return iter.next();
    }
}
