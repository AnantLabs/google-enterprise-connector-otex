// Copyright (C) 2007-2008 Google Inc.
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

package com.google.enterprise.connector.otex.client.lapi;

import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.otex.client.Client;
import com.google.enterprise.connector.otex.client.ClientValue;
import com.google.enterprise.connector.otex.client.ClientValueFactory;

import com.opentext.api.LAPI_ATTRIBUTES;
import com.opentext.api.LAPI_DOCUMENTS;
import com.opentext.api.LAPI_USERS;
import com.opentext.api.LLSession;
import com.opentext.api.LLValue;

/**
 * A direct LAPI client implementation.
 */
final class LapiClient implements Client {
    /** The logger for this class. */
    private static final Logger LOGGER =
        Logger.getLogger(LapiClient.class.getName());

    static {
        // Verify that the Client class constants are correct.
        assert TOPICSUBTYPE == LAPI_DOCUMENTS.TOPICSUBTYPE :
            LAPI_DOCUMENTS.TOPICSUBTYPE;
        assert REPLYSUBTYPE == LAPI_DOCUMENTS.REPLYSUBTYPE :
            LAPI_DOCUMENTS.REPLYSUBTYPE;
        assert PROJECTSUBTYPE == LAPI_DOCUMENTS.PROJECTSUBTYPE :
            LAPI_DOCUMENTS.PROJECTSUBTYPE;
        assert TASKSUBTYPE == LAPI_DOCUMENTS.TASKSUBTYPE :
            LAPI_DOCUMENTS.TASKSUBTYPE;
        assert CHANNELSUBTYPE == LAPI_DOCUMENTS.CHANNELSUBTYPE :
            LAPI_DOCUMENTS.CHANNELSUBTYPE;
        assert NEWSSUBTYPE == LAPI_DOCUMENTS.NEWSSUBTYPE :
            LAPI_DOCUMENTS.NEWSSUBTYPE;
        assert POLLSUBTYPE == LAPI_DOCUMENTS.POLLSUBTYPE :
            LAPI_DOCUMENTS.POLLSUBTYPE;
        assert DISPLAYTYPE_HIDDEN == LAPI_DOCUMENTS.DISPLAYTYPE_HIDDEN :
            LAPI_DOCUMENTS.DISPLAYTYPE_HIDDEN;
        assert CHARACTER_ENCODING_NONE ==
            LAPI_DOCUMENTS.CHARACTER_ENCODING_NONE :
            LAPI_DOCUMENTS.CHARACTER_ENCODING_NONE;
        assert CHARACTER_ENCODING_UTF8 ==
            LAPI_DOCUMENTS.CHARACTER_ENCODING_UTF8 :
            LAPI_DOCUMENTS.CHARACTER_ENCODING_UTF8;
        assert ATTR_DATAVALUES == LAPI_ATTRIBUTES.ATTR_DATAVALUES :
            LAPI_ATTRIBUTES.ATTR_DATAVALUES;
        assert ATTR_DEFAULTVALUES == LAPI_ATTRIBUTES.ATTR_DEFAULTVALUES :
            LAPI_ATTRIBUTES.ATTR_DEFAULTVALUES;
        assert ATTR_TYPE_BOOL == LAPI_ATTRIBUTES.ATTR_TYPE_BOOL :
            LAPI_ATTRIBUTES.ATTR_TYPE_BOOL;
        assert ATTR_TYPE_DATE == LAPI_ATTRIBUTES.ATTR_TYPE_DATE :
            LAPI_ATTRIBUTES.ATTR_TYPE_DATE;
        assert ATTR_TYPE_DATEPOPUP == LAPI_ATTRIBUTES.ATTR_TYPE_DATEPOPUP :
            LAPI_ATTRIBUTES.ATTR_TYPE_DATEPOPUP;
        assert ATTR_TYPE_REAL == LAPI_ATTRIBUTES.ATTR_TYPE_REAL :
            LAPI_ATTRIBUTES.ATTR_TYPE_REAL;
        assert ATTR_TYPE_REALPOPUP == LAPI_ATTRIBUTES.ATTR_TYPE_REALPOPUP :
            LAPI_ATTRIBUTES.ATTR_TYPE_REALPOPUP;
        assert ATTR_TYPE_INTPOPUP == LAPI_ATTRIBUTES.ATTR_TYPE_INTPOPUP :
            LAPI_ATTRIBUTES.ATTR_TYPE_INTPOPUP;
        assert ATTR_TYPE_SET == LAPI_ATTRIBUTES.ATTR_TYPE_SET :
            LAPI_ATTRIBUTES.ATTR_TYPE_SET;
        assert ATTR_TYPE_STRFIELD == LAPI_ATTRIBUTES.ATTR_TYPE_STRFIELD :
            LAPI_ATTRIBUTES.ATTR_TYPE_STRFIELD;
        assert ATTR_TYPE_STRMULTI == LAPI_ATTRIBUTES.ATTR_TYPE_STRMULTI :
            LAPI_ATTRIBUTES.ATTR_TYPE_STRMULTI;
        assert ATTR_TYPE_STRPOPUP == LAPI_ATTRIBUTES.ATTR_TYPE_STRPOPUP :
            LAPI_ATTRIBUTES.ATTR_TYPE_STRPOPUP;
        assert ATTR_TYPE_USER == LAPI_ATTRIBUTES.ATTR_TYPE_USER :
            LAPI_ATTRIBUTES.ATTR_TYPE_USER;
        assert CATEGORY_TYPE_LIBRARY == LAPI_ATTRIBUTES.CATEGORY_TYPE_LIBRARY :
            LAPI_ATTRIBUTES.CATEGORY_TYPE_LIBRARY;
        assert CATEGORY_TYPE_WORKFLOW ==
            LAPI_ATTRIBUTES.CATEGORY_TYPE_WORKFLOW :
            LAPI_ATTRIBUTES.CATEGORY_TYPE_WORKFLOW;
    }
    
