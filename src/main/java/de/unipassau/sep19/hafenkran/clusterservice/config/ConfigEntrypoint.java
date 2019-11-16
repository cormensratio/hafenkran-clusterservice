package de.unipassau.sep19.hafenkran.clusterservice.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.controller",
        "de.unipassau.sep19.hafenkran.clusterservice.util",
        "de.unipassau.sep19.hafenkran.clusterservice.service.impl"
})
@EntityScan(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.model"
})
@EnableJpaRepositories(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.repository",
})
@EnableAutoConfiguration
public class ConfigEntrypoint {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters.", ex);
    }

}
