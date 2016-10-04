package org.workable.movierama;

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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages={"org.workable.movierama"})
public class MovieRamaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieRamaApplication.class, args);
	}
	
	@Bean
	RestTemplate restTemplate(ObjectMapper mapper){
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
	public EhCacheManagerFactoryBean ehCacheManager(){
		
		EhCacheManagerFactoryBean ecmfb = new EhCacheManagerFactoryBean();
		ecmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
		return ecmfb;
	}
	
	@Bean
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheManager().getObject());
	}
	
	

}
