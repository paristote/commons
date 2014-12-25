package org.mortbay.cometd.continuation;

/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.CometDServlet;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class EXoContinuationCometdServlet extends CometDServlet {

    /**
    * 
    */
    private static final long serialVersionUID = 9204910608302112814L;

    /**
     * Logger.
     */
    private static final Log  LOG              = ExoLogger.getLogger(CometDServlet.class);

    /**
     * The portal container
     */
    private ExoContainer      container;
    
    public static final String PREFIX = "eXo.cometd.";
    
    public static String[] configs = {"transports", "allowedTransports", "jsonContext", "validateMessageFields", "broadcastToPublisher",
                                      "timeout", "interval", "maxInterval", "maxLazyTimeout", "metaConnectDeliverOnly", "maxQueue", "maxSessionsPerBrowser",
                                      "allowMultiSessionsNoBrowser", "multiSessionInterval", "browserCookieName", "browserCookieDomain", "browserCookiePath",
                                      "ws.cometdURLMapping", "ws.messagesPerFrame", "ws.bufferSize", "ws.maxMessageSize", "ws.idleTimeout"};
    
    /**
     * {@inheritDoc}
     */
    public void init(final ServletConfig config) throws ServletException {
        final PortalContainerPostInitTask task = new PortalContainerPostInitTask() {

            public void execute(ServletContext context, PortalContainer portalContainer) {
                EXoContinuationCometdServlet.this.container = portalContainer;
                try {                    
                    EXoContinuationCometdServlet.super.init(config);
                } catch (ServletException e) {
                    LOG.error("Cannot initialize Bayeux", e);
                }
            }
        };
        PortalContainer.addInitTask(config.getServletContext(), task);
    }

    /**
     * {@inheritDoc}
     */
    protected EXoContinuationBayeux newBayeuxServer() {
        try {
            if (LOG.isDebugEnabled())
                LOG.debug("EXoContinuationCometdServlet - Current Container-ExoContainer: "
                        + container);
            EXoContinuationBayeux bayeux = (EXoContinuationBayeux) container.getComponentInstanceOfType(BayeuxServer.class);
            bayeux.setTimeout(Long.parseLong(getInitParameter("timeout")));
            // bayeux.initialize(getServletContext());
            if (LOG.isDebugEnabled())
                LOG.debug("EXoContinuationCometdServlet - -->AbstractBayeux=" + bayeux);
            return bayeux;
        } catch (NumberFormatException e) {
            LOG.error("Error new Bayeux creation ", e);
            return null;
        }
    }

    @Override
    public String getInitParameter(String name) {
        String value = PropertyManager.getProperty(PREFIX + name);
        if (value == null) {
            value = super.getInitParameter(name);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getInitParameterNames() {
        Set<String> names = new HashSet<String>();
        names.addAll(Collections.list(super.getInitParameterNames()));        
        names.addAll(Arrays.asList(configs));
        
        return Collections.enumeration(names);
    }
}
