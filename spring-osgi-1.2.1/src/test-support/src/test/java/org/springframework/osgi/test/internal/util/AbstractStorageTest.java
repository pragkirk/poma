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

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.core.io.Resource;
import org.springframework.osgi.test.internal.util.jar.storage.Storage;
import org.springframework.util.FileCopyUtils;

/**
 * Set of storage tests which can be applied on various Storage
 * implementations.This class should be subclassed to run the test against
 * concrete Storage implementations.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractStorageTest extends TestCase {

	protected Storage storage;

	/**
	 * Create the actual storage.
	 * 
	 * @return
	 */
	protected abstract Storage createStorage();

	protected void setUp() throws Exception {
		storage = createStorage();
	}

	protected void tearDown() throws Exception {
		storage.dispose();
	}

	public void testInitialInputStream() throws Exception {
		InputStream in = storage.getInputStream();
		try {
			assertEquals(-1, in.read());
		}
		finally {
			IOUtils.closeStream(in);
		}
	}

	public void testIdenticalInputStreams() throws Exception {
		assertTrue("streams not identical", compareStreams(storage.getInputStream(), storage.getInputStream()));
	}

	public void testIdenticalInputStreamsFromResource() throws Exception {
		assertTrue("streams not identical", compareStreams(storage.getResource().getInputStream(), storage
				.getResource().getInputStream()));
	}

	public void testReadWrite() throws Exception {
		int wrote = FileCopyUtils.copy(getSampleContentAsInputStream(), storage.getOutputStream());
		System.out.println("wrote " + wrote + " bytes");
		assertTrue("streams content is different", compareStreams(getSampleContentAsInputStream(), storage
				.getInputStream()));
	}

	public void testResource() throws Exception {
		Resource res = storage.getResource();
		assertNotNull(res);
		assertFalse("underlying storage is not reusable", res.isOpen());
	}

	public void testCompareInputStreamAndResourceInputStream() throws Exception {
		InputStream in1 = storage.getInputStream();
		InputStream in2 = storage.getResource().getInputStream();
		assertTrue("streams content is different", compareStreams(in1, in2));
	}

	private boolean compareStreams(InputStream in1, InputStream in2) throws Exception {
		int count = 0;
		try {
			int b;
			while ((b = in1.read()) != -1) {
				count++;
				int a = in2.read();
				boolean same = (a == b);
				if (!same) {
					System.out.println("expected " + b + " but was " + a + ";problem occured after reading " + count + " bytes");
					return false;
				}
			}
			// check we have reached the end on both streams
			return (in1.read() == in2.read());
		}
		finally {
			IOUtils.closeStream(in1);
			IOUtils.closeStream(in2);
		}
	}

	private InputStream getSampleContentAsInputStream() throws Exception {
		return getClass().getClassLoader().getResourceAsStream(getClass().getName().replace('.', '/') + ".class");
	}
}
