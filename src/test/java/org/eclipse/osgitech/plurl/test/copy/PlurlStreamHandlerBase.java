/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgitech.plurl.test.copy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public abstract class PlurlStreamHandlerBase extends URLStreamHandler implements PlurlStreamHandler {
	private volatile PlurlSetter plurlSetter;

	@Override
	public abstract URLConnection openConnection(URL u) throws IOException;

	@Override
	public void parseURL(PlurlSetter setter, URL u, String spec, int start, int limit) {
		this.plurlSetter = setter;
		parseURL(u, spec, start, limit);
	}

	@Override
	public URLConnection openConnection(URL u, Proxy p) throws IOException {
		return super.openConnection(u, p);
	}

	@Override
	public String toExternalForm(URL u) {
		return super.toExternalForm(u);
	}

	@Override
	public boolean equals(URL u1, URL u2) {
		return super.equals(u1, u2);
	}

	@Override
	public int getDefaultPort() {
		return super.getDefaultPort();
	}

	@Override
	public InetAddress getHostAddress(URL u) {
		return super.getHostAddress(u);
	}

	@Override
	public int hashCode(URL u) {
		return super.hashCode(u);
	}

	@Override
	public boolean hostsEqual(URL u1, URL u2) {
		return super.hostsEqual(u1, u2);
	}

	@Override
	public boolean sameFile(URL u1, URL u2) {
		return super.sameFile(u1, u2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setURL(URL u, String proto, String host, int port, String file, String ref) {
		PlurlSetter current = plurlSetter;
		if (current == null) {
			// something is calling the handler directly, probably passed it to URL directly
			super.setURL(u, proto, host, port, null, null, file, null, ref);
		} else {
			current.setURL(u, proto, host, port, null, null, file, null, ref);
		}
	}

	@Override
	public void setURL(URL u, String proto, String host, int port, String auth, String user, String path,
			String query, String ref) {
		PlurlSetter current = plurlSetter;
		if (current == null) {
			// something is calling the handler directly, probably passed it to URL directly
			super.setURL(u, proto, host, port, auth, user, path, query, ref);
		} else {
			current.setURL(u, proto, host, port, auth, user, path, query, ref);
		}
	}

}
