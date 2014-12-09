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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.MarkedReference;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.ServerSessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class EXoContinuationBayeux extends BayeuxServerImpl {

    /**
     * Map for userToken.
     */
    private static Map<String, String> userToken         = new HashMap<String, String>();

    /**
     * Generate userToken.
     */
    transient Random                   random;

    /**
     * Timeout.
     */
    private long                       timeout;

    /**
     * Cometd webapp context name
     */
    private String                     cometdContextName = "cometd";

    /**
     * Used to send message to all client or a specific client that listen a
     * specific channel
     */
    private ServerSessionImpl          systemClient;

    /**
     * Logger.
     */
    private static final Log           LOG               = ExoLogger.getLogger(EXoContinuationBayeux.class);

    /**
     * Default constructor.
     */
    public EXoContinuationBayeux() {
        super();
        this.setSecurityPolicy(new EXoSecurityPolicy());
    }

    /**
     * {@inheritDoc}
     */
    public ServerSessionImpl newRemoteClient() {
        EXoContinuationClient client = new EXoContinuationClient(this);
        return client;
    }

    /**
     * {@inheritDoc}
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @return context name of cometd webapp
     */
    public String getCometdContextName() {
        return cometdContextName;
    }

    /**
     * {@inheritDoc}
     */
    long getRandom(long variation) {
        long l = random.nextLong() ^ variation;
        return l < 0 ? -l : l;
    }

    /**
     * @param eXoId the client id.
     * @return token for client
     */
    public String getUserToken(String eXoId) {
        if (userToken.containsKey(eXoId)) {
            return (String) userToken.get(eXoId);
        }
        String token = Long.toString(this.getRandom(System.identityHashCode(this)
                                             ^ System.currentTimeMillis()),
                                     36);
        userToken.put(eXoId, token);
        return token;
    }

    /* ------------------------------------------------------------ */
    /**
     * {@inheritDoc}
     */
    protected void initialize(ServletContext context) {
        // http://docs.cometd.org/3/apidocs/org/cometd/server/BayeuxServerImpl.html#createChannelIfAbsent-java.lang.String-org.cometd.bayeux.server.ConfigurableServerChannel.Initializer...-
        // It seems to be the new way to initialize a channel

        MarkedReference<ServerChannel> ref = createChannelIfAbsent(Channel.SERVICE,
                                                                   (ConfigurableServerChannel.Initializer) null);
        if (ref.isMarked())
            return; // avoid initializing twice

        cometdContextName = context.getServletContextName();
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            context.log("Could not get secure random for ID generation", e);
            random = new Random();
        }
        random.setSeed(random.nextLong() ^ hashCode() ^ (context.hashCode() << 32)
                ^ Runtime.getRuntime().freeMemory());
        if (LOG.isDebugEnabled())
            LOG.debug("Initialized");
    }

    /**
     * @param eXoID the id of client.
     * @return client with eXoID
     */
    public EXoContinuationClient getClientByEXoId(String eXoID) {
        List<ServerSession> ids = getSessions();
        for (ServerSession client : ids) {
            if (client instanceof EXoContinuationClient) {
                EXoContinuationClient exoClient = (EXoContinuationClient) client;
                if (exoClient.getEXoId() != null && exoClient.getEXoId().equals(eXoID))
                    return exoClient;
            }
        }
        return null;
    }

    /**
     * @param channel the id of channel.
     * @param data the message
     * @param msgId the message id
     */
    public void sendBroadcastMessage(String channel, Object data, String msgId) {
        ServerSessionImpl fromClient = getSystemClient();
        ServerChannel ch = getChannel(channel);
        if (ch != null) {
            ch.publish(fromClient, data);
            if (LOG.isDebugEnabled())
                LOG.debug("Send broadcast message " + data.toString() + " on channel " + channel);
        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("Message " + data.toString() + " not send. Channel " + channel
                        + " not exist!");
        }
    }

    /**
     * Send data to a individual client. The data passed is sent to the client
     * as the "data" member of a message with the given channel and id. The
     * message is not published on the channel and is thus not broadcast to all
     * channel subscribers. However to the target client, the message appears as
     * if it was broadcast.
     * <p>
     * Typcially this method is only required if a service method sends
     * response(s) to channels other than the subscribed channel. If the
     * response is to be sent to the subscribed channel, then the data can
     * simply be returned from the subscription method.
     * 
     * @param eXoId the id of target client
     * @param channel The id of channel the message is for
     * @param data The data of the message
     * @param id The id of the message (or null for a random id).
     */
    public void sendMessage(String eXoId, String channel, Object data, String id) {
        EXoContinuationClient toClient = getClientByEXoId(eXoId);
        ServerSessionImpl fromClient = getSystemClient();
        if (toClient != null) {
            toClient.deliver(fromClient, channel, data);
            if (LOG.isDebugEnabled())
                LOG.debug("Send message " + data.toString() + " on channel " + channel
                        + " to client " + eXoId);
        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("Message " + data.toString() + " not send on channel " + channel
                        + " client " + eXoId + " not exist!");
        }
    }

    private ServerSessionImpl getSystemClient() {
        if (systemClient == null) {
            systemClient = (ServerSessionImpl) newServerSession();
        }
        return systemClient;
    }

    /* ------------------------------------------------------------ */
    /**
     * @author vetal
     */
    public static class EXoSecurityPolicy implements SecurityPolicy {

        /**
       * 
       */
        public EXoSecurityPolicy() {
            super();
        }

        /**
         * @param message the cometd message.
         * @return true if user valid else false.
         */
        private boolean checkUser(ServerMessage message) {
            String userId = (String) message.get("exoId");
            String eXoToken = (String) message.get("exoToken");
            return (userId != null && userToken.containsKey(userId) && userToken.get(userId)
                                                                                .equals(eXoToken));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canCreate(BayeuxServer server,
                                 ServerSession client,
                                 String channelId,
                                 ServerMessage message) {
            //
            Boolean b = client != null && !channelId.startsWith("/meta/");
            return b;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canHandshake(BayeuxServer server, ServerSession client, ServerMessage message) {
            return checkUser(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canPublish(BayeuxServer server,
                                  ServerSession client,
                                  ServerChannel channel,
                                  ServerMessage message) {
            Boolean b = client != null && !channel.getId().startsWith("/meta/");
            return b;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canSubscribe(BayeuxServer server,
                                    ServerSession client,
                                    ServerChannel channel,
                                    ServerMessage message) {

            if (!checkUser(message)) {
                return false;
            }
            // We set the eXoID
            if (((EXoContinuationClient) client).getEXoId() == null) {
                ((EXoContinuationClient) client).setEXoId((String) message.get("exoId"));
            }
            return client != null && !channel.getId().startsWith("/meta/");
        }

    }

}
