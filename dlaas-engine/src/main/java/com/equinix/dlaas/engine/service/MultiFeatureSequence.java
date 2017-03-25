package com.equinix.dlaas.engine.service;


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
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class MultiFeatureSequence {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiFeatureSequence.class);

    public void process(String trainFilePath, String testFilePath) throws Exception {

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
            LOGGER.info("Epoch " + i + " complete. Time series evaluation:");

            //Run regression evaluation on our single column input
            RegressionEvaluation evaluation = new RegressionEvaluation(1);
            INDArray features = testData.getFeatureMatrix();

            INDArray lables = testData.getLabels();
            INDArray predicted = net.output(features, false);

            evaluation.evalTimeSeries(lables, predicted);

            //Just do sout here since the logger will shift the shift the columns of the stats
            System.out.println(evaluation.stats());
        }

        File locationToSave = new File(trainFilePath + "_model.zip");
        boolean saveUpdater = true;
        ModelSerializer.writeModel(net, locationToSave, saveUpdater);

        LOGGER.info("----- Complete -----");
    }

    private void formatRawData(String rawDataFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rawDataFilePath));
             FileWriter fw = new FileWriter(rawDataFilePath + "_")) {
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
}