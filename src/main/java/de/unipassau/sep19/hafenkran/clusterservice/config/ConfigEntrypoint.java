package de.unipassau.sep19.hafenkran.clusterservice.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "de.unipassau.sep19.hafenkran.clusterservice.controller",
})
@EnableAutoConfiguration
public class ConfigEntrypoint {

}
