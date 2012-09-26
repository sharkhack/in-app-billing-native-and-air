/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class Extension implements FREExtension {

	public static FREContext context;
	
	/**
	 * Create the context (AS to Java).
	 */
	public FREContext createContext(String extId) {
		return context = new ExtensionContext();
	}

	/**
	 * Dispose the context.
	 */
	public void dispose() {
		context = null;
	}
	
	/**
	 * Initialize the context.
	 * Doesn't do anything for now.
	 */
	public void initialize() {
	}
}
