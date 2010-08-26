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

package org.springframework.osgi.samples.console.web;

import org.springframework.osgi.samples.console.service.BundleIdentifier;

/**
 * Command object for selecting the bundle under analysis, the bundle display
 * option and the bundle search pattern and choice.
 * 
 * @author Costin Leau
 * 
 */
public class SelectionCommand {

	private static final String DEFAULT_SEARCH_PATTERN = "**/*";
	private Long bundleId;
	private BundleIdentifier displayChoice = BundleIdentifier.NAME;
	private SearchSpace searchChoice = SearchSpace.BUNDLE;
	private String searchPattern = DEFAULT_SEARCH_PATTERN;


	public Long getBundleId() {
		return bundleId;
	}

	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

	public BundleIdentifier getDisplayChoice() {
		return displayChoice;
	}

	public void setDisplayChoice(BundleIdentifier selectedDisplayOption) {
		this.displayChoice = selectedDisplayOption;
	}

	public SearchSpace getSearchChoice() {
		return searchChoice;
	}

	public void setSearchChoice(SearchSpace searchChoice) {
		this.searchChoice = searchChoice;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	@Override
	public String toString() {
		return "[bundleId=" + bundleId + "|displayChoice=" + displayChoice.toString() + "|searchSpace="
				+ searchChoice.toString() + "]";
	}
}
