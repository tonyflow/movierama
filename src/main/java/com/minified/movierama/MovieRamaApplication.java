package com.minified.movierama;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Entry point of our application. {@code EhCacheManagerFactoryBean}'s,
 * {@code RestTemplate}'s and {@code CacheManager}'s bean configuration reside
 * in that class.
 * 
 * @author niko.strongioglou
 *
 */
@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = { "com.minified.movierama" })
public class MovieRamaApplication extends SpringServletContainerInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MovieRamaApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate(ObjectMapper mapper) {
		List<HttpMessageConverter<? extends Object>> converters = Arrays.asList(
				new ByteArrayHttpMessageConverter(),
				new StringHttpMessageConverter(),
				new ResourceHttpMessageConverter(),
				new SourceHttpMessageConverter<Source>(),
				new AllEncompassingFormHttpMessageConverter(),
				new MappingJackson2HttpMessageConverter(mapper));

		RestTemplate restTemplate = new RestTemplate(converters);
		return restTemplate;
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheManager() {

		EhCacheManagerFactoryBean ecmfb = new EhCacheManagerFactoryBean();
		ecmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
		ecmfb.setShared(true);
		return ecmfb;
	}

	@Bean
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager().getObject());
	}

}
