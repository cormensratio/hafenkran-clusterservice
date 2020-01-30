package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * The REST-Controller with the GET-request endpoint for retrieving all metrics.
 */
@Slf4j
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * GET-Endpoint for receiving an ArrayList with all {@link MetricDTO}s of the currently running containers.
     * Cluster-internal pods excluded.
     *
     * @return ArrayList with all {@link MetricDTO}s.
     */
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<MetricDTO> retrieveMetrics() {
        return metricsService.retrieveMetrics();
    }
}
