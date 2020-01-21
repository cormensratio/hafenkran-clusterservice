package de.unipassau.sep19.hafenkran.clusterservice.serviceclient.impl;

import de.unipassau.sep19.hafenkran.clusterservice.dto.ResultDTO;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ReportingServiceClient;
import de.unipassau.sep19.hafenkran.clusterservice.serviceclient.ServiceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ReportingServiceClientImpl implements ReportingServiceClient {

    private final ServiceClient serviceClient;

    @Value("${reporting-service-uri}")
    private String basePath;

    @Value("${service-user.secret}")
    private final String serviceSecret;

    /**
     * {@inheritDoc}
     */
    public void sendResultsToResultsService(@NonNull ResultDTO resultDTO) {
        serviceClient.post(basePath + "/results?secret=" + serviceSecret, resultDTO, String.class, null);
    }

}
