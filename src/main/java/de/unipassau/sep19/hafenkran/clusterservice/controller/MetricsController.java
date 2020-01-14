package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricsDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;

/**
 * The REST-Controller with the GET-request endpoint for retrieving all metrics.
 */
@Slf4j
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetricsController {

    private final MetricsServerService metricsServerService;

    /**
     * GET-Endpoint for receiving a ArrayList with all {@link MetricsDTO}s.
     *
     * @return ArrayList with all {@link MetricsDTO}s.
     */
    @GetMapping(value = "/all")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<MetricsDTO> retrieveMetrics() {
        return metricsServerService.retrieveMetrics();
    }
}
