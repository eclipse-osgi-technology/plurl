/*
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.osgitech.plurl.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.eclipse.osgitech.plurl.Plurl;
import org.eclipse.osgitech.plurl.PlurlStreamHandlerBase;
import org.eclipse.osgitech.plurl.PlurlStreamHandlerFactory;
import org.eclipse.osgitech.plurl.impl.PlurlImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Several parties may share one protocol and be distinguishable only by the URL
 * itself, for example multiple instances of the same framework where the owner is
 * identified by an id in the URL host. Such a URL can also be used by a caller that
 * no factory recognises from the call stack, which leaves nothing to select on.
 *
 * @see org.eclipse.osgitech.plurl.PlurlFactory#shouldHandle(String, String)
 */
@SuppressWarnings("nls")
public class PlurlURLSelectionTest {
	static final String PROTOCOL = "plurlowner";

	/**
	 * Claims only the URLs whose host names it, and never claims by call stack, so
	 * selection can only succeed through the URL being parsed.
	 */
	static class OwnerFactory implements PlurlStreamHandlerFactory {
		final String owner;

		OwnerFactory(String owner) {
			this.owner = owner;
		}

		@Override
		public boolean shouldHandle(Class<?> clazz) {
			return false;
		}

		@Override
		public boolean shouldHandle(String protocol, String spec) {
			return PROTOCOL.equals(protocol) && owner.equals(hostOf(spec));
		}

		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			return PROTOCOL.equals(protocol) ? new OwnerHandler(owner) : null;
		}
	}

	/**
	 * The owner is in the host of the spec, which has to be read out of the string
	 * because no URL exists to ask yet.
	 */
	static String hostOf(String spec) {
		int start = spec.indexOf("//");
		if (start < 0) {
			return null;
		}
		start += 2;
		int end = start;
		while (end < spec.length() && "/?#".indexOf(spec.charAt(end)) < 0) {
			end++;
		}
		return spec.substring(start, end);
	}

	static class OwnerHandler extends PlurlStreamHandlerBase {
		final String owner;

		OwnerHandler(String owner) {
			this.owner = owner;
		}

		@Override
		public URLConnection openConnection(URL u) throws IOException {
			return new URLConnection(u) {
				@Override
				public void connect() throws IOException {
					// nothing to do
				}

				@Override
				public Object getContent() throws IOException {
					return owner;
				}
			};
		}
	}

	private Plurl plurl;
	private OwnerFactory first;
	private OwnerFactory second;

	@Before
	public void installPlurl() throws IOException {
		plurl = new PlurlImpl();
		plurl.install(Plurl.PLURL_FORBID_NOTHING);
		// Two factories, so plurl cannot take the single factory shortcut and has to
		// select between them.
		first = new OwnerFactory("first");
		second = new OwnerFactory("second");
		Plurl.add(first);
		Plurl.add(second);
	}

	@After
	public void uninstallPlurl() throws IOException {
		Plurl.remove(first);
		Plurl.remove(second);
		plurl.uninstall();
	}

	/**
	 * Each URL must be served by the factory that claims it, not by whichever factory
	 * was added first.
	 */
	@Test
	public void urlIsServedByItsOwner() throws IOException {
		assertEquals("second", new URL(PROTOCOL + "://second/resource").getContent());
		assertEquals("first", new URL(PROTOCOL + "://first/resource").getContent());
	}

	/**
	 * The same must hold for a URL rebuilt from its external form, which is the case
	 * that has nothing else to select on: nothing in the call stack belongs to either
	 * factory.
	 */
	@Test
	public void reparsedUrlIsServedByItsOwner() throws IOException {
		URL url = new URL(PROTOCOL + "://second/resource");
		assertEquals("second", new URL(url.toExternalForm()).getContent());
	}
}
