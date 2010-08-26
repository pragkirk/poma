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
package org.springframework.osgi.iandt.lifecycle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;

/**
 * @author Hal Hildebrand
 *         Date: Oct 15, 2006
 *         Time: 5:23:16 PM
 */
public class GuineaPig implements InitializingBean, DisposableBean, BundleContextAware {
    BundleContext bundleContext;
    Listener listener;


    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }


    public void afterPropertiesSet() throws Exception {
        System.setProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.startUp", "true");
        listener = new Listener();
        bundleContext.addFrameworkListener(listener);
    }


    public void destroy() throws Exception {
        bundleContext.removeFrameworkListener(listener);
        System.setProperty("org.springframework.osgi.iandt.lifecycle.GuineaPig.close", "true");
    }


    static class Listener implements FrameworkListener {
        public void frameworkEvent(FrameworkEvent frameworkEvent) {
            System.out.println("Eavesdropping on " + frameworkEvent);
        }
    }
}
