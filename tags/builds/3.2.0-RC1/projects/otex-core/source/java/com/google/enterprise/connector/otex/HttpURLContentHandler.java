// Copyright (C) 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.otex;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.otex.client.Client;
import com.google.enterprise.connector.otex.client.ClientValue;

/**
 * This content handler implementation uses a Livelink download URL.
 * It depends on the LAPI client, so it can't be used in deployments
 * with the mock client.
 *
 * @see ContentHandler
 */
class HttpURLContentHandler implements ContentHandler {
    /** The logger for this class. */
    private static final Logger LOGGER =
        Logger.getLogger(HttpURLContentHandler.class.getName());

    @VisibleForTesting
    static final String DEFAULT_URL_PATH =
        "?func=ll&objAction=download&objId=";

    /** The connector contains configuration information. */
    private LivelinkConnector connector;

    /** The client provides access to the server. */
    private Client client;

    /** The user's LLCookie value. */
    private String llCookie;

    /** The Livelink base URL. The default is the configured Livelink URL. */
    String urlBase;

    /** The document URL path. */
    String urlPath = DEFAULT_URL_PATH;

    HttpURLContentHandler() {
    }

    /** {@inheritDoc} */
    public void initialize(LivelinkConnector connector, Client client)
            throws RepositoryException {
        this.connector = connector;
        this.client = client;

        llCookie = getLLCookie();

        // Use the default value if no custom value was set.
        if (urlBase == null) {
          urlBase = connector.getDisplayUrl();
        }
    }

    /**
     * Sets the document base URL. By default the configured Livelink
     * URL is used.
     *
     * @param urlBase the Livelink base URL for document retrieval
     * @since 3.0
     */
    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    @VisibleForTesting
    String getUrlBase() {
        return urlBase;
    }

    /**
     * Sets the document URL path, which is appended to the base URL.
     * By default the URL path used is
     * "?func=ll&amp;objAction=download&amp;objId=".
     *
     * @param urlPath the URL path for document retrieval
     * @since 3.0
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    @VisibleForTesting
    String getUrlPath() {
        return urlPath;
    }

    /**
     * Gets the LLCookie value for this session.
     *
     * @return the cookie value
     * @throws RepositoryException if the cookie is not set
     */
    private String getLLCookie() throws RepositoryException {
        ClientValue cookies = client.GetCookieInfo();
        for (int i = 0; i < cookies.size(); i++) {
            ClientValue cookie = cookies.toValue(i);
            if ("LLCookie".equalsIgnoreCase(cookie.toString("Name"))) {
                return cookie.toString("Value");
            }
        }
        throw new RepositoryException("Missing LLCookie");
    }

    /** {@inheritDoc} */
    public InputStream getInputStream(int volumeId, int objectId,
            int versionNumber, int size) throws RepositoryException {
        try {
            URL downloadUrl = new URL(urlBase + urlPath + objectId);
            if (LOGGER.isLoggable(Level.FINEST))
                LOGGER.fine("DOWNLOAD URL: " + downloadUrl);
            URLConnection download = downloadUrl.openConnection();
            download.addRequestProperty("Cookie", "LLCookie=" + llCookie);

            // XXX: Does the BufferedInputStream help at all?
            return new BufferedInputStream(download.getInputStream(), 32768);
        } catch (IOException e) {
            throw new LivelinkException(e, LOGGER);
        }
    }
}
