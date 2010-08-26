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

package org.springframework.osgi.samples.simplewebapp.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that serves resources from its war.
 * 
 * @author Costin Leau
 * 
 */
public class ResourceServingServlet extends HttpServlet {

	private static final String SLASH = "/";
	private static final String RESOURCE_PARAM = "resource";


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		serveResource(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		serveResource(req, resp);
	}

	public String getServletInfo() {
		return "Resource Serving Servlet";
	}

	private void serveResource(HttpServletRequest request, HttpServletResponse response) throws IOException {

		ServletOutputStream out = response.getOutputStream();

		String resourceName = request.getParameter(RESOURCE_PARAM);
		if (resourceName == null || resourceName.trim().length() < 1) {
			out.println("No resource specified, returning the list of available resources...");
			listAvailableResources(response);
		}

		else {

			if (!resourceName.startsWith(SLASH))
				resourceName = SLASH + resourceName;

			URL sourceURL = getServletContext().getResource(resourceName);

			if (sourceURL == null) {
				out.println("Resource [" + resourceName + "] not found, returning the list of available resources...");
				listAvailableResources(response);
			}
			else {
				URLConnection connection = sourceURL.openConnection();
				connection.setUseCaches(false);
				response.setContentLength(connection.getContentLength());
				response.setContentType(connection.getContentType());
				sendStreamToClient(connection.getInputStream(), out);
			}
		}
		out.close();
	}

	private void listAvailableResources(HttpServletResponse response) throws IOException {
		Set sortedResources = new TreeSet();

		discoverFolderContent(sortedResources, SLASH);
		response.setContentType("text/plain");
		ServletOutputStream out = response.getOutputStream();
		for (Iterator iterator = sortedResources.iterator(); iterator.hasNext();) {
			out.println((String) iterator.next());
		}
	}

	private void discoverFolderContent(Set aggregatedContent, String path) {
		Set resources = getServletContext().getResourcePaths(path);
		for (Iterator resourceIterator = resources.iterator(); resourceIterator.hasNext();) {
			String resource = (String) resourceIterator.next();
			aggregatedContent.add(resource);
			if (resource.endsWith(SLASH)) {
				// recursively add folder
				discoverFolderContent(aggregatedContent, resource);
			}
		}
	}

	/**
	 * Copies the bytes from one {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	private void sendStreamToClient(InputStream source, OutputStream target) throws IOException {
		try {
			int i;
			while ((i = source.read()) != -1)
				target.write(i);
			target.flush();
		}
		finally {
			source.close();
		}
	}
}
