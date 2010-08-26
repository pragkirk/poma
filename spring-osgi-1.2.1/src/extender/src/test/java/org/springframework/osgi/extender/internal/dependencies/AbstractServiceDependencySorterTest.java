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
package org.springframework.osgi.extender.internal.dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.osgi.framework.Bundle;
import org.springframework.osgi.extender.internal.DependencyMockBundle;
import org.springframework.osgi.extender.internal.dependencies.shutdown.ServiceDependencySorter;
import org.springframework.util.ObjectUtils;

/**
 * Base testing suite for ordering bundles based on service dependencies. To
 * visualize the graph, see the .dot files in the same folder which can read
 * through Graphviz tool.
 *
 * @author Costin Leau
 */
public abstract class AbstractServiceDependencySorterTest extends TestCase {

	protected ServiceDependencySorter sorter;

	private int count = 1;

	protected void setUp() throws Exception {
		sorter = createSorter();
	}

	protected abstract ServiceDependencySorter createSorter();

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		sorter = null;
	}

	// A -> B -> C
	public void testSimpleTree() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");

		// A -> B -> C
		a.setDependentOn(b);
		b.setDependentOn(c);

		testDependencyTreeWithShuffle(new Bundle[]{c, b, a}, new Bundle[]{a, b, c});
	}

	// A -> B, C, D
	// B -> C, E
	// C -> E
	// D -> B
	public void testMediumTree() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");

		a.setDependentOn(new Bundle[]{d, c, b});
		b.setDependentOn(new Bundle[]{e, c});
		c.setDependentOn(e);
		d.setDependentOn(b);

		testDependencyTreeWithShuffle(new Bundle[]{e, c, b, d, a}, new Bundle[]{a, b, c, d, e});
	}

	// A -> B, C, D
	// B -> C
	// D -> B, E
	// E -> F, G
	// F -> G
	// H -> G
	// I -> H, J

	// depending on the order there are multiple shutdown orders
	public void testLargeTree() {
		DependencyMockBundle a = new DependencyMockBundle("a");
		DependencyMockBundle b = new DependencyMockBundle("b");
		DependencyMockBundle c = new DependencyMockBundle("c");
		DependencyMockBundle d = new DependencyMockBundle("d");
		DependencyMockBundle e = new DependencyMockBundle("e");
		DependencyMockBundle f = new DependencyMockBundle("f");
		DependencyMockBundle g = new DependencyMockBundle("g");
		DependencyMockBundle h = new DependencyMockBundle("h");
		DependencyMockBundle i = new DependencyMockBundle("i");
		DependencyMockBundle j = new DependencyMockBundle("j");

		a.setDependentOn(new Bundle[]{b, c, d});
		b.setDependentOn(c);
		d.setDependentOn(new Bundle[]{b, e});
		e.setDependentOn(new Bundle[]{f, g});
		f.setDependentOn(g);
		h.setDependentOn(g);
		i.setDependentOn(new Bundle[]{h, j});

		testDependencyTree(new Bundle[]{g, f, e, c, b, d, a, h, j, i}, new Bundle[]{a, b, c, d, e, f,
			g, h, i, j});
	}

	// A -> B,D
	// B -> C, E
	// C
	// D -> B, C
	// E -> C
	public void testComplexTree() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");

		a.setDependentOn(new Bundle[]{b, d});
		b.setDependentOn(new Bundle[]{c, e});
		d.setDependentOn(new Bundle[]{b, c});
		e.setDependentOn(new Bundle[]{c});

		testDependencyTreeWithShuffle(new Bundle[]{c, e, b, d, a}, new Bundle[]{a, b, c, d, e});
	}

	// Although this is an interesting test, the shutdown logic does not require that
	// it pass and the current algorithm does not handle this case.
	public void XtestMissingMiddle() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");
		DependencyMockBundle D = new DependencyMockBundle("D");
		DependencyMockBundle E = new DependencyMockBundle("E");

		// Sets dependency A -> B -> C -> D -> E
		B.setDependentOn(A);
		C.setDependentOn(new Bundle[]{B});
		D.setDependentOn(new Bundle[]{C});
		E.setDependentOn(new Bundle[]{D});

		testDependencyTree(new Bundle[]{A, C, E}, new Bundle[]{C, E, A});
	}

	// //////////
	// CIRCULAR TREES
	// /////////

	// A -> B (id = 1)
	// B -> A (id = 2)
	// B -> A has to be stopped first since it was created last (highest id)
	public void testSimpleCircularTreeTieOnServiceRankingUsingServiceId() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");

		b.setDependentOn(a, 0, 2);
		a.setDependentOn(b, 0, 1);

		Bundle[] expectedVer1 = new Bundle[]{a, b};
		Bundle[] expectedVer2 = new Bundle[]{b, a};

		// we should get the same order always (B should be stopped first)
		assertTrue(Arrays.equals(expectedVer2, sorter.computeServiceDependencyGraph(expectedVer1)));
		assertTrue(Arrays.equals(expectedVer2, sorter.computeServiceDependencyGraph(expectedVer2)));
	}

	public void testSimpleCircularTreeUsingServiceRanking() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");

		b.setDependentOn(a);
		a.setDependentOn(b);

		testDependencyTreeWithShuffle(new Bundle[]{a, b}, new Bundle[]{a, b});
	}

	// A -> B, C
	// B -> C, D
	// C -> D
	// D -> A
	public void testMediumCircularCycle() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");

		a.setDependentOn(new Bundle[]{b, c});
		b.setDependentOn(new Bundle[]{c, d});
		c.setDependentOn(d);
		d.setDependentOn(a);

		testDependencyTreeWithShuffle(new Bundle[]{d, c, b, a}, new Bundle[]{a, b, c, d});
	}

	// A -> B, D
	// B -> C, E
	// C -> D
	// D -> B, C
	// E -> C
	// E should be stopped first, since it is the last service started
	public void testComplexCyclicTree() {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");

		a.setDependentOn(new Bundle[]{b, d});
		b.setDependentOn(new Bundle[]{c, e});
		c.setDependentOn(d);
		d.setDependentOn(new Bundle[]{b, c});
		e.setDependentOn(c);

		testDependencyTreeWithShuffle(new Bundle[]{e, d, c, b, a}, new Bundle[]{a, b, c, d, e});
	}

	public void testCircularReferenceId() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");
		DependencyMockBundle D = new DependencyMockBundle("D");
		DependencyMockBundle E = new DependencyMockBundle("E");

		// Sets dependency A -> B -> C -> D -> E -> A
		// A has lowest id so gets shutdown last (started first).
		A.setDependentOn(new Bundle[]{E}, 0, 0);
		B.setDependentOn(new Bundle[]{A}, 0, 1);
		C.setDependentOn(new Bundle[]{B}, 0, 2);
		D.setDependentOn(new Bundle[]{C}, 0, 3);
		E.setDependentOn(new Bundle[]{D}, 0, 4);

		testDependencyTreeWithShuffle(new Bundle[]{E, D, C, B, A}, new Bundle[]{E, D, C, B, A});
	}

	public void testCircularReferenceIdMulti() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");

		// Sets dependency A -> C -> B -> A
		// A has lowest id so gets shutdown last (started first).
		// B should go first since its service was started last (id = 4)
		A.setDependentOn(new Bundle[]{C}, 0, 0);
		B.setDependentOn(new Bundle[]{A, A}, new int[]{0, 0}, new long[]{4, 1});
		C.setDependentOn(new Bundle[]{B}, 0, 2);

		testDependencyTreeWithShuffle(new Bundle[]{B, C, A}, new Bundle[]{C, B, A});
	}

	public void testCircularReferenceRankMulti() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");

		// Sets dependency A -> B -> C -> A
		// B has highest rank so gets shutdown last (its started first).
		// C should go second since its service has the second ranking (2)
		// which means A should go first
		A.setDependentOn(new Bundle[]{B}, 0, 0);
		B.setDependentOn(new Bundle[]{C, C}, new int[]{0, 3}, new long[]{0, 0});
		C.setDependentOn(new Bundle[]{A}, 2, 0);

		testDependencyTreeWithShuffle(new Bundle[]{A, C, B}, new Bundle[]{C, B, A});
	}

	public void testCircularReferenceReference() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");
		DependencyMockBundle D = new DependencyMockBundle("D");
		DependencyMockBundle E = new DependencyMockBundle("E");

		// Sets dependency A -> B -> C -> D -> E -> A
		// A has higher ranking so gets shutdown last

		// A -> E
		// B -> A
		// C -> B
		// D -> C
		// E -> D
		// E -> D -> C -> B -> A -> E
		A.setDependentOn(new Bundle[]{E}, 4, 0);
		B.setDependentOn(new Bundle[]{A}, 3, 1);
		C.setDependentOn(new Bundle[]{B}, 2, 2);
		D.setDependentOn(new Bundle[]{C}, 1, 3);
		E.setDependentOn(new Bundle[]{D}, 0, 4);

		testDependencyTreeWithShuffle(new Bundle[]{E, D, C, B, A}, new Bundle[]{E, D, C, B, A});
	}

	public void testForest() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");
		DependencyMockBundle D = new DependencyMockBundle("D");
		DependencyMockBundle E = new DependencyMockBundle("E");
		DependencyMockBundle F = new DependencyMockBundle("F");
		DependencyMockBundle G = new DependencyMockBundle("G");
		DependencyMockBundle H = new DependencyMockBundle("H");
		DependencyMockBundle I = new DependencyMockBundle("I");
		DependencyMockBundle J = new DependencyMockBundle("J");

		// Sets dependency A -> B -> C, B -> D -> E
		A.setDependentOn(new Bundle[]{B});
		B.setDependentOn(new Bundle[]{C, D});
		D.setDependentOn(new Bundle[]{E});

		// Sets dependency F -> G, F -> H
		F.setDependentOn(new Bundle[]{G, H});

		// Sets dependency I -> J
		I.setDependentOn(new Bundle[]{J});

		testDependencyTree(new Bundle[]{G, H, F, E, D, J, C, B, A, I}, new Bundle[]{F, D, J, B, E, A,
			H, I, G, C});
	}

	public void testInversedForest() throws Exception {
		DependencyMockBundle A = new DependencyMockBundle("A");
		DependencyMockBundle B = new DependencyMockBundle("B");
		DependencyMockBundle C = new DependencyMockBundle("C");
		DependencyMockBundle D = new DependencyMockBundle("D");
		DependencyMockBundle E = new DependencyMockBundle("E");
		DependencyMockBundle F = new DependencyMockBundle("F");
		DependencyMockBundle G = new DependencyMockBundle("G");
		DependencyMockBundle H = new DependencyMockBundle("H");
		DependencyMockBundle I = new DependencyMockBundle("I");
		DependencyMockBundle J = new DependencyMockBundle("J");

		// C -> B -> A, E -> D -> B
		B.setDependentOn(new Bundle[]{A});
		C.setDependentOn(new Bundle[]{B});
		D.setDependentOn(new Bundle[]{B});
		E.setDependentOn(new Bundle[]{D});

		// Sets dependency G -> F, H -> F
		G.setDependentOn(new Bundle[]{F});
		H.setDependentOn(new Bundle[]{F});

		// Sets dependency J -> I
		J.setDependentOn(new Bundle[]{I});

		testDependencyTree(new Bundle[]{F, A, B, D, I, J, E, H, G, C}, new Bundle[]{F, D, J, B, E, A,
			H, I, G, C});
	}

	/**
	 * Test the resulting tree after shuffling the input bundles several times.
	 *
	 * @param expected
	 * @param bundles
	 * @return
	 */
	protected void testDependencyTree(Bundle[] expected, Bundle[] bundles) {
		Bundle[] tree = sorter.computeServiceDependencyGraph(bundles);
		assertTrue("array [" + ObjectUtils.nullSafeToString(tree) + "] does not match ["
			+ ObjectUtils.nullSafeToString(expected) + "] for input [" + ObjectUtils.nullSafeToString(bundles)
			+ "]", Arrays.equals(expected, tree));

	}

	protected void testDependencyTreeWithShuffle(Bundle[] expected, Bundle[] bundles) {
		List input = new ArrayList(bundles.length);

		for (int i = 0; i < bundles.length; i++) {
			input.add(bundles[i]);
		}

		// shuffle based on the number of elements
		for (int i = 0; i < bundles.length; i++) {
			testDependencyTree(expected, (Bundle[]) input.toArray(new Bundle[bundles.length]));
			Collections.shuffle(input);
		}

		count += bundles.length;
	}

	public int countTestCases() {
		return count;
	}

}
