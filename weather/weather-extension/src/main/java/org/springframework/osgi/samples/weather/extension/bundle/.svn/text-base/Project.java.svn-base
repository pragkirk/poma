/*
 * Copyright 2006-2009 the original author or authors.
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
 *
 * Taken from xbean 2.x on 4-May-2006 by Andy Piper
 */
package org.springframework.osgi.samples.weather.extension.bundle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Dain Sundstrom
 * @author Andy Piper
 */
/* package */ class Project implements Artifact
{
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String type;
  private final String jar;
  private final Set dependencies;
  private final Set/*<String>*/ exports;
  private final Set/*<String>*/ imports;

  public Project(String groupId, String artifactId, String version, String type,
                 Set dependencies, Set/*<String>*/ exports, Set/*<String>*/ imports) {
    this(groupId, artifactId, version, type, artifactId + "-" + version + "." + type,
        dependencies, exports, imports);
  }

  public Project(String groupId, String artifactId, String version, String type) {
    this(groupId, artifactId, version, type, artifactId + "-" + version + "." + type,
        Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET);
  }

  public Project(String groupId, String artifactId, String version, String type, String jar,
                 Set dependencies, Set/*<String>*/ exports, Set/*<String>*/ imports) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.type = type;
    this.exports = Collections.unmodifiableSet(exports);
    this.imports = Collections.unmodifiableSet(imports);
    this.jar = jar;

    Set deps = new HashSet();
    for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
      Artifact dependency = (Artifact) iterator.next();
      deps.add(dependency);
    }
    this.dependencies = Collections.unmodifiableSet(deps);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getType() {
    return type;
  }

  public String getJar() {
    return jar;
  }

  public Set/*<String>*/ getExports() {
    return exports;
  }

  public Set/*<String>*/ getImports() {
    return imports;
  }

  public URL getJarPath(String searchSpace) throws MalformedURLException {
    // Check for absolute URL in jar
    if (jar.indexOf(':') > 0) {
      return new URL(jar);
    }
    else {
      return new URL(searchSpace + "/"
          + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + getJar());
    }
  }

  public Set getDependencies() {
    return dependencies;
  }
}
