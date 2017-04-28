package com.equinix.dlaas.service;

import com.equinix.dlaas.config.NetworkConfig;
import com.equinix.dlaas.domain.CaseType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by ransay on 3/28/2017.
 */

@Component
public interface NetworkService {

    List<String> predict(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                         List<String> lastValue, int count,
                         NetworkConfig config);

    String output(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                        String lastValue, NetworkConfig config) ;

    MultiLayerNetwork updateNetwork(NormalizerMinMaxScaler normalizer, MultiLayerNetwork net,
                                    List<String> payload, NetworkConfig config);

    MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath,
                                    NetworkConfig config)
            throws IOException, InterruptedException;

    MultiLayerNetwork createNetwork(NormalizerMinMaxScaler normalizer, String trainFilePath, String testFilePath,
                                    NetworkConfig config)
            throws IOException, InterruptedException;

    NormalizerMinMaxScaler createNormalizer();

    static NetworkService getInstance(CaseType type, Map<String, NetworkService> networks) {
        return networks.get(type.toString());
    }

}
