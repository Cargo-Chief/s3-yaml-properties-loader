package com.cargochief.spring.properties.configuration;


import java.io.IOException;
import java.io.InputStream;

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

import com.spring.loader.cloud.S3StreamLoader;

public class S3YamlPropertiesSourceConfigurer
        implements EnvironmentAware, BeanFactoryPostProcessor, PriorityOrdered {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(S3YamlPropertiesSourceConfigurer.class);

    private Environment environment;
    private S3StreamLoader s3ResourceLoader;
    private String[] locations;

    public void setS3ResourceLoader(S3StreamLoader s3ResourceLoader) {
        this.s3ResourceLoader = s3ResourceLoader;
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
                InputStream in = s3ResourceLoader.getProperty(location);
                InputStreamResource resource = new InputStreamResource(in);
                PropertySource<?> propertySource =
                        sourceLoader.load(location, resource, null);
                if (propertySource != null) {
                    propertySources.addFirst(propertySource);

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

}

