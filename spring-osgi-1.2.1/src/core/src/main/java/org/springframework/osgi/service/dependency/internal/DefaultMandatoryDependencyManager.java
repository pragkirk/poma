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

package org.springframework.osgi.service.dependency.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.ConcurrentMap;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterControllerUtils;
import org.springframework.osgi.service.exporter.support.internal.controller.ExporterInternalActions;
import org.springframework.osgi.service.importer.OsgiServiceDependency;
import org.springframework.osgi.service.importer.support.AbstractOsgiServiceImportFactoryBean;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.osgi.service.importer.support.internal.controller.ImporterControllerUtils;
import org.springframework.osgi.service.importer.support.internal.controller.ImporterInternalActions;
import org.springframework.osgi.service.importer.support.internal.dependency.ImporterStateListener;
import org.springframework.osgi.util.internal.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link MandatoryServiceDependencyManager} which determines the relationship between
 * importers and exporters and unpublishes exported service if they dependent, transitively, on imported OSGi services
 * that are mandatory and cannot be satisfied.
 * 
 * <strong>Note:</strong> aimed for singleton beans only
 * 
 * @author Costin Leau
 * 
 */
public class DefaultMandatoryDependencyManager implements MandatoryServiceDependencyManager, BeanFactoryAware,
		DisposableBean {

	/**
	 * Importer state listener that gets associated with each exporter.
	 * 
	 * @author Costin Leau
	 */
	private class ImporterDependencyListener implements ImporterStateListener {

		private final Object exporter;
		private final String exporterName;

		private ImporterDependencyListener(Object exporter) {
			this.exporter = exporter;
			this.exporterName = (String) exporterToName.get(exporter);

		}

		public void importerSatisfied(Object importer, OsgiServiceDependency dependency) {

			boolean trace = log.isTraceEnabled();
			boolean exporterRemoved = false;

			// update importer status
			synchronized (exporter) {
				Map importers = (Map) exporterToImporterDeps.get(exporter);
				exporterRemoved = !(importers != null);

				// if the list is not present (exporter was removed), bail out
				if (!exporterRemoved) {
					importers.put(importer, Boolean.TRUE);
					if (trace)
						log.trace("Importer [" + importerToName.get(importer)
								+ "] is satisfied; checking the rest of the dependencies for exporter "
								+ exporterToName.get(exporter));

					checkIfExporterShouldStart(exporter, importers);
				}
			}

			if (exporterRemoved && trace)
				log.trace("Exporter [" + exporterName + "] removed; ignoring dependency [" + dependency.getBeanName()
						+ "] update");
		}

		public void importerUnsatisfied(Object importer, OsgiServiceDependency dependency) {

			boolean exporterRemoved = false;

			synchronized (exporter) {
				Map importers = (Map) exporterToImporterDeps.get(exporter);
				exporterRemoved = !(importers != null);
				if (!exporterRemoved) {
					// record the importer status
					importers.put(importer, Boolean.FALSE);
				}
			}

			boolean trace = log.isTraceEnabled();

			if (!exporterRemoved) {
				if (trace)
					log.trace("Exporter [" + exporterName + "] stopped; transitive OSGi dependency ["
							+ dependency.getBeanName() + "] is unsatifised");

				// if the importer goes down, simply shut down the exporter
				stopExporter(exporter);
			} else {
				if (trace) {
					log.trace("Exporter [" + exporterName + "] removed; ignoring dependency ["
							+ dependency.getBeanName() + "] update");
				}
			}
		}
	}

	private static final Log log = LogFactory.getLog(DefaultMandatoryDependencyManager.class);

	/** cache map - useful for avoiding double registration */
	private final ConcurrentMap exportersSeen = CollectionFactory.createConcurrentMap(4);

	private static final Object VALUE = new Object();

	/**
	 * Importers on which an exporter depends. The exporter instance is used as a key, while the value is represented by
	 * a list of importers name and their status (up or down).
	 */
	private final Map exporterToImporterDeps = CollectionFactory.createConcurrentMap(8);

	/** exporter -> importer listener map */
	private final Map exporterListener = CollectionFactory.createConcurrentMap(8);

	/** importer -> name map */
	private final ConcurrentMap importerToName = CollectionFactory.createConcurrentMap(8);

	/** exporter name map */
	private final Map exporterToName = CollectionFactory.createConcurrentMap(8);

	/** owning bean factory */
	private ConfigurableListableBeanFactory beanFactory;

	public void addServiceExporter(Object exporter, String exporterBeanName) {
		Assert.hasText(exporterBeanName);

		if (exportersSeen.putIfAbsent(exporterBeanName, VALUE) == null) {

			String beanName = exporterBeanName;

			if (beanFactory.isFactoryBean(exporterBeanName))
				beanName = BeanFactory.FACTORY_BEAN_PREFIX + exporterBeanName;

			// check if it's factory bean (no need to check for abstract
			// definition since we're called by a BPP)
			if (!beanFactory.isSingleton(beanName)) {
				log.info("Exporter [" + beanName + "] is not singleton and will not be tracked");
			}

			else {
				exporterToName.put(exporter, exporterBeanName);
				// retrieve associated controller
				ExporterInternalActions controller = ExporterControllerUtils.getControllerFor(exporter);

				// disable publication at startup
				controller.registerServiceAtStartup(false);

				// populate the dependency maps
				discoverDependentImporterFor(exporterBeanName, exporter);
			}
		}
	}

	/**
	 * Discover all the importers for the given exporter. Since the importers are already created before the exporter
	 * instance is created, this method only does filtering based on the mandatory imports.
	 */
	protected void discoverDependentImporterFor(String exporterBeanName, Object exporter) {

		boolean trace = log.isTraceEnabled();

		// determine exporters
		String[] importerA = BeanFactoryUtils.getTransitiveDependenciesForBean(beanFactory, exporterBeanName, true,
				OsgiServiceProxyFactoryBean.class);

		String[] importerB = BeanFactoryUtils.getTransitiveDependenciesForBean(beanFactory, exporterBeanName, true,
				OsgiServiceCollectionProxyFactoryBean.class);

		String[] importerNames = StringUtils.concatenateStringArrays(importerA, importerB);

		// create map of associated importers
		Map dependingImporters = new LinkedHashMap(importerNames.length);

		if (trace)
			log.trace("Exporter [" + exporterBeanName + "] depends (transitively) on the following importers:"
					+ ObjectUtils.nullSafeToString(importerNames));

		// first create a listener for the exporter
		ImporterStateListener listener = new ImporterDependencyListener(exporter);
		exporterListener.put(exporter, listener);

		// exclude non-singletons and non-mandatory importers
		for (int i = 0; i < importerNames.length; i++) {
			if (beanFactory.isSingleton(importerNames[i])) {
				Object importer = beanFactory.getBean(importerNames[i]);

				// create an importer -> exporter association
				if (isMandatory(importer)) {
					dependingImporters.put(importer, importerNames[i]);
					importerToName.putIfAbsent(importer, importerNames[i]);
				}

				else if (trace)
					log.trace("Importer [" + importerNames[i] + "] is optional; skipping it");
			} else if (trace)
				log.trace("Importer [" + importerNames[i] + "] is a non-singleton; ignoring it");
		}

		if (trace)
			log.trace("After filtering, exporter [" + exporterBeanName + "] depends on importers:"
					+ dependingImporters.values());

		Collection filteredImporters = dependingImporters.keySet();

		// add the importers and their status to the collection
		synchronized (exporter) {
			Map importerStatuses = new LinkedHashMap(filteredImporters.size());

			for (Iterator iter = filteredImporters.iterator(); iter.hasNext();) {
				Object importer = iter.next();
				importerStatuses.put(importer, Boolean.valueOf(isSatisfied(importer)));
				// add the listener after the importer status has been recorded
				addListener(importer, listener);
			}
			exporterToImporterDeps.put(exporter, importerStatuses);
			checkIfExporterShouldStart(exporter, importerStatuses);
		}
	}

	private void checkIfExporterShouldStart(Object exporter, Map importers) {

		if (!importers.containsValue(Boolean.FALSE)) {
			startExporter(exporter);

			if (log.isDebugEnabled())
				log
						.trace("Exporter [" + exporterToName.get(exporter)
								+ "] started; all its dependencies are satisfied");
		} else {
			List unsatisfiedDependencies = new ArrayList(importers.size());

			for (Iterator iterator = importers.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				if (Boolean.FALSE.equals(entry.getValue()))
					unsatisfiedDependencies.add(importerToName.get(entry.getKey()));
			}

			if (log.isTraceEnabled()) {
				log.trace("Exporter [" + exporterToName.get(exporter)
						+ "] not started; there are still has unsatisfied dependencies " + unsatisfiedDependencies);
			}
		}
	}

	public void removeServiceExporter(Object bean, String beanName) {
		if (log.isTraceEnabled()) {
			log.trace("Removing exporter [" + beanName + "]");
		}

		// remove the exporter and its listeners from the map
		ImporterStateListener stateListener = (ImporterStateListener) exporterListener.remove(bean);

		Map importers;

		synchronized (bean) {
			importers = (Map) exporterToImporterDeps.remove(bean);
		}

		// no need to do synchronization anymore since no other threads will find the collection
		if (importers != null)
			for (Iterator iterator = importers.keySet().iterator(); iterator.hasNext();) {
				Object importer = iterator.next();
				// get associated controller
				removeListener(importer, stateListener);
			}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	public void destroy() {
		exportersSeen.clear();
		exporterListener.clear();
		exporterToImporterDeps.clear();
		exporterToName.clear();
		importerToName.clear();
	}

	private void startExporter(Object exporter) {
		ExporterControllerUtils.getControllerFor(exporter).registerService();
	}

	private void stopExporter(Object exporter) {
		ExporterControllerUtils.getControllerFor(exporter).unregisterService();
	}

	private void addListener(Object importer, ImporterStateListener stateListener) {
		ImporterInternalActions controller = ImporterControllerUtils.getControllerFor(importer);
		controller.addStateListener(stateListener);
	}

	private void removeListener(Object importer, ImporterStateListener stateListener) {
		ImporterInternalActions controller = ImporterControllerUtils.getControllerFor(importer);
		controller.removeStateListener(stateListener);
	}

	private boolean isSatisfied(Object importer) {
		return ImporterControllerUtils.getControllerFor(importer).isSatisfied();
	}

	private boolean isMandatory(Object importer) {
		if (importer instanceof AbstractOsgiServiceImportFactoryBean) {
			AbstractOsgiServiceImportFactoryBean imp = (AbstractOsgiServiceImportFactoryBean) importer;
			return imp.getCardinality().isMandatory();
		}

		return false;
	}
}