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

package org.springframework.osgi.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SimpleLoggerTest extends TestCase {

	class AssertivePrintStream extends PrintStream {

		public AssertivePrintStream(OutputStream out, boolean autoFlush, String encoding)
				throws UnsupportedEncodingException {
			super(out, autoFlush, encoding);
		}

		public AssertivePrintStream(OutputStream out, boolean autoFlush) {
			super(out, autoFlush);
		}

		public AssertivePrintStream(OutputStream out) {
			super(out);
		}

		public void println(Object x) {
			loggingCalled(this, x);
		}
	}

	class NullOutputStream extends OutputStream {

		public void write(int b) throws IOException {
			// do nothing
		}
	}

	class MyThrowable extends Exception {

		public void printStackTrace(PrintStream s) {
			assertSame("the right stream [" + shouldBeCalled + "] is not called", shouldBeCalled, s);
			super.printStackTrace(s);
		}
	}


	private PrintStream outStream, errStream;
	private PrintStream shouldBeCalled, shouldNotBeCalled;
	private Log simpleLogger;
	private Object object;
	private Throwable throwable;


	protected void setUp() throws Exception {
		outStream = new AssertivePrintStream(new NullOutputStream());
		errStream = new AssertivePrintStream(new NullOutputStream());
		System.setErr(errStream);
		System.setOut(outStream);

		simpleLogger = new SimpleLogger();
		object = new Object();
		throwable = new MyThrowable();
	}

	protected void tearDown() throws Exception {
		System.setErr(null);
		System.setOut(null);
		simpleLogger = null;
		object = null;
		throwable = null;
	}

	private void loggingCalled(AssertivePrintStream assertivePrintStream, Object x) {
		assertSame("the right stream [" + shouldBeCalled + "] is not called", shouldBeCalled, assertivePrintStream);
		assertNotSame("the wrong stream [" + shouldBeCalled + "] is called", shouldNotBeCalled, assertivePrintStream);
	}

	public void testDebugObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.debug(object);
	}

	public void testDebugObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.debug(object, throwable);
	}

	public void testErrorObject() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.error(object);
	}

	public void testErrorObjectThrowable() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.error(object, throwable);
	}

	public void testFatalObject() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.fatal(object);
	}

	public void testFatalObjectThrowable() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.fatal(object, throwable);
	}

	public void testInfoObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object);
	}

	public void testInfoObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object, throwable);
	}

	public void testIsDebugEnabled() {
		assertTrue(simpleLogger.isDebugEnabled());
	}

	public void testIsErrorEnabled() {
		assertTrue(simpleLogger.isErrorEnabled());
	}

	public void testIsFatalEnabled() {
		assertTrue(simpleLogger.isFatalEnabled());
	}

	public void testIsInfoEnabled() {
		assertTrue(simpleLogger.isInfoEnabled());
	}

	public void testIsTraceEnabled() {
		assertTrue(simpleLogger.isTraceEnabled());
	}

	public void testIsWarnEnabled() {
		assertTrue(simpleLogger.isWarnEnabled());
	}

	public void testTraceObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.trace(object);
	}

	public void testTraceObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object, throwable);
	}

	public void testWarnObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.warn(object);
	}

	public void testWarnObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.warn(object, throwable);
	}
}
