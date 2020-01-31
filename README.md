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


```

experimentsFileUploadLocation:          # location for storage of uploaded experiments

jwt:
  secret:                               # JWT secret used for signing and validation

kubernetes:                             # settings for the target kubernetes infrastructure
  deployment:
    defaults:                           # default pod resource limits
      ram: 1000
      cpu: 10
      bookedTime: 3600
  defaultLogLines: 500
  debugging: false
  pod-cleanup-scheduler-delay: 60
  config:
    load-default: true
    path: /kubernetes/config
  metrics:
    path: http://localhost:8001/apis/metrics.k8s.io/v1beta1
  mock:
    kubernetesClient: false
    metricsServer: false
  namespace:
    limits:                             # resource limits for namespaces
      cpu: 500
      memory: 10000

dockerClient:                           # docker registry settings used for image upload
  username:
  password:
  email:
  certPath:
  configPath:
  tls: 
  host:
  authKey:

service-user:
  name:                         # name of the user used for microservice communication
  password:                     # password of the user used for microservice communication
  secret:                       # secret token used as request parameter for internal service calls
```