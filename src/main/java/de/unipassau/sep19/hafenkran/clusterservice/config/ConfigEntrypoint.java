package de.unipassau.sep19.hafenkran.clusterservice.config;

import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.KubernetesClient;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl.KubernetesClientImpl;
import de.unipassau.sep19.hafenkran.clusterservice.kubernetesclient.impl.KubernetesClientMockImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Configuration
@ComponentScan(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.controller",
        "de.unipassau.sep19.hafenkran.clusterservice.util",
        "de.unipassau.sep19.hafenkran.clusterservice.service.impl",
        "de.unipassau.sep19.hafenkran.clusterservice.reportingserviceclient"
})
@EntityScan(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.model"
})
@EnableJpaRepositories(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.repository",
})
@Slf4j
@EnableAutoConfiguration
public class ConfigEntrypoint {

    @Bean
    @ConditionalOnProperty(
            value = "mockKubernetesClient",
            havingValue = "true",
            matchIfMissing = true
    )
    public KubernetesClient kubernetesMockClient() {
        return new KubernetesClientMockImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "mockKubernetesClient",
            havingValue = "false"
    )
    public KubernetesClient kubernetesClient() throws IOException {
        return new KubernetesClientImpl();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters.", ex);
    }
}
