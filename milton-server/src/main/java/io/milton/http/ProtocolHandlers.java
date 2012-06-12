/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http;

import io.milton.http.acl.ACLProtocol;
import io.milton.http.caldav.CalDavProtocol;
import io.milton.http.carddav.CardDavProtocol;
import io.milton.http.http11.Http11Protocol;
import io.milton.http.quota.DefaultStorageChecker;
import io.milton.http.quota.StorageChecker;
import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.WebDavProtocol;
import io.milton.http.webdav.WebDavResponseHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProtocolHandlers implements Iterable<HttpExtension> {

	private final List<HttpExtension> handlers;
	private final HandlerHelper handlerHelper;

	public ProtocolHandlers(List<HttpExtension> handlers) {
		this.handlers = handlers;
		this.handlerHelper = null;
	}

	public ProtocolHandlers(WebDavResponseHandler responseHandler, HandlerHelper handlerHelper, ResourceFactory resourceFactory) {
		this.handlerHelper = handlerHelper;
		this.handlers = new ArrayList<HttpExtension>();
		this.handlers.add(new Http11Protocol(responseHandler, handlerHelper));
		WebDavProtocol webDavProtocol = new WebDavProtocol(responseHandler, handlerHelper);
		this.handlers.add(webDavProtocol);
		CalDavProtocol calDavProtocol = new CalDavProtocol(resourceFactory, responseHandler, handlerHelper, webDavProtocol);
		this.handlers.add(calDavProtocol);
		ACLProtocol acl = new ACLProtocol(webDavProtocol);
		this.handlers.add(acl);
		CardDavProtocol cardDavProtocol = new CardDavProtocol(resourceFactory, responseHandler, handlerHelper, webDavProtocol);
		this.handlers.add(cardDavProtocol);
	}


	@Override
	public Iterator<HttpExtension> iterator() {
		return handlers.iterator();
	}

	public boolean isEnableExpectContinue() {
		if (handlerHelper == null) {
			throw new RuntimeException("handlerHelper is not set. Read the appropriate property directly on injected HttpExtension implementations");
		}
		return handlerHelper.isEnableExpectContinue();
	}

	public void setEnableExpectContinue(boolean enableExpectContinue) {
		if (handlerHelper == null) {
			throw new RuntimeException("handlerHelper is not set. Set the appropriate property directly on injected HttpExtension implementations");
		}
		handlerHelper.setEnableExpectContinue(enableExpectContinue);
	}
}