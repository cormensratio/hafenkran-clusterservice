package de.unipassau.sep19.hafenkran.clusterservice.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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

}
