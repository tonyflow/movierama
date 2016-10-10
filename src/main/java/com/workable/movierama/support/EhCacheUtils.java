package com.workable.movierama.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CommonsLogWriter;

/**
 * Methods used by the {@code EhCacheController}.
 * 
 * @author niko.strongioglou
 */
public abstract class EhCacheUtils {
	private static final Log log = LogFactory.getLog(EhCacheUtils.class);

	private EhCacheUtils() {
	}

	public static void clearCache() {
		for (CacheManager cm : CacheManager.ALL_CACHE_MANAGERS) {
			if (cm.getStatus().equals(Status.STATUS_ALIVE)) {
				for (String name : cm.getCacheNames()) {
					Ehcache cache = cm.getEhcache(name);
					if (cache.getStatus().equals(Status.STATUS_ALIVE)) {
						if (log.isTraceEnabled()) {
							log.trace("Cache " + name + " contains " + cache.getSize() + " objects.");
						}
						cache.removeAll();
						if (log.isTraceEnabled()) {
							log.trace("Clearing cache " + name + ". Now contains " + cache.getSize() + " objects.");
						}
					}
				}
			}
		}
	}

	public static void inspectCache() {
		try {
			inspectCache(new CommonsLogWriter(log));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void inspectCache(Writer writer) throws IOException {
		PrintWriter out = new PrintWriter(writer);
		for (CacheManager cm : CacheManager.ALL_CACHE_MANAGERS) {
			if (cm.getStatus().equals(Status.STATUS_ALIVE)) {
				for (String name : cm.getCacheNames()) {
					Ehcache cache = cm.getEhcache(name);
					if (cache.getStatus().equals(Status.STATUS_ALIVE)) {
						out.println(" ================== Cache Manager:[" + cm.getName() + "], Region: [" + cache.getName() + "] (" + cache.getSize()
								+ " objects) ==================");
						List keys = new ArrayList(cache.getKeys());
						Collections.sort(keys, new Comparator<Object>() {
							@Override
							public int compare(Object o1, Object o2) {
								return o1.toString().compareTo(o2.toString());
							}
						});
						for (Object key : keys) {
							Element element = cache.getQuiet(key);
							if (element != null) {
								out.println(
										element.getObjectKey() + (element.isExpired() ? " (EXPIRED) " : "") +
												" hitCount=" + element.getHitCount() +
												", creationTime=" + Instant.ofEpochMilli(element.getCreationTime()) +
												", lastAccessTime=" + Instant.ofEpochMilli(element.getLastAccessTime()) +
												", value=" + getObjectValue(element.getObjectValue()) + "\n");
							} else {
								out.println("null");
							}
						}
					}
				}
			}
		}
	}

	public static Object getObjectValue(Object object) {
		if (object != null) {
			Class<? extends Object> objectClass = object.getClass();
			try {
				Field value = objectClass.getDeclaredField("value");
				value.setAccessible(true);
				return value.get(object);
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		return object;
	}
}