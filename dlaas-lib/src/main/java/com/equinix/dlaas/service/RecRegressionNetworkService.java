package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ransay on 4/28/2017.
 */

@Component("REC_REGRESSION")
public class RecRegressionNetworkService implements NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    public List<String> predict(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                List<String> lastValue, int count,
                                NetworkConfig config) {
        DataSet dataSet = initializeDataSet(lastValue, config);
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

    public String output(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                               String lastValue, NetworkConfig config) {
        return null;
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
                .seed(140)
                .regularization(true)
                .l2(0.001)
                .dropOut(0.5)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .learningRate(0.0005)
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(2).nOut(100)
                        .build())
                .layer(1, new GravesLSTM.Builder().activation(Activation.TANH).nIn(100).nOut(100)
                        .build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(100).nOut(2).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(100).tBPTTBackwardLength(100)
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

    private DataSet initializeDataSet(String filePath, NetworkConfig config) throws IOException, InterruptedException {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initialize(new NumberedFileInputSplit(filePath, 0, 0));
        return initializeDataSet(reader, config);
    }

    private DataSet initializeDataSet(List<String> payload, NetworkConfig config) {
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(payload);
        return initializeDataSet(reader, config);
    }

    private DataSet initializeDataSet(MultiFeatureSequenceRecordReader reader, NetworkConfig config) {
        DataSetIterator iter = new SequenceRecordReaderDataSetIterator(
                reader, config.getMiniBatchSize(), -1, 1, true);
        return iter.next();
    }

}
