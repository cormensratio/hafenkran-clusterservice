package de.unipassau.sep19.hafenkran.clusterservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricDTO {

    @JsonProperty("executionId")
    UUID executionId;

    @JsonProperty("ownerId")
    UUID ownerId;

    @JsonProperty("metadata")
    MetadataDTO metadata;

    @JsonProperty("containers")
    List<ContainerDTO> containers;

    @JsonProperty("timestamp")
    Timestamp timestamp;

    @JsonCreator
    public MetricDTO(@JsonProperty("metadata") MetadataDTO metadata, @JsonProperty("containers")
            List<ContainerDTO> containers) {
        this.metadata = metadata;
        this.containers = containers;
        Date date = new Date();
        long time = date.getTime();
        timestamp = new Timestamp(time);
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataDTO {

        @JsonProperty("name")
        String name;

        @JsonProperty("namespace")
        String namespace;

        @JsonCreator
        public MetadataDTO(
                @JsonProperty("name") String name,
                @JsonProperty("namespace")
                        String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContainerDTO {

        @JsonProperty("usage")
        UsageDTO usage;

        @JsonCreator
        public ContainerDTO(@JsonProperty("usage")
                                    UsageDTO usage) {
            this.usage = usage;
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
