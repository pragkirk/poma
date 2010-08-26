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

package org.springframework.osgi.test.provisioning.internal;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class LocalFileSystemMavenRepositoryTest extends TestCase {

	private LocalFileSystemMavenRepository repository;


	protected void tearDown() throws Exception {
		System.getProperties().remove("localRepository");
	}

	public void testSystemProperty() throws Exception {
		String SYS_PROP = "fake/sys/location";
		System.setProperty("localRepository", SYS_PROP);
		repository = new LocalFileSystemMavenRepository();
		assertTrue("system property not used", repository.locateArtifact("foo", "bar", "1.0").toString().indexOf(
			SYS_PROP) >= -1);
	}

	public void testLocalSettingsFile() throws Exception {
		repository = new LocalFileSystemMavenRepository();
		Resource res = new ClassPathResource("/org/springframework/osgi/test/provisioning/internal/settings.xml");
		String location = repository.getMavenSettingsLocalRepository(res);
		assertNotNull("location hasn't been picked up", location);
		assertEquals("wrong location discovered", location, "fake/location");
	}
}
