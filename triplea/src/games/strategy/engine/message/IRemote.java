/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package games.strategy.engine.message;

/**
 * 
 * A marker interface, used to indicate that the interface
 * can be used by IRemoteMessenger.<br>
 * 
 * All arguments and return values to all methods of
 * an IRemote must be serializable, since the methods
 * may be called by a remote VM.<br>
 * 
 * Modifications to the paramaters of an IRemote may or may not
 * be visible to the calling object. <br>
 * 
 * All methods declared by an IRemote may though a MessengerException.
 * <p>
 * 
 * @author Sean Bridges
 */
public interface IRemote
{
}
