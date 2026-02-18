package com.cargochief.spring.properties.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.Map;

/**
 * Loads YAML properties from S3 before the application context starts,
 * making them available for @ConditionalOnProperty on @Configuration classes.
 *
 * <p>The existing {@link S3YamlPropertiesSourceConfigurer} mechanism uses a
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor},
 * which runs AFTER {@code @ConditionalOnProperty} is evaluated. This
 * {@code EnvironmentPostProcessor} ensures S3 properties are in the Environment
 * early enough for conditional beans to be resolved correctly.</p>
 *
 * <p>This processor scans all system environment variables for values that
 * start with {@code s3://} and end with {@code .yml} or {@code .yaml},
 * then loads them as YAML property sources.</p>
 */
public class S3YamlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String S3_PROTOCOL_PREFIX = "s3://";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		AmazonS3 s3Client = null;

		Map<String, Object> systemEnv = environment.getSystemEnvironment();
		for (Map.Entry<String, Object> entry : systemEnv.entrySet()) {
			String value = String.valueOf(entry.getValue());
			if (isS3YamlLocation(value)) {
				try {
					if (s3Client == null) {
						s3Client = AmazonS3ClientBuilder.defaultClient();
					}
					loadProperties(environment, loader, s3Client, value, entry.getKey());
				} catch (Exception e) {
					System.err.println("S3YamlEnvironmentPostProcessor: Failed to load from "
							+ entry.getKey() + ": " + e.getMessage());
				}
			}
		}
	}

	private boolean isS3YamlLocation(String value) {
		if (value == null || !value.startsWith(S3_PROTOCOL_PREFIX)) {
			return false;
		}
		String lower = value.toLowerCase();
		return lower.endsWith(".yml") || lower.endsWith(".yaml");
	}

	private void loadProperties(ConfigurableEnvironment environment, YamlPropertySourceLoader loader,
								AmazonS3 s3Client, String location, String envVarName) throws Exception {
		String path = location.substring(S3_PROTOCOL_PREFIX.length());
		int slashIndex = path.indexOf('/');
		if (slashIndex < 0) {
			return;
		}
		String bucket = path.substring(0, slashIndex);
		String key = path.substring(slashIndex + 1);

		S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucket, key));
		byte[] content = s3Object.getObjectContent().readAllBytes();

		List<PropertySource<?>> propertySources = loader.load(location, new ByteArrayResource(content));
		for (PropertySource<?> propertySource : propertySources) {
			environment.getPropertySources().addLast(propertySource);
		}

		System.out.println("S3YamlEnvironmentPostProcessor: Loaded properties from " + envVarName + " (" + location + ")");
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}
}
