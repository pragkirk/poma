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
 *
 * Created on 25-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi.context.support;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

/**
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class BundleContextAwareProcessorTest extends TestCase{

	private MockControl bundleContextControl;
	private MockControl bundleContextAwareControl;
	private BundleContext mockContext;
	private BundleContextAware mockAware;
	
	protected void setUp() throws Exception {
		this.bundleContextControl = MockControl.createControl(BundleContext.class);
		this.mockContext = (BundleContext) this.bundleContextControl.getMock();
		// no tests should ever call the mockContext, we're really
		// using it just as a convenient implementation
		this.bundleContextControl.replay();
		
		this.bundleContextAwareControl = MockControl.createControl(BundleContextAware.class);
		this.mockAware = (BundleContextAware) this.bundleContextAwareControl.getMock();
	}
	
	protected void tearDown() throws Exception {
		this.bundleContextControl.verify();
	}
	
	public void testBeforeInitializationNoBundleContext() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(null);
		this.bundleContextAwareControl.replay();
		try {
			//bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
			fail("should throw an IllegalStateException when no BundleContext available");
		} 
		catch(IllegalStateException ex) {
			assertEquals("Cannot satisfy BundleContextAware for bean 'aName' without BundleContext",
					     ex.getMessage());
		}
		this.bundleContextAwareControl.verify();
	}
	
	public void testBeforeInitializationNonImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object bean = new Object();
		Object ret = bcaProcessor.postProcessBeforeInitialization(bean, "aName");
		assertSame("should return same bean instance",bean,ret);
	}
	
	public void testBeforeInitializationBundleContextImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		this.mockAware.setBundleContext(this.mockContext);
		this.bundleContextAwareControl.replay();
		//boolean ret = bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
		this.bundleContextAwareControl.verify();
		//assertTrue("should return true",ret);
	}
	
	public void testAfterInitialization() {
		Object bean = new Object();
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object ret = bcaProcessor.postProcessAfterInitialization(bean, "aName");
		assertSame("should return the same bean instance",bean,ret);
	}
}
