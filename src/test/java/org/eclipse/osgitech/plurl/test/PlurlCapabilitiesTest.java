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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.eclipse.osgitech.plurl.Plurl;
import org.eclipse.osgitech.plurl.impl.PlurlImpl;
import org.junit.Test;

/**
 * A factory that can only be identified by the URL cannot be routed to correctly by
 * a plurl that does not consult {@link
 * org.eclipse.osgitech.plurl.PlurlFactory#shouldHandle(String, String)}. Since
 * copies of different versions can be in use in the same JVM, a factory needs to be
 * able to ask what the installed implementation supports rather than assume.
 */
@SuppressWarnings("nls")
public class PlurlCapabilitiesTest {
	/**
	 * An implementation that predates an operation rejects it, and that rejection is
	 * exactly how a factory tells an older plurl apart from one reporting no
	 * capabilities. Asserted here for an unknown operation, since an older
	 * implementation cannot be installed from this test.
	 */
	@Test
	public void unknownOperationIsRejected() throws IOException {
		Plurl plurl = new PlurlImpl();
		plurl.install(Plurl.PLURL_FORBID_NOTHING);
		try {
			new URL("plurl://op/noSuchOperation").openConnection().getContent();
			fail("an unknown operation must be rejected");
		} catch (IOException e) {
			// expected: capabilities() turns this into an empty set
		} finally {
			plurl.uninstall();
		}
	}

	@Test
	public void capabilitiesAreReported() throws IOException {
		Plurl plurl = new PlurlImpl();
		plurl.install(Plurl.PLURL_FORBID_NOTHING);
		try {
			Set<String> capabilities = Plurl.capabilities();
			assertTrue("selection by spec is supported: " + capabilities,
					capabilities.contains(Plurl.PLURL_CAPABILITY_SELECT_BY_SPEC));
			assertFalse("an unknown capability is not claimed",
					capabilities.contains("somethingElseEntirely"));
		} finally {
			plurl.uninstall();
		}
	}

	/**
	 * The operation must also be reachable through the protocol form the javadoc
	 * documents, which parses with a leading slash in the path.
	 */
	@Test
	public void capabilitiesReachableThroughTheProtocol() throws IOException {
		Plurl plurl = new PlurlImpl();
		plurl.install(Plurl.PLURL_FORBID_NOTHING);
		try {
			Object content = new URL("plurl://op/" + Plurl.PLURL_CAPABILITIES).openConnection().getContent();
			assertEquals(Plurl.capabilities(), content);
		} finally {
			plurl.uninstall();
		}
	}
}
