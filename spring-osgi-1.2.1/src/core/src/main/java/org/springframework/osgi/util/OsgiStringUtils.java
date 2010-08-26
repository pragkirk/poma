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

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.springframework.core.ConstantException;
import org.springframework.core.Constants;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Utility class for creating nice string representations of various OSGi
 * classes.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiStringUtils {

	/** Constant over the Bundle events */
	public static final Constants BUNDLE_EVENTS = new Constants(BundleEvent.class);
	/** Constant over the Framework events */
	public static final Constants FRAMEWORK_EVENTS = new Constants(FrameworkEvent.class);
	/** Constant over the Service events */
	public static final Constants SERVICE_EVENTS = new Constants(ServiceEvent.class);
	/** Constant over the Bundle states */
	public static final Constants BUNDLE_STATES = new Constants(Bundle.class);

	private static final String UNKNOWN_EVENT_TYPE = "UNKNOWN EVENT TYPE";
	private static final String NULL_STRING = "null";
	private static final String EMPTY_STRING = "";


	/**
	 * Returns a String representation for the given bundle event.
	 * 
	 * @param eventType OSGi <code>BundleEvent</code> given as an int
	 * @return String representation for the bundle event
	 */
	public static String nullSafeBundleEventToString(int eventType) {
		try {
			return BUNDLE_EVENTS.toCode(new Integer(eventType), "");
		}
		catch (ConstantException cex) {
			return UNKNOWN_EVENT_TYPE;
		}

	}

	/**
	 * Returns a String representation for the given bundle event.
	 * 
	 * @param event OSGi <code>BundleEvent</code> (can be <code>null</code>)
	 * @return String representation for the given bundle event
	 */
	public static String nullSafeToString(BundleEvent event) {
		if (event == null)
			return NULL_STRING;
		try {
			return BUNDLE_EVENTS.toCode(new Integer(event.getType()), EMPTY_STRING);
		}
		catch (ConstantException cex) {
			return UNKNOWN_EVENT_TYPE;
		}
	}

	/**
	 * Returns a String representation for the given <code>ServiceEvent</code>.
	 * 
	 * @param event OSGi <code>ServiceEvent</code> (can be <code>null</code>)
	 * @return String representation for the given event
	 */
	public static String nullSafeToString(ServiceEvent event) {
		if (event == null)
			return NULL_STRING;
		try {
			return SERVICE_EVENTS.toCode(new Integer(event.getType()), EMPTY_STRING);
		}
		catch (ConstantException cex) {
			return UNKNOWN_EVENT_TYPE;
		}

	}

	/**
	 * Returns a String representation for the given <code>FrameworkEvent</code>.
	 * 
	 * @param event OSGi <code>FrameworkEvent</code> (can be <code>null</code>)
	 * @return String representation of the given event
	 */
	public static String nullSafeToString(FrameworkEvent event) {
		if (event == null)
			return NULL_STRING;
		try {
			return FRAMEWORK_EVENTS.toCode(new Integer(event.getType()), EMPTY_STRING);
		}
		catch (ConstantException cex) {
			return UNKNOWN_EVENT_TYPE;
		}
	}

	/**
	 * Returns a String representation of the given
	 * <code>ServiceReference</code>.
	 * 
	 * @param reference OSGi service reference (can be <code>null</code>)
	 * @return String representation of the given service reference
	 */
	public static String nullSafeToString(ServiceReference reference) {
		if (reference == null)
			return NULL_STRING;

		StringBuffer buf = new StringBuffer();
		Bundle owningBundle = reference.getBundle();

		buf.append("ServiceReference [").append(OsgiStringUtils.nullSafeSymbolicName(owningBundle)).append("] ");
		String clazzes[] = (String[]) reference.getProperty(org.osgi.framework.Constants.OBJECTCLASS);

		buf.append(ObjectUtils.nullSafeToString(clazzes));
		buf.append("={");

		String[] keys = reference.getPropertyKeys();

		for (int i = 0; i < keys.length; i++) {
			if (!org.osgi.framework.Constants.OBJECTCLASS.equals(keys[i])) {
				buf.append(keys[i]).append('=').append(reference.getProperty(keys[i]));
				if (i < keys.length - 1) {
					buf.append(',');
				}
			}
		}

		buf.append('}');

		return buf.toString();
	}

	/**
	 * Returns a String representation of the <code>Bundle</code> state.
	 * 
	 * @param bundle OSGi bundle (can be <code>null</code>)
	 * @return bundle state as a string
	 */
	public static String bundleStateAsString(Bundle bundle) {
		Assert.notNull(bundle, "bundle is required");
		int state = bundle.getState();

		try {
			return BUNDLE_STATES.toCode(new Integer(state), "");
		}
		catch (ConstantException cex) {
			return "UNKNOWN STATE";
		}
	}

	/**
	 * Returns the given <code>Bundle</code> symbolic name.
	 * 
	 * @param bundle OSGi bundle (can be <code>null</code>)
	 * @return the bundle, symbolic name
	 */
	public static String nullSafeSymbolicName(Bundle bundle) {
		if (bundle == null)
			return NULL_STRING;

		Dictionary headers = bundle.getHeaders();

		if (headers == null)
			return NULL_STRING;

		return (String) (bundle.getSymbolicName() == null ? NULL_STRING : bundle.getSymbolicName());
	}

	/**
	 * Returns the name of the given <code>Bundle</code> in a null-safe
	 * manner.
	 * 
	 * @param bundle OSGi bundle (can be <code>null</code>)
	 * @return bundle name
	 */
	public static String nullSafeName(Bundle bundle) {
		if (bundle == null)
			return NULL_STRING;

		Dictionary headers = bundle.getHeaders();

		if (headers == null)
			return NULL_STRING;

		String name = (String) headers.get(org.osgi.framework.Constants.BUNDLE_NAME);

		return (name == null ? NULL_STRING : name);

	}

	/**
	 * Returns the bundle name and symbolic name - useful when logging bundle
	 * info.
	 * 
	 * @param bundle OSGi bundle (can be null)
	 * @return the bundle name and symbolic name
	 */
	public static String nullSafeNameAndSymName(Bundle bundle) {
		if (bundle == null)
			return NULL_STRING;

		Dictionary dict = bundle.getHeaders();

		if (dict == null)
			return NULL_STRING;

		StringBuffer buf = new StringBuffer();
		String name = (String) dict.get(org.osgi.framework.Constants.BUNDLE_NAME);
		if (name == null)
			buf.append(NULL_STRING);
		else
			buf.append(name);
		buf.append(" (");
		String sname = (String) dict.get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);

		if (sname == null)
			buf.append(NULL_STRING);
		else
			buf.append(sname);

		buf.append(")");

		return buf.toString();
	}

}
