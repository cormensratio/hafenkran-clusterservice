package de.unipassau.sep19.hafenkran.clusterservice.controller;

import de.unipassau.sep19.hafenkran.clusterservice.dto.MetricDTO;
import de.unipassau.sep19.hafenkran.clusterservice.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @Value("${service-user.secret}")
    private String serviceSecret;

    /**
     * GET-Endpoint for receiving an ArrayList with all {@link MetricDTO}s of the currently running containers.
     * Cluster-internal pods excluded.
     *
     * @return ArrayList with all {@link MetricDTO}s.
     */
    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<MetricDTO> retrieveMetrics(@RequestParam("secret") String secret) {
        if (!secret.equals(serviceSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You are not authorized to call an internal service endpoint");
        }
        return metricsService.retrieveMetrics();
    }
}
