package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeMetricsDTO {

    @JsonProperty("metadata")
    MetadataDTO metadata;

    @JsonProperty("usage")
    UsageDTO usage;

    @JsonCreator
    public NodeMetricsDTO(@JsonProperty("metadata") MetadataDTO metadata, @JsonProperty("usage")
            UsageDTO usage) {
        this.metadata = metadata;
        this.usage = usage;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataDTO {

        @JsonProperty("name")
        String name;

        @JsonCreator
        public MetadataDTO(
                @JsonProperty("name") String name) {
            this.name = name;
        }

    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageDTO {

        //used for removing units from kubernetes
        private static final String REGEX = "[^0-9]";

        @JsonProperty("memory")
        String memory;

        @JsonProperty("cpu")
        String cpu;

        @JsonCreator
        public UsageDTO(@JsonProperty("memory") String memory, @JsonProperty("cpu") String cpu) {
            this.memory = memory.replaceAll(REGEX, "");
            this.cpu = cpu.replaceAll(REGEX, "");
        }
    }
}