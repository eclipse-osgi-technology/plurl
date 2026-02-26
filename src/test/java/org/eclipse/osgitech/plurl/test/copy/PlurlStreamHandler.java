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


public interface PlurlStreamHandler {

	public interface PlurlSetter {

		public void setURL(URL u, String protocol, String host, int port, String authority, String userInfo,
				String path, String query, String ref);
	}

	public boolean equals(URL u1, URL u2);

	public int hashCode(URL u);

	public boolean hostsEqual(URL u1, URL u2);

	public int getDefaultPort();

	public InetAddress getHostAddress(URL u);

	public URLConnection openConnection(URL u) throws IOException;

	public URLConnection openConnection(URL u, Proxy p) throws IOException;

	public boolean sameFile(URL u1, URL u2);

	public String toExternalForm(URL u);

	public void parseURL(PlurlSetter plurlSetter, URL u, String spec, int start, int limit);

	public void setURL(URL u, String proto, String host, int port, String file, String ref);

	public void setURL(URL u, String proto, String host, int port, String auth, String user, String path,
			String query, String ref);
}
