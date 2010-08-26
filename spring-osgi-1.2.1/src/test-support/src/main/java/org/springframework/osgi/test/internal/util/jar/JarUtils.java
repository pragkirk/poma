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

package org.springframework.osgi.test.internal.util.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import org.springframework.core.io.Resource;

/**
 * Utility class for Jar files. As opposed to {@link JarCreator}, this class is
 * stateless and contains only static methods (hence the abstract qualifier).
 * 
 * @author Costin Leau
 * 
 */
public abstract class JarUtils {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static final String MANIFEST_JAR_LOCATION = "/META-INF/MANIFEST.MF";

	static final String SLASH = "/";


	/**
	 * Dumps the entries of a jar and return them as a String. This method can
	 * be memory expensive depending on the jar size.
	 * 
	 * @param jis
	 * @return
	 * @throws Exception
	 */
	public static String dumpJarContent(JarInputStream jis) {
		StringBuffer buffer = new StringBuffer();

		try {
			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null) {
				buffer.append(entry.getName());
				buffer.append("\n");
			}
		}
		catch (IOException ioException) {
			buffer.append("reading from stream failed");
		}
		finally {
			closeStream(jis);
		}

		return buffer.toString();
	}

	/**
	 * Dump the entries of a jar and return them as a String. This method can be
	 * memory expensive depending on the jar size.
	 * 
	 * @param resource
	 * @return
	 */
	public static String dumpJarContent(Resource resource) {
		try {
			return dumpJarContent(new JarInputStream(resource.getInputStream()));
		}
		catch (IOException ex) {
			return "reading from stream failed" + ex;
		}
	}

	/**
	 * Writes a resource content to a jar.
	 * 
	 * @param res
	 * @param entryName
	 * @param jarStream
	 * @return the number of bytes written to the jar file
	 * @throws Exception
	 */
	public static int writeToJar(Resource res, String entryName, JarOutputStream jarStream) throws IOException {
		return writeToJar(res, entryName, jarStream, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * 
	 * Writes a resource content to a jar.
	 * 
	 * @param res
	 * @param entryName
	 * @param jarStream
	 * @param bufferSize
	 * @return the number of bytes written to the jar file
	 * @throws Exception
	 */
	public static int writeToJar(Resource res, String entryName, JarOutputStream jarStream, int bufferSize)
			throws IOException {
		byte[] readWriteJarBuffer = new byte[bufferSize];

		// remove leading / if present.
		if (entryName.charAt(0) == '/')
			entryName = entryName.substring(1);

		jarStream.putNextEntry(new ZipEntry(entryName));
		InputStream entryStream = res.getInputStream();

		int numberOfBytes;

		// read data into the buffer which is later on written to the jar.
		while ((numberOfBytes = entryStream.read(readWriteJarBuffer)) != -1) {
			jarStream.write(readWriteJarBuffer, 0, numberOfBytes);
		}
		return numberOfBytes;
	}

	/**
	 * Read the manifest for a given stream. The stream will be wrapped in a
	 * JarInputStream and closed after the manifest was read.
	 * 
	 * @param stream
	 * @return
	 */
	public static Manifest getManifest(InputStream stream) {
		JarInputStream myStream = null;
		try {
			myStream = new JarInputStream(stream);
			return myStream.getManifest();
		}
		catch (IOException ioex) {
			// just ignore it
		}
		finally {
			closeStream(myStream);
		}

		// return (man != null ? man : new Manifest());
		return null;
	}

	/**
	 * Convenience method for reading a manifest from a given resource. Will
	 * assume the resource points to a jar.
	 * 
	 * @param resource
	 * @return
	 */
	public static Manifest getManifest(Resource resource) {
		try {
			return getManifest(resource.getInputStream());
		}
		catch (IOException ex) {
			// ignore
		}
		return null;
	}

	/**
	 * Creates a jar based on the given entries and manifest. This method will
	 * always close the given output stream.
	 * 
	 * @param manifest jar manifest
	 * @param entries map of resources keyed by the jar entry named
	 * @param outputStream output stream for writing the jar
	 * @return number of byte written to the jar
	 */
	public static int createJar(Manifest manifest, Map entries, OutputStream outputStream) throws IOException {
		int writtenBytes = 0;

		// load manifest
		// add it to the jar
		JarOutputStream jarStream = null;

		try {
			// add a jar stream on top
			jarStream = (manifest != null ? new JarOutputStream(outputStream, manifest) : new JarOutputStream(
				outputStream));

			// select fastest level (no compression)
			jarStream.setLevel(Deflater.NO_COMPRESSION);

			// add deps
			for (Iterator iter = entries.entrySet().iterator(); iter.hasNext();) {
				Map.Entry element = (Map.Entry) iter.next();

				String entryName = (String) element.getKey();

				// safety check - all entries must start with /
				if (!entryName.startsWith(SLASH))
					entryName = SLASH + entryName;

				Resource entryValue = (Resource) element.getValue();

				// skip special/duplicate entries (like MANIFEST.MF)
				if (MANIFEST_JAR_LOCATION.equals(entryName)) {
					iter.remove();
				}
				else {
					// write jar entry
					writtenBytes += JarUtils.writeToJar(entryValue, entryName, jarStream);
				}
			}
		}
		finally {
			try {
				jarStream.flush();
			}
			catch (IOException ex) {
				// ignore
			}
			try {
				jarStream.finish();
			}
			catch (IOException ex) {
				// ignore
			}

		}

		return writtenBytes;
	}

	private static void closeStream(InputStream stream) {
		if (stream != null)
			try {
				stream.close();
			}
			catch (IOException ex) {
				// ignore
			}
	}
}