    /**
     * The Livelink session. LLSession instances are not thread-safe,
     * so all of the methods that access this session, directly or
     * indirectly, must be synchronized.
     */
    private final LLSession session;
    
    private final LAPI_DOCUMENTS documents;

    private final LAPI_USERS users;

    private final LAPI_ATTRIBUTES attributes;

    /*
     * Constructs a new client using the given session. Initializes
     * and subsidiary objects that are needed.
     *
     * @param session a new Livelink session
     */
    LapiClient(LLSession session) {
        this.session = session;
        this.documents = new LAPI_DOCUMENTS(session);
        this.users = new LAPI_USERS(session);
        this.attributes = new LAPI_ATTRIBUTES(session);
    }

    /** {@inheritDoc} */
    public ClientValueFactory getClientValueFactory()
            throws RepositoryException {
        return new LapiClientValueFactory();
    }

    /** {@inheritDoc} */
    public synchronized ClientValue GetServerInfo() throws RepositoryException {
        LLValue value = new LLValue();
        try {
            if (documents.GetServerInfo(value) != 0)
                throw new LapiException(session, LOGGER);
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(value);
    }

    /** {@inheritDoc} */
    public synchronized int GetCurrentUserID() throws RepositoryException {
        LLValue id = new LLValue();
        try {
            if (users.GetCurrentUserID(id) != 0)
                throw new LapiException(session, LOGGER);
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return id.toInteger();
    }
    
    /** {@inheritDoc} */
    public synchronized ClientValue GetCookieInfo()
            throws RepositoryException {
        LLValue cookies = new LLValue();
        try {
            if (users.GetCookieInfo(cookies) != 0)
                throw new LapiException(session, LOGGER);
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(cookies);
    }
    
    /** {@inheritDoc} */
    public synchronized ClientValue GetUserOrGroupByIDNoThrow(int id)
            throws RepositoryException {
        LLValue userInfo = new LLValue();
        try {
            if (users.GetUserOrGroupByID(id, userInfo) != 0) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(LapiException.buildMessage(session));
                return null;
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(userInfo);
    }

    /** {@inheritDoc} */
    public synchronized ClientValue AccessEnterpriseWS()
            throws RepositoryException {
        LLValue info = new LLValue();
        try {
            if (documents.AccessEnterpriseWS(info) != 0)
                throw new LapiException(session, LOGGER);
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(info);
    }
    
    /** {@inheritDoc} */
    public synchronized ClientValue ListNodes(String query, String view,
            String[] columns) throws RepositoryException {

        ClientValue recArray = ListNodesNoThrow(query, view, columns);
        if (recArray == null)
            throw new LapiException(session, LOGGER);
        return recArray;
    }

    /** {@inheritDoc} */
    public synchronized ClientValue ListNodesNoThrow(String query, String view,
            String[] columns) throws RepositoryException {

        /* This ListNodesNoThrow version returns null rather than throwing
         * an exception on SQL query errors.
         * LivelinkTraversalManager tries to determine the back-end
         * repository DB by running an Oracle-specific query that succeeds
         * on Oracle, but throws an exception for SQL-Server.  The test
         * probe caught the expected exception, but it was still getting 
         * logged to the error log (to which users objected).  See Issue 4:
         * http://code.google.com/p/google-enterprise-connector-otex/issues/detail?id=4
         */
        LLValue recArray = new LLValue();
        LLValue args = (new LLValue()).setList();
        LLValue columnsList = (new LLValue()).setList();
        
        for (int i = 0; i < columns.length; i++)
            columnsList.add(columns[i]);

        try {
            if (documents.ListNodes(query, args, view, columnsList, 
                                    LAPI_DOCUMENTS.PERM_SEECONTENTS,
                                    LLValue.LL_FALSE, recArray) != 0) {
                return null;
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(recArray);
    }

    /** {@inheritDoc} */
    public synchronized ClientValue GetObjectInfo(int volumeId, int objectId)
            throws RepositoryException {
        LLValue objectInfo = new LLValue();
        try {
            if (documents.GetObjectInfo(volumeId, objectId, objectInfo) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(objectInfo);
    }
    
    /** {@inheritDoc} */
    public synchronized ClientValue GetObjectAttributesEx(
            ClientValue objectIdAssoc, ClientValue categoryIdAssoc)
            throws RepositoryException {
        LLValue categoryVersion = new LLValue();
        try {
            LLValue objIDa = ((LapiClientValue) objectIdAssoc).getLLValue();
            LLValue catIDa = ((LapiClientValue) categoryIdAssoc).getLLValue();
            if (documents.GetObjectAttributesEx(objIDa, catIDa,
                    categoryVersion) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(categoryVersion);
    }
    
    
    /** {@inheritDoc} */
    public synchronized ClientValue AttrListNames(ClientValue categoryVersion,
            ClientValue attributeSetPath) throws RepositoryException {
        LLValue attrNames = new LLValue();
        try {
            LLValue catVersion =
                ((LapiClientValue) categoryVersion).getLLValue();
            LLValue attrPath = (attributeSetPath == null) ? null :
                ((LapiClientValue) attributeSetPath).getLLValue();

            // LAPI AttrListNames method does not reset the session status.
            session.setError(0, "");
            if (attributes.AttrListNames(catVersion, attrPath,
                    attrNames) != 0) {
                throw new LapiException(session, LOGGER); 
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(attrNames);
    }
    
    
    /** {@inheritDoc} */
    public synchronized ClientValue AttrGetInfo(ClientValue categoryVersion,
            String attributeName, ClientValue attributeSetPath)
            throws RepositoryException {
        LLValue info = new LLValue();
        try {
            LLValue catVersion =
                ((LapiClientValue) categoryVersion).getLLValue();
            LLValue attrPath = (attributeSetPath == null) ? null :
                ((LapiClientValue) attributeSetPath).getLLValue();

            // LAPI AttrGetInfo method does not reset the session status.
            session.setError(0, "");
            if (attributes.AttrGetInfo(catVersion, attributeName, attrPath,
                    info) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(info);
    }
    
    
    /** {@inheritDoc} */
    public synchronized ClientValue AttrGetValues(ClientValue categoryVersion,
            String attributeName, ClientValue attributeSetPath)
            throws RepositoryException {
        LLValue attrValues = new LLValue();
        try {
            LLValue catVersion =
                ((LapiClientValue) categoryVersion).getLLValue();
            LLValue attrPath = (attributeSetPath == null) ? null :
                ((LapiClientValue) attributeSetPath).getLLValue();

            // LAPI AttrGetValues method does not reset the session status.
            session.setError(0, "");
            if (attributes.AttrGetValues(catVersion, attributeName,
                    LAPI_ATTRIBUTES.ATTR_DATAVALUES, attrPath,
                    attrValues) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(attrValues);
    }


    /** {@inheritDoc} */
    public synchronized ClientValue ListObjectCategoryIDs(
            ClientValue objectIdAssoc) throws RepositoryException {
        LLValue categoryIds = new LLValue();
        try {
            LLValue objIDa = ((LapiClientValue) objectIdAssoc).getLLValue();
            if (documents.ListObjectCategoryIDs(objIDa, categoryIds) != 0)
                throw new LapiException(session, LOGGER);
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(categoryIds);
    }
    

    /** {@inheritDoc} */
    public synchronized void FetchVersion(int volumeId, int objectId,
            int versionNumber, File path) throws RepositoryException {
        try {
            if (documents.FetchVersion(volumeId, objectId, versionNumber,
                    path.getPath()) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
    }

    /** {@inheritDoc} */
    public synchronized void FetchVersion(int volumeId, int objectId,
            int versionNumber, OutputStream out) throws RepositoryException {
        try {
            if (documents.FetchVersion(volumeId, objectId,
                    versionNumber, out) != 0) { 
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
    }

    /** {@inheritDoc} */
    public synchronized ClientValue GetVersionInfo(int volumeId, int objectId,
            int versionNumber) throws RepositoryException {
        LLValue versionInfo = new LLValue();
        try {
            if (documents.GetVersionInfo(volumeId, objectId, versionNumber,
                                         versionInfo) != 0) {
                throw new LapiException(session, LOGGER);
            }
        } catch (RuntimeException e) {
            throw new LapiException(e, LOGGER);
        }
        return new LapiClientValue(versionInfo);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void ImpersonateUser(String username) {
        session.ImpersonateUser(username);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void ImpersonateUserEx(String username, String domain) {
        if ((domain == null) || (domain.length() == 0))
            session.ImpersonateUser(username);
        else
            session.ImpersonateUserEx(username, domain);
    }
}
