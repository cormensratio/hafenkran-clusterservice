package de.unipassau.sep19.hafenkran.clusterservice.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class HelloWorldController {

    @GetMapping("/hello")
    public @ResponseBody
    HelloWorld helloWorld() {
        HelloWorld hw = new HelloWorld();
        log.info(hw.toString());
        return hw;
    }

    @Data
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    private class HelloWorld {
        @JsonProperty(value = "message")
        private final String message = "Hello World!";
    }
}
