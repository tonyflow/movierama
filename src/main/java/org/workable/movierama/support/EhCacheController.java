package org.workable.movierama.support;

import java.io.Writer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
