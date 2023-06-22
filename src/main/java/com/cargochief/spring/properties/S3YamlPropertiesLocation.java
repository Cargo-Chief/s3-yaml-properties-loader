package com.cargochief.spring.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.cargochief.spring.properties.configuration.S3YamlPropertiesLocationRegistrar;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(S3YamlPropertiesLocationRegistrar.class)
public @interface S3YamlPropertiesLocation {

    /**
     * The location of the properties in aws s3.
     *
     * @return the location of the properties files in s3 e.g. "my-bucket/my-folder/application.yml"
     */
    String[] value();

    /**
     * Spring profiles to load the properties for.
     *
     * @return the profile name e.g. "prod"
     */
    String[] profiles() default {};

}
