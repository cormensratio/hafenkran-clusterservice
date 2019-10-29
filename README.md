# Hafenkran

## ClusterService

### Setup
Make sure that the required parameters in `application.yml` are configured, when deploying the application, otherwise startup will fail.

### Development
- Use the `dev` Spring profile configured in `application-dev.yml`to use the default configuration. 
    - In IntelliJ set the profile under `Active Profiles` in your build configuration
    - In maven use:
        > mvn spring-boot:run -Dspring-boot.run.profiles=dev
- This project requires `Lombok`. Please check that your IDE has the latest Lombok plugin installed.
- For logging use `@Slf4j` provided by Lombok.
- To use Spring devtools in IntelliJ set `On Update` and `On Frame deactivation` actions to `Update classes and resources` in your build configuration.