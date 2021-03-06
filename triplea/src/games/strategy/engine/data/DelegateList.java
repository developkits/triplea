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
/*
 * DelegateList.java
 * 
 * Created on October 17, 2001, 9:21 PM
 */
package games.strategy.engine.data;

import games.strategy.engine.delegate.IDelegate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Sean Bridges
 * 
 * 
 */
public class DelegateList extends GameDataComponent implements Iterable<IDelegate>
{
	private static final long serialVersionUID = 4156921032854553312L;
	private Map<String, IDelegate> m_delegates = new HashMap<String, IDelegate>();
	
	public DelegateList(final GameData data)
	{
		super(data);
	}
	
	public void addDelegate(final IDelegate del)
	{
		m_delegates.put(del.getName(), del);
	}
	
	public int size()
	{
		return m_delegates.size();
	}
	
	public Iterator<IDelegate> iterator()
	{
		return m_delegates.values().iterator();
	}
	
	public IDelegate getDelegate(final String name)
	{
		return m_delegates.get(name);
	}
	
	private void writeObject(final ObjectOutputStream out)
	{
		// dont write since delegates should be handled seperatly.
	}
	
	private void readObject(final ObjectInputStream in)
	{
		m_delegates = new HashMap<String, IDelegate>();
	}
}
