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

package org.springframework.osgi.iandt.web;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * java.net based Http client for testing whether http resources are up.
 * Suitable for integration testing to check if pages/servlets/taglibs & co are
 * present.
 * 
 * @author Costin Leau
 * 
 */
public abstract class HttpClient {

	/** logger */
	private static final Log log = LogFactory.getLog(HttpClient.class);

	public static final String LOCAL_ADDRESS = "http://localhost:8080/";

	private static final String HTTP_PROTOCOL = "http";

	private static final String PROTOCOL_DELIMITER = "://";

	private static final String SLASH = "/";

	private static final String CON_TIMEOUT_SYS_PROP = "sun.net.client.defaultConnectTimeout";

	private static final String READ_TIMEOUT_SYS_PROP = "sun.net.client.defaultReadTimeout";

	/** default 15 seconds (nightly build seems to be quite slow) */
	private static final String DEFAULT_TIMEOUT = "" + (15 * 1000);

	private static final String READ_TIMEOUT = "" + 15 * 1000;


	public static HttpResponse getLocalResponse(String base, String resource) throws Exception {

		String res;
		if (base.endsWith(SLASH) || resource.startsWith(SLASH))
			res = base.concat(resource);
		else
			res = base.concat(SLASH).concat(resource);

		return getLocalResponse(res);
	}

	/**
	 * Connect to a local address. This is used as a shortcut for the
	 * integration tests. The given String will always be looked up locally at
	 * port 8080.
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse getLocalResponse(String resource) throws Exception {
		Assert.notNull(resource);
		return getResponse(LOCAL_ADDRESS.concat(resource));
	}

	public static HttpResponse getResponse(String address) throws Exception {

		// on JDK 1.5 there is a dedicated method - but not on 1.4...
		System.setProperty(CON_TIMEOUT_SYS_PROP, DEFAULT_TIMEOUT);
		System.setProperty(READ_TIMEOUT_SYS_PROP, READ_TIMEOUT);

		Assert.notNull(address);
		if (!address.startsWith(HTTP_PROTOCOL))
			address = HTTP_PROTOCOL.concat(PROTOCOL_DELIMITER).concat(address);

		log.info("creating connection to [" + address + "] ...");
		// create the URL
		URL url = new URL(address);

		URLConnection urlCon = url.openConnection();
		Assert.isInstanceOf(HttpURLConnection.class, urlCon, "only http(s) connections supported");

		HttpURLConnection http = (HttpURLConnection) urlCon;
		//
		// adjust the connection settings (before connecting)
		//
		// no cache
		http.setUseCaches(false);

		try {
			http.connect();
			return new HttpResponse(http);
		}
		finally {
			http.disconnect();
			// erase the timeout property (since it's system wide)
			System.setProperty(CON_TIMEOUT_SYS_PROP, "");
			System.setProperty(READ_TIMEOUT_SYS_PROP, "");
		}
	}
}
