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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.osgi.samples.console.service.BundleIdentifier;
import org.springframework.osgi.samples.console.service.OsgiConsole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OSGi console controller. The application main entry point, this class handles
 * the HTTP resources and provides the logic behind the console web page.
 * 
 * @author Costin Leau
 */
@Controller
public class ConsoleController {

	private final OsgiConsole console;
	private final Validator searchPatternValidator;


	/**
	 * Constructs a new <code>ConsoleController</code> instance.
	 * 
	 * @param console console service
	 */
	@Autowired
	public ConsoleController(OsgiConsole console) {
		this.console = console;
		this.searchPatternValidator = new SearchPatternValidator();
	}

	/**
	 * Custom handler for the welcome view.
	 * 
	 * @param selectionCommand
	 * @param model
	 */
	@RequestMapping("/console.do")
	public void consoleHandler(@ModelAttribute("selection")
	SelectionCommand selectionCommand, BindingResult bindingResult, Model model) {
		// apply default for selected bundle (if needed)
		if (selectionCommand.getBundleId() == null) {
			selectionCommand.setBundleId(console.getDefaultBundleId());
		}
		BundleIdentifier displayChoice = selectionCommand.getDisplayChoice();
		Bundle bundle = console.getBundle(selectionCommand.getBundleId());

		model.addAttribute("bundles", listBundles(displayChoice));
		model.addAttribute("bundleInfo", createBundleInfo(bundle, displayChoice));

		searchPatternValidator.validate(selectionCommand.getSearchPattern(), bindingResult);

		if (bindingResult.hasErrors()) {
			return;
		}

		model.addAttribute("searchResult", search(bundle, selectionCommand.getSearchChoice(),
			selectionCommand.getSearchPattern()));
	}

	/**
	 * Returns a map containing the list of bundles installed in the platform.
	 * Additionally, the method considers how the bundles should be displayed.
	 * 
	 * @param model model associated with the view
	 * @return "bundles" attribute
	 */
	public Map<Long, String> listBundles(BundleIdentifier displayChoice) {
		Bundle[] bundles = console.listBundles();
		Map<Long, String> map = new LinkedHashMap<Long, String>(bundles.length);
		for (Bundle bundle : bundles) {
			map.put(bundle.getBundleId(), displayChoice.toString(bundle));
		}
		return map;
	}

	/**
	 * Returns an map of installed bundles.
	 * 
	 * @return installed bundles map
	 */
	@ModelAttribute("displayOptions")
	public Map<BundleIdentifier, String> listingOptions() {
		return BundleIdentifier.toStringMap();
	}

	@ModelAttribute("searchChoices")
	public Map<SearchSpace, String> searchOptions() {
		return SearchSpace.toStringMap();
	}

	private BundleInfo createBundleInfo(Bundle bundle, BundleIdentifier displayChoice) {
		BundleInfo info = new BundleInfo(bundle);
		addWiring(info);
		addServices(info, displayChoice);
		return info;
	}

	private void addWiring(BundleInfo info) {
		info.addExportedPackages(console.getExportedPackages(info.getBundle()));
		info.addImportedPackages(console.getImportedPackages(info.getBundle()));
	}

	private void addServices(BundleInfo info, BundleIdentifier displayChoice) {
		for (ServiceReference registeredReference : console.getRegisteredServices(info.getBundle())) {
			info.addRegisteredServices(new BundleInfo.OsgiService(registeredReference, displayChoice));
		}

		for (ServiceReference importedRef : console.getServicesInUse(info.getBundle())) {
			info.addServiceInUse(new BundleInfo.OsgiService(importedRef, displayChoice));
		}
	}

	/**
	 * Searches the bundle for the given user pattern. Additionally, this method
	 * handles the search space.
	 * 
	 * @param bundle OSGi bundle
	 * @param searchChoice bundle search space
	 * @param userPattern search pattern
	 * 
	 * @return collection of matching paths
	 */
	private Collection<String> search(Bundle bundle, SearchSpace searchChoice, String userPattern) {
		if (!StringUtils.hasText(userPattern)) {
			return Collections.emptyList();
		}

		// strip any prefix specified by the user
		return console.search(bundle, searchChoice.resourcePrefix() + userPattern);
	}
}
