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

import org.springframework.core.io.Resource;
import org.springframework.osgi.test.internal.util.jar.storage.FileSystemStorage;
import org.springframework.osgi.test.internal.util.jar.storage.Storage;

/**
 * @author Costin Leau
 * 
 */
public class FileSystemStorageTest extends AbstractStorageTest {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.osgi.test.util.StorageGenericTest#createStorage()
	 */
	protected Storage createStorage() {
		return new FileSystemStorage();
	}

	public void testResourceForTempFile() throws Exception {
		Resource res = storage.getResource();
		assertTrue(res.exists());
		File tempFile = res.getFile();
		assertTrue(tempFile.exists());
		assertTrue(tempFile.canRead());
		assertTrue(tempFile.canWrite());
	}

	public void testDispose() throws Exception {
		Resource res = storage.getResource();
		File file = res.getFile();
		assertTrue(res.exists());
		assertTrue(file.exists());
		storage.dispose();
		assertFalse(res.exists());
		assertFalse(file.exists());
	}

}
