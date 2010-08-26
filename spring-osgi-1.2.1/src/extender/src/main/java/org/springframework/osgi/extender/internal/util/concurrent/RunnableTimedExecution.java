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

package org.springframework.osgi.extender.internal.util.concurrent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

/**
 * Utility class that executes the given Runnable task on the given task
 * executor or , if none is given, to a new thread.
 * 
 * <p/> If the thread does not return in the given amount of time, it will be
 * interrupted and a logging message sent.
 * 
 * <p/> This class is intended for usage inside the framework, mainly by the
 * extender package for controlling runaway threads.
 * 
 * @see Counter
 * @see Thread
 * @author Costin Leau
 * 
 */
public abstract class RunnableTimedExecution {

	/** logger */
	private static final Log log = LogFactory.getLog(RunnableTimedExecution.class);


	private static class MonitoredRunnable implements Runnable {

		private Runnable task;

		private Counter counter;


		public MonitoredRunnable(Runnable task, Counter counter) {
			this.task = task;
			this.counter = counter;
		}

		public void run() {
			try {
				task.run();
			}
			finally {
				counter.decrement();
			}
		}
	}

	private static class SimpleTaskExecutor implements TaskExecutor, DisposableBean {

		private Thread thread;


		public void execute(Runnable task) {
			thread = new Thread(task);
			thread.setName("Thread for runnable [" + task + "]");
			thread.start();
		}

		public void destroy() throws Exception {
			if (thread != null) {
				thread.interrupt();
			}
		}
	}


	public static boolean execute(Runnable task, long waitTime) {
		return execute(task, waitTime, null);
	}

	public static boolean execute(Runnable task, long waitTime, TaskExecutor taskExecutor) {
		Assert.notNull(task);

		Counter counter = new Counter("counter for task: " + task);
		Runnable wrapper = new MonitoredRunnable(task, counter);

		boolean internallyManaged = false;

		if (taskExecutor == null) {
			taskExecutor = new SimpleTaskExecutor();
			internallyManaged = true;
		}

		counter.increment();

		taskExecutor.execute(wrapper);

		if (counter.waitForZero(waitTime)) {
			log.error(task + " did not finish in " + waitTime
					+ "ms; consider taking a snapshot and then shutdown the VM in case the thread still hangs");

			if (internallyManaged) {
				try {
					((DisposableBean) taskExecutor).destroy();
				}
				catch (Exception e) {
					// no exception is thrown, nothing to worry
				}
			}
			return true;
		}

		return false;
	}
}
