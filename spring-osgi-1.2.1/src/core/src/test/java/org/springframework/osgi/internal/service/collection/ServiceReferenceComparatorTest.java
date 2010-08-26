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

package org.springframework.osgi.internal.service.collection;

import java.util.Comparator;
import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.service.importer.support.internal.collection.comparator.ServiceReferenceComparator;

/**
 * @author Costin Leau
 * 
 */
public class ServiceReferenceComparatorTest extends TestCase {

	private Comparator comparator;


	protected void setUp() throws Exception {
		comparator = new ServiceReferenceComparator();
	}

	protected void tearDown() throws Exception {
		comparator = null;
	}

	public void testServiceRefsWithTheSameId() throws Exception {
		ServiceReference refA = createReference(new Long(1), null);
		ServiceReference refB = createReference(new Long(1), null);

		// refA is higher then refB
		assertEquals(0, comparator.compare(refA, refB));
	}

	public void testServiceRefsWithDifferentIdAndNoRanking() throws Exception {
		ServiceReference refA = createReference(new Long(1), null);
		ServiceReference refB = createReference(new Long(2), null);

		// refA is higher then refB
		// default ranking is equal
		assertTrue(comparator.compare(refA, refB) > 0);
	}

	public void testServiceRefsWithDifferentIdAndDifferentRanking() throws Exception {
		ServiceReference refA = createReference(new Long(1), new Integer(0));
		ServiceReference refB = createReference(new Long(2), new Integer(1));

		// refB is higher then refA (due to ranking)
		assertTrue(comparator.compare(refA, refB) < 0);
	}

	public void testServiceRefsWithSameRankAndDifId() throws Exception {
		ServiceReference refA = createReference(new Long(1), new Integer(5));
		ServiceReference refB = createReference(new Long(2), new Integer(5));

		// same ranking, means id equality applies
		assertTrue(comparator.compare(refA, refB) > 0);
	}

	public void testNullObjects() throws Exception {
		assertEquals(0, comparator.compare(null, null));
	}

	public void testNonNullWithNull() throws Exception {
		try {
			comparator.compare(new MockServiceReference(), null);
			fail("should have thrown exception");
		}
		catch (ClassCastException cce) {
		}
	}

	private ServiceReference createReference(Long id, Integer ranking) {
		Dictionary dict = new Properties();
		dict.put(Constants.SERVICE_ID, id);
		if (ranking != null)
			dict.put(Constants.SERVICE_RANKING, ranking);

		return new MockServiceReference(null, dict, null);
	}
}
