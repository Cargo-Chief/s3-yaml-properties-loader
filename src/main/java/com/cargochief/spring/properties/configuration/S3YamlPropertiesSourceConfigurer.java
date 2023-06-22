package com.cargochief.spring.properties.configuration;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.cargochief.spring.properties.exception.InvalidS3LocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.StringUtils;

public class S3YamlPropertiesSourceConfigurer
        implements EnvironmentAware, BeanFactoryPostProcessor, PriorityOrdered {
    private static final String S3_PROTOCOL_PREFIX = "s3://";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(S3YamlPropertiesSourceConfigurer.class);

    private Environment environment;
    private String[] locations;
    private AmazonS3 amazonS3;

    public void setAmazonS3(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        if (this.environment instanceof ConfigurableEnvironment) {

            PropertiesFactoryBean propertiesFactory = new PropertiesFactoryBean();
            MutablePropertySources propertySources =
                    ((ConfigurableEnvironment) this.environment).getPropertySources();

            YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();

            try {
                propertiesFactory.setSingleton(false);
                propertiesFactory.afterPropertiesSet();

                for (final String location : locations) {
                    processLocation(sourceLoader, propertySources, location);
                }

            } catch (IOException e) {
                LOGGER.error("Could not configure spring properties", e);
            }

        } else {
            LOGGER.warn(
                    "Environment is not of type '{}' property source with instance data is not available",
                    ConfigurableEnvironment.class.getName());
        }
    }

    private void processLocation(
            YamlPropertySourceLoader sourceLoader,
            MutablePropertySources propertySources,
            String location) {
        try {
            if (!StringUtils.isEmpty(location)) {
                GetObjectRequest getObjectRequest = parseGetObjectRequestFromS3Location(location);
                S3Object responseObject = amazonS3.getObject(getObjectRequest);

                InputStream in = responseObject.getObjectContent();
                InputStreamResource resource = new InputStreamResource(in);
                List<PropertySource<?>> propertySource = sourceLoader.load(location, resource);
                if (propertySource != null) {
                    for (PropertySource propSource : propertySource) {
                        propertySources.addFirst(propSource);
                    }

                    String msg = String.format(
                            "Loaded yaml properties from: %s",
                            locations != null ? location : "null");
                    LOGGER.info(msg);
                } else {
                    String msg = String.format(
                            "No properties loaded from: %s",
                            locations != null ? location : "null");
                    LOGGER.info(msg);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Could not load properties from location " + location, e);
        }
    }

    private GetObjectRequest parseGetObjectRequestFromS3Location(String location) {
        String path = location.startsWith(S3_PROTOCOL_PREFIX) ? location.substring(S3_PROTOCOL_PREFIX.length(), location.length()) : location;

        if(!path.contains("/")) {
            throw new InvalidS3LocationException("The location must contains the full path of the properties file");
        }

        String bucketName = path.substring(0, path.indexOf('/'));
        String keyName = path.substring(path.indexOf('/') + 1);

        return new GetObjectRequest(bucketName, keyName);
    }

}

