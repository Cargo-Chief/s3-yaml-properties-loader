# Spring S3 Yaml Property Loader

_S3 Yaml Property Loader_ has the aim of allowing loading of Spring property files from S3 bucket, in order to guarantee stateless machine configuration.

Spring PropertyConfigurer uses `PropertiesFactoryBean` to load property files from *AWS S3* bucket.


## How to use

- Adding this annotation to any spring managed bean
```java
@S3YamlPropertiesLocation("my-bucket/my-folder/my-properties.properties")
```
- Using a specific profile to only load properties if the app is running with that profile
```java
@S3YamlPropertiesLocation(value = "my-bucket/my-folder/my-properties.properties", profiles = "production")
```
- Load from a System env variable
```java
@S3YamlPropertiesLocation(value = "${AWS_S3_LOCATION}", profiles = "developer")
or
@S3YamlPropertiesLocation(value = "${AWS_S3_BUCKET}/application/my.properties", profiles = "developer")
```

## Requisites

Official [spring aws sdk lib](https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-aws).

## Problems and Issues

Found some bug? Have some enhancement ? Open a Issue [here](https://github.com/Cargo-Chief/s3-yaml-properties-loader/issues)