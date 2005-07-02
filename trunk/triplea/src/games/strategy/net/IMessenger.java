/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package games.strategy.net;

import java.io.*;
import java.util.*;

/**
 * A simple way to connect multiple socket end points.
 * An IMessenger listens for incoming messages, and sends
 * them to all registered listeners.
 * 
 * Messages are recieved and sent in order.  Note that message listeners are multi threaded, in
 * that they process messages from multiple nodes at the same time, but no more than 1 message 
 * from any particular node at a time.
 * 
 *  
 */
public interface IMessenger
{
    /**
     * Send a message to the given node. Returns immediately.
     */
    public void send(Serializable msg, INode to);
    /**
     * Send a message to all nodes.
     */
    public void broadcast(Serializable msg);
    /**
     * Listen for messages.  
     */
    public void addMessageListener(IMessageListener listener);
    /**
     * Stop listening to messages.
     */
    public void removeMessageListener(IMessageListener listener);
    /**
     * Listen for errors
     */
    public void addErrorListener(IMessengerErrorListener listener);
    /**
     * Stop listening for errors.
     */
    public void removeErrorListener(IMessengerErrorListener listener);

    /**
     * Get the local node
     */
    public INode getLocalNode();

    /**
     * Get a list of nodes.
     */
    public Set<INode> getNodes();
    /**
     * test the connection.
     */
    public boolean isConnected();
    /**
     * Shut the connection down.
     */
    public void shutDown();

    /**
     * Add a listener for change in connection status.
     */
    public void addConnectionChangeListener(IConnectionChangeListener listener);

    /**
     * Remove a listener for change in connection status.
     */
    public void removeConnectionChangeListener(IConnectionChangeListener listener);

    /**
     * Returns when all messages have been written over the network. shutdown causes this method to return. Does not gaurantee that the messages have reached their destination.
     */
    public void flush();
    
    /**
     * Am I the server node?
     * There should only be one server node, and it should exist before other nodes.
     */
    public boolean isServer();
}
