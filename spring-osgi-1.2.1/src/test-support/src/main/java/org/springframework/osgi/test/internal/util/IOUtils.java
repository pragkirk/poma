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
package org.springframework.osgi.test.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for IO operations.
 * 
 * @author Costin Leau
 * 
 */
public abstract class IOUtils {

	public static interface IOCallback {
		void doWithIO() throws IOException;
	}

	public static void doWithIO(IOCallback callback) {
		try {
			callback.doWithIO();
		}
		catch (IOException ioException) {

		}
	}

	public static void closeStream(InputStream stream) {
		if (stream != null)
			try {
				stream.close();
			}
			catch (IOException ex) {
				// ignore
			}
	}

	public static void closeStream(OutputStream stream) {
		if (stream != null)
			try {
				stream.close();
			}
			catch (IOException ex) {
				// ignore
			}
	}

	/**
	 * Delete the given file (can be a simple file or a folder).
	 * 
	 * @param file the file to be deleted
	 * @return if the deletion succeded or not
	 */
	public static boolean delete(File file) {

		// bail out quickly
		if (file == null)
			return false;

		// recursively delete children file
		boolean success = true;

		if (file.isDirectory()) {
			String[] children = file.list();
			for (int i = 0; i < children.length; i++) {
				success &= delete(new File(file, children[i]));
			}
		}

		// The directory is now empty so delete it
		return (success &= file.delete());
	}
}
