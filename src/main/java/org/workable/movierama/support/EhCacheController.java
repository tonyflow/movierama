package org.workable.movierama.support;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/ehcache")
public class EhCacheController {
	
	public String inspect(){
		return null;
	}
	
	public void evict(){
		
	}

}
