package com.minified.movierama.support;

import java.io.Writer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller enables the developer to inspect the elements of the cache
 * being updated every time a query is submitted to the application.
 * 
 * There is also a ehcache/clear POST request which can be performed in order
 * for the cache to evict all its elements. This utility was developed for
 * testing purposes mainly.
 * 
 * @author niko.strongioglou
 *
 */
@RestController
@RequestMapping(value = "/ehcache")
public class EhCacheController {

	@RequestMapping(value = "/clear", method = RequestMethod.POST)
	public String clearCache() throws Exception {
		EhCacheUtils.clearCache();
		return "OK";
	}

	@RequestMapping(value = "/inspect", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public void inspectCache(Writer writer) throws Exception {
		EhCacheUtils.inspectCache(writer);
	}

}
