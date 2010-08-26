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

package org.springframework.osgi.test.provisioning;

import org.springframework.core.io.Resource;

/**
 * Interface describing the contract for finding dependencies artifacts.
 * Implementations can rely on various lookup strategies for finding the actual
 * artifacts (i.e. Maven, Ant, Ivy, etc...)
 * 
 * @author Costin Leau
 */
public interface ArtifactLocator {

	/** default artifact type */
	String DEFAULT_ARTIFACT_TYPE = "jar";


	/**
	 * Locates the artifact under the given group, with the given id, version
	 * and type. Implementations are free to provide defaults, in case
	 * <code>null</code> values are passed in. The only required field is #id.
	 * 
	 * @param group artifact group (can be <code>null</code>)
	 * @param id artifact id or name (required)
	 * @param version artifact version (can be <code>null</code>)
	 * @param type artifact type (can be <code>null</code>)
	 * 
	 * @return Spring resource to the located artifact
	 */
	Resource locateArtifact(String group, String id, String version, String type);

	/**
	 * Locates the artifact under the given group, with the given id, version
	 * and type. This is a shortcut version which uses the implementation
	 * default artifact type {@link #DEFAULT_ARTIFACT_TYPE}.
	 * 
	 * @param group artifact group (can be <code>null</code>)
	 * @param id artifact id or name (required)
	 * @param version artifact version (can be <code>null</code>)
	 * @return Spring resource to the located artifact
	 */
	Resource locateArtifact(String group, String id, String version);
}
