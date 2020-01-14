package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.model.ExecutionDetails;
import de.unipassau.sep19.hafenkran.clusterservice.service.ExecutionService;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetricsController {

    private final MetricsServerService metricsServerService;

    private final ExecutionService executionService;

    @GetMapping(value = "/retrieve")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<MetricsDTO> retrieveMetrics() {
        Map<String, MetricsDTO> map = metricsServerService.retrieveMetrics();
        ArrayList<MetricsDTO> metricsDTOArrayList = new ArrayList<>();
        for (Map.Entry<String, MetricsDTO> entry : map.entrySet()) {
            String podName = entry.getKey();
            MetricsDTO metricsDTO = entry.getValue();
            if (metricsDTO.getExecutionId() == null) {
                UUID experimentId = metricsDTO.getExperimentId();
                ExecutionDetails executionDetails = executionService.getExecutionOfPod(podName, experimentId);
                metricsDTO.setExecutionId(executionDetails.getId());
            }
            metricsDTOArrayList.add(metricsDTO);
        }
        return metricsDTOArrayList;
    }
}
