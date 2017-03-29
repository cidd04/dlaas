package com.equinix.dlaas.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.datavec.api.records.SequenceRecord;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.metadata.RecordMetaDataURI;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.FileRecordReader;
import org.datavec.api.writable.Writable;
import org.datavec.common.data.NDArrayWritable;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Rafael Ansay
 *
 */
public class MultiFeatureSequenceRecordReader extends FileRecordReader implements SequenceRecordReader {

    private int skipNumLines = 0;
    private String delimiter = ",";
    private Iterator<String> dataIter;

    public MultiFeatureSequenceRecordReader() {
        this(0, ",");
    }

    public MultiFeatureSequenceRecordReader(int skipNumLines) {
        this(skipNumLines, ",");
    }

    public MultiFeatureSequenceRecordReader(int skipNumLines, String delimiter) {
        this.skipNumLines = skipNumLines;
        this.delimiter = delimiter;
    }

    public void initializeData(List<String> data) {
        this.dataIter = data.iterator();
    }

    @Override
    public List<List<Writable>> sequenceRecord(URI uri, DataInputStream dataInputStream) throws IOException {
        throw new UnsupportedOperationException("Invalid operation: loadSequenceFromMetaData");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<List<Writable>> sequenceRecord() {
        return nextSequence().getSequenceRecord();
    }


    @Override
    public SequenceRecord nextSequence() {

        if (dataIter == null) {
            File next = iter.next();
            invokeListeners(next);

            List<List<Writable>> out = null;
            try {
                out = loadAndClose(new FileInputStream(next));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new org.datavec.api.records.impl.SequenceRecord(out, new RecordMetaDataURI(next.toURI()));
        } else {
            List<List<Writable>> out = load(dataIter);
            return new org.datavec.api.records.impl.SequenceRecord(out, null);
        }
    }

    public boolean hasNext() {
        if (dataIter == null) {
            return super.hasNext();
        } else {
            return dataIter != null && dataIter.hasNext();
        }
    }


    private List<List<Writable>> loadAndClose(InputStream inputStream) {
        LineIterator lineIter = null;
        try {
            lineIter = IOUtils.lineIterator(new InputStreamReader(inputStream));
            return load(lineIter);
        } finally {
            if (lineIter != null) {
                lineIter.close();
            }
            IOUtils.closeQuietly(inputStream);
        }
    }

    private List<List<Writable>> load(Iterator<String> lineIter) {
        if (skipNumLines > 0) {
            int count = 0;
            while (count++ < skipNumLines && lineIter.hasNext())
                lineIter.next();
        }

        List<List<Writable>> out = new ArrayList<>();
        while (lineIter.hasNext()) {
            String line = lineIter.next();
            String[] split = line.split(delimiter);
            ArrayList<Writable> list = new ArrayList<>();

            //Create feature
            double[] feature = new double[split.length - 1];
            for (int i = 0; i < feature.length; i++) {
                feature[i] = Double.parseDouble(split[i]);
            }
            list.add(new NDArrayWritable(Nd4j.create(feature)));

            //Create label
            double[] label = new double[1];
            label[0] = Double.parseDouble(split[split.length - 1]);
            list.add(new NDArrayWritable(Nd4j.create(label)));

            //Add everything to list
            out.add(list);
        }
        return out;
    }

    @Override
    public SequenceRecord loadSequenceFromMetaData(RecordMetaData recordMetaData) throws IOException {
        throw new UnsupportedOperationException("Invalid operation: loadSequenceFromMetaData");
    }

    @Override
    public List<SequenceRecord> loadSequenceFromMetaData(List<RecordMetaData> recordMetaDatas) throws IOException {
        throw new UnsupportedOperationException("Invalid operation: loadSequenceFromMetaData (returns a list)");
    }
}
