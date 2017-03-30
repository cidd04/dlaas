package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import org.datavec.api.records.reader.SequenceRecordReader;
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

    public List<String> predict(MultiLayerNetwork net, List<String> lastValue, int count) {

        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(lastValue);
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(reader, config.getMiniBatchSize(), -1, 1, true);
        DataSet dataSet = trainIter.next();
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);

        List<INDArray> predictedList = new ArrayList<>();
        INDArray input = dataSet.getFeatures();
        for (int i = 0; i < count; i++) {
            INDArray predicted = net.rnnTimeStep(input);
            input = predicted;
        }

        List<String> output = new ArrayList<>();
        for (INDArray value : predictedList) {
            output.add(String.valueOf(value.getDouble(0)));
        }
        return output;
    }

    public MultiLayerNetwork updateNetwork(MultiLayerNetwork net, List<String> payload) {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(payload);
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(reader, config.getMiniBatchSize(), -1, 1, true);
        DataSet dataSet = trainIter.next();
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);
        net.fit(dataSet);
        return net;
    }

    public MultiLayerNetwork createNetwork(String trainFilePath, String testFilePath)
            throws IOException, InterruptedException {

        SequenceRecordReader trainReader = new MultiFeatureSequenceRecordReader(0, ";");
        trainReader.initialize(new NumberedFileInputSplit(trainFilePath, 0, 0));
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainReader, config.getMiniBatchSize(), -1, 1, true);
        SequenceRecordReader testReader = new MultiFeatureSequenceRecordReader(0, ";");
        testReader.initialize(new NumberedFileInputSplit(testFilePath, 0, 0));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, config.getMiniBatchSize(), -1, 1, true);
        DataSet trainData = trainIter.next();
        DataSet testData = testIter.next();
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(trainData);
        normalizer.transform(trainData);
        normalizer.transform(testData);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(config.getSeed())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(config.getIterations())
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .learningRate(config.getLearningRate())
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(2).nOut(config.getHidden())
                        .build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(config.getHidden()).nOut(1).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(20));
        for (int i = 0; i < config.getnEpochs(); i++) {
            net.fit(trainData);
            logger.info("Epoch " + i + " complete. Time series evaluation:");
            RegressionEvaluation evaluation = new RegressionEvaluation(1);
            INDArray features = testData.getFeatureMatrix();
            INDArray lables = testData.getLabels();
            INDArray predicted = net.output(features, false);
            evaluation.evalTimeSeries(lables, predicted);
            System.out.println(evaluation.stats());
        }
        logger.info("----- Complete -----");
        return net;
    }
}
