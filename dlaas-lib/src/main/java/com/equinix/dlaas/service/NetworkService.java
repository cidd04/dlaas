package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleRecord;
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
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ransay on 3/28/2017.
 */

@Component
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private RedisMap<String, SimpleRecord> recordMap;

    public List<String> predict(String id, int count) {
        SimpleRecord record = recordMap.get(id);
        MultiLayerNetwork net = record.getNet();
        if (net == null)
            throw new RuntimeException("No network configured on this id: " + id);
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(record.getLastValue());
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(reader, 32, -1, 1, true);
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

    public void updateNetwork(String id, List<String> payload) {
        SimpleRecord record = recordMap.get(id);
        MultiLayerNetwork net = record.getNet();
        if (net == null)
            throw new RuntimeException("No network configured on this id: " + id);
        MultiFeatureSequenceRecordReader reader = new MultiFeatureSequenceRecordReader(0, ";");
        reader.initializeData(payload);
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(reader, 32, -1, 1, true);
        DataSet dataSet = trainIter.next();
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);
        net.fit(dataSet);
        record.setNet(net);
        recordMap.put(id, record);
    }

    public void createNetwork(String trainFilePath, String testFilePath)
            throws IOException, InterruptedException {

        int miniBatchSize = 32;

        // ----- Load the training data -----
        SequenceRecordReader trainReader = new MultiFeatureSequenceRecordReader(0, ";");
        trainReader.initialize(new NumberedFileInputSplit(trainFilePath, 0, 0));

        //For regression, numPossibleLabels is not used. Setting it to -1 here
        DataSetIterator trainIter = new SequenceRecordReaderDataSetIterator(trainReader, miniBatchSize, -1, 1, true);

        SequenceRecordReader testReader = new MultiFeatureSequenceRecordReader(0, ";");
        testReader.initialize(new NumberedFileInputSplit(testFilePath, 0, 0));
        DataSetIterator testIter = new SequenceRecordReaderDataSetIterator(testReader, miniBatchSize, -1, 1, true);

        //Create data set from iterator here since we only have a single data set
        DataSet trainData = trainIter.next();
        DataSet testData = testIter.next();

        //Normalize data, including labels (fitLabel=true)
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(trainData);              //Collect training data statistics

        normalizer.transform(trainData);
        normalizer.transform(testData);

        // ----- Configure the network -----
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(140)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .learningRate(0.0015)
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(1).nOut(10)
                        .build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY).nIn(10).nOut(1).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        net.setListeners(new ScoreIterationListener(20));

        // ----- Train the network, evaluating the test set performance at each epoch -----
        int nEpochs = 600;

        for (int i = 0; i < nEpochs; i++) {
            net.fit(trainData);
            logger.info("Epoch " + i + " complete. Time series evaluation:");

            //Run regression evaluation on our single column input
            RegressionEvaluation evaluation = new RegressionEvaluation(1);
            INDArray features = testData.getFeatureMatrix();

            INDArray lables = testData.getLabels();
            INDArray predicted = net.output(features, false);

            evaluation.evalTimeSeries(lables, predicted);

            //Just do sout here since the logger will shift the shift the columns of the stats
            System.out.println(evaluation.stats());
        }

        SimpleRecord record = new SimpleRecord.SimpleRecordBuilder()
                .id("")
                .trainFilePath(trainFilePath)
                .testFilePath(testFilePath).build();
        recordMap.put("", record);

        logger.info("----- Complete -----");
    }

    public void createRecord(SimpleRecord record) {
        recordMap.put(record.getId(), record);
    }
}
