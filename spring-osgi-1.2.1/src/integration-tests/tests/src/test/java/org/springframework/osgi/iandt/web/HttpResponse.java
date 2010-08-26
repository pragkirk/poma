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

/**
 * Simple http response.
 * 
 * @author Costin Leau
 * 
 */
public class HttpResponse {

	// made public to avoid the usage of a getter (less code)
	public final int code;
	public final String message;
	private final String toString;


	public HttpResponse(HttpURLConnection connection) throws Exception {
		this(connection.getURL().toExternalForm(), connection.getResponseCode(), connection.getResponseMessage());

	}

	public HttpResponse(String address, int code, String message) {
		this.code = code;
		this.message = message;
		this.toString = address + " returned " + "[" + message + "|" + code + "]";
	}

	// simple testing methods
	public boolean isNotFound() {
		return code == HttpURLConnection.HTTP_NOT_FOUND;
	}

	public boolean isUnavailable() {
		return code == HttpURLConnection.HTTP_UNAVAILABLE;
	}

	public boolean isOk() {
		return code == HttpURLConnection.HTTP_OK;
	}

	public String toString() {
		return toString;
	}

}
