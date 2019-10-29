package de.unipassau.sep19.hafenkran.clusterservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("images")
public class ImageController {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.FOUND)
    public ImageController imageController() {
        ImageController ic = new ImageController();
        log.info(ic.toString());
        return ic;
    }

    /*
    @Data
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    private class HelloWorld {
        @JsonProperty(value = "message")
        private final String message = "Hello World!";
    }

     */
}
