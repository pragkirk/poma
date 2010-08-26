/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.osgi.web.deployer.support;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Dictionary;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.osgi.web.deployer.ContextPathStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link ContextPathStrategy} default implementation. This class takes into account the OSGi bundle properties for
 * determining the war context path. by iterating through the following properties, considering the first one that is
 * available in the following order:
 * 
 * <ol> <li><tt>Web-ContextPath</tt> manifest header (identical to the one in SpringSource Application Platform). If
 * present, the value of this header will be used as the context path. </li>
 * 
 * <li>bundle location - if present, the implementation will try to determine if the location points to a file or a
 * folder. In both cases, the name will be returned without the extension (if it's present):
 * 
 * <pre class="code"> /root/bundle.jar -&gt; /bundle /root/bundle/ -&gt; /bundle /root/bundle.jar/ -&gt; /bundle
 * file:/path/bundle.jar -&gt; /bundle jar:url:/root/bundle.jar -&gt; /bundle </pre>
 * 
 * </li> <li>bundle name - if present, it is used as a fall back to the bundle location (ex: <tt>/myBundle</tt>)</li>
 * <li>bundle symbolic name - if present, it used as a fall back to the bundle name (ex:
 * <tt>/org.comp.osgi.some.bundle</tt>)</li> <li>bundle identity - if neither of the properties above is present, the
 * bundle object identity will be used as context path (ex: <tt>/BundleImpl-15a0305</tt>)</li> </ol>
 * 
 * Additionally, the returned context path will be HTML encoded (using 'UTF-8') to avoid problems with unsafe characters
 * (such as whitespace).
 * 
 * @see Bundle#getLocation()
 * @see Constants#BUNDLE_NAME
 * @see Bundle#getSymbolicName()
 * @see System#identityHashCode(Object)
 * @see URLEncoder#encode(String, String)
 * 
 * @author Costin Leau
 */
public class DefaultContextPathStrategy implements ContextPathStrategy {

	/** logger */
	private static final Log log = LogFactory.getLog(DefaultContextPathStrategy.class);

	private static String encodingScheme = "UTF-8";

	private static String CONTEXT_PATH_HEADER = "Web-ContextPath";

	private static final String SLASH = "/";

	private static final String PREFIX_DELIMITER = ":";

	// "/" should not be encoded
	private static final Pattern ENCODED_SLASH_PTRN = Pattern.compile("%2F");

	/** determine encoding */
	static {
		// do encoding
		try {
			URLEncoder.encode(" \"", encodingScheme);

		} catch (UnsupportedEncodingException e) {
			// platform default
			encodingScheme = null;
		}
	}

	public String getContextPath(Bundle bundle) {
		Assert.notNull(bundle);

		String path = determineContextPath(bundle);
		if (log.isTraceEnabled())
			log.trace("Detected context path [" + path + "]");

		String encodedPath = encodePath(path);
		if (log.isTraceEnabled())
			log.trace("Encoding context path [" + path + "] to [" + encodedPath + "]");

		// add leading slash
		return (encodedPath.startsWith(SLASH) ? encodedPath : SLASH.concat(encodedPath));
	}

	/**
	 * Determines the context path associated with this bundle. This method can be overridden by possible subclasses
	 * that wish to decorate or modify the existing behaviour.
	 * 
	 * @param bundle bundle for which the context path needs to be determined
	 * @return non-null context path determined for the given bundle
	 */
	protected String determineContextPath(Bundle bundle) {
		boolean trace = log.isTraceEnabled();

		// first get the header
		String path = getBundleHeader(bundle);

		if (path == null) {
			if (trace)
				log.trace("No " + CONTEXT_PATH_HEADER + " found; falling back to bundle location...");
			String location = bundle.getLocation();
			if (StringUtils.hasText(location)) {
				path = getBundleLocation(location);
			}
			// the location is not good, use a fall back
			else {
				if (trace)
					log.trace("No bundle location found; falling back to bundle name...");

				// fall-back to bundle name
				Dictionary headers = bundle.getHeaders();
				path = (headers != null ? (String) headers.get(Constants.BUNDLE_NAME) : null);

				if (!StringUtils.hasText(path)) {
					if (trace)
						log.trace("No bundle name found; falling back to bundle symbolic name...");

					// fall back to bundle sym name
					path = bundle.getSymbolicName();

					// fall back to object identity
					if (!StringUtils.hasText(path)) {
						path =
								ClassUtils.getShortName(bundle.getClass()) + "-"
										+ ObjectUtils.getIdentityHexString(bundle);
						if (trace)
							log.trace("No bundle symbolic found; returning bundle identity [" + path
									+ "] as context path");
					} else {
						if (trace)
							log.trace("Returning bundle symbolic name [" + path + "] as context path");
					}
				} else {
					if (trace)
						log.trace("Returning bundle name [" + path + "] as context path");
				}
			}
		} else {
			if (trace)
				log.trace("Using the bundle " + CONTEXT_PATH_HEADER + " header as context path [" + path + "]");
		}

		return path;
	}

	private String getBundleHeader(Bundle bundle) {
		Dictionary headers = bundle.getHeaders();
		if (headers == null)
			return null;

		String header = (String) headers.get(CONTEXT_PATH_HEADER);
		if (header != null) {
			header = header.trim();
			// check whether it starts with a "/"
			return (header.startsWith(SLASH) ? header.substring(1) : header);
		}
		return null;
	}

	private String getBundleLocation(String location) {
		// remove prefix (if there's any)
		int index = location.lastIndexOf(PREFIX_DELIMITER);
		String path = ((index > 0) ? location.substring(index + 1) : location);
		// clean up the path
		path = StringUtils.cleanPath(path);
		// check if it's a folder
		if (path.endsWith(SLASH)) {
			// remove trailing slash
			path = path.substring(0, path.length() - 1);
			int separatorIndex = path.lastIndexOf(SLASH);

			// if there is no other slash, consider the whole location, otherwise detect the folder
			path = (separatorIndex > -1 ? path.substring(separatorIndex + 1) : path);
		}
		path = StringUtils.getFilename(path);
		// remove file extension
		path = StringUtils.stripFilenameExtension(path);

		if (log.isTraceEnabled())
			log.trace("Bundle location [" + location + "] resulted in context path [" + path + "]");

		return path;
	}

	private String encodePath(String path) {
		try {
			String encode = URLEncoder.encode(path, encodingScheme);
			String result = ENCODED_SLASH_PTRN.matcher(encode).replaceAll(SLASH);
			return result;
		} catch (UnsupportedEncodingException ex) {
			throw (RuntimeException) new IllegalStateException((encodingScheme == null ? "default " : encodingScheme)
					+ " encoding scheme detected but unsable").initCause(ex);
		}
	}
}
