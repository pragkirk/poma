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
package org.springframework.osgi.extender.internal.dependencies.shutdown;

import java.util.Arrays;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Comparator based dependency sorter.
 *
 * @author Costin Leau
 * @author Andy Piper
 */
public class ComparatorServiceDependencySorter implements ServiceDependencySorter {

	public Bundle[] computeServiceDependencyGraph(Bundle[] bundles) {
		TarganStronglyConnectedSorter parser = new TarganStronglyConnectedSorter(bundles);
		return parser.computeServiceDependencyGraph();
	}

	/**
	 * Strongly Connected Component graph algorithm due to R. E. Targan, 1972. The implementation
	 * is adapted from "Algorithms in C" by Robert Sedgewick. In particular we make use of the property that
	 * non-connected components are traversed depth-first and so provide us with a regular dependency graph.
	 * Strongly connected components (a.k.a cycles) are gathered in a batch which gives us an opportunity to do
	 * futher sorting before output.
	 */
	public static class TarganStronglyConnectedSorter {
		private BundleDependencyComparator comparator = new BundleDependencyComparator();

		// Note that variable names reflect those in Sedgewick for easy comparison.
		private int id = 0;
		private int[] val; // visited list
		private int[] stack;
		private int p = 0; // current stack position
		private Node[] adj; // adjacency list of edges
		private int V; // the number of Vertices
		private Bundle[] bundles; // the output bundle list
		private Bundle[] sourcebundles; // the input bundle list
		private int index = 0; // current position in output list

		private static class Node {
			private int v;
			private Node next;

			public Node(int v, Node next) {
				this.v = v;
				this.next = next;
			}

			public int v() {
				return v;
			}

			public Node next() {
				return next;
			}
		}

		public TarganStronglyConnectedSorter(Bundle[] bundles) {
			this.V = bundles.length;
			this.bundles = new Bundle[V];
			this.sourcebundles = bundles;
			val = new int[V + 1];
			adj = new Node[V + 1];
			stack = new int[V + 1];
		}

		public Bundle[] computeServiceDependencyGraph() {
			// Zero adjacency matrix
			for (int k = 1; k <= V; k++) {
				val[k] = 0;
				adj[k] = null;
			}
			// Build adjacency matrix, x -> y
			// This probably could be more efficient
			for (int y = 1; y <= V; y++) {
				for (int x = 1; x <= V; x++) {
					if (references(sourcebundles[x - 1], sourcebundles[y - 1])) {
						adj[y] = new Node(x, adj[y]);
					}
				}
			}
			// Modified depth-first search of the nodes
			for (int k = 1; k <= V; k++) {
				if (val[k] == 0) visit(k);
			}
			return bundles;
		}

		private int visit(int k) {
			int m, min;
			val[k] = ++id;
			min = id;
			stack[p++] = k;
			for (Node t = adj[k]; t != null; t = t.next()) {
				m = (val[t.v()] == 0) ? visit(t.v()) : val[t.v()];
				if (m < min) min = m;
			}
			if (min == val[k]) {
				int subset = index;
				while (stack[p] != k) {
					int visited = stack[--p];
					bundles[index++] = sourcebundles[visited - 1];
					val[visited] = V + 1;
				}
				// Strongly connected set, so sort via ranking
				if (index > subset) {
					Arrays.sort(bundles, subset, index, comparator);
				}
			}
			return min;
		}

		/**
		 * Answer whether Bundle b is directly referenced by Bundle a
		 */
		protected static boolean references(Bundle a, Bundle b) {
			ServiceReference[] services = b.getRegisteredServices();
			if (services == null) {
				return false;
			}
			for (int i = 0; i < services.length; i++) {
				// filter on spring managed services
				if (BundleDependencyComparator.isSpringManagedService(services[i])) {
					Bundle[] referingBundles = services[i].getUsingBundles();
					if (referingBundles != null) {
						for (int j = 0; j < referingBundles.length; j++) {
							if (a.equals(referingBundles[j])) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}
}
