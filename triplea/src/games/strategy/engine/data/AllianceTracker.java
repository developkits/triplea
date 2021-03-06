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
 * AllianceTracker.java
 * 
 * Created on October 13, 2001, 9:37 AM
 */
package games.strategy.engine.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Tracks alliances between players.
 * 
 * An alliance is a named entity, players are added to an alliance.
 * 
 * Currently only used for tracking stats (like TUV, total production, etc), and for tracking total victory cities for alliance based victory conditions.
 * 
 * Not used for determining in-game alliances (instead, see the Relationship tracker for that).
 * 
 * @author Sean Bridges, modified by Erik von der Osten
 * @version 1.0
 */
public class AllianceTracker extends GameDataComponent
{
	private static final long serialVersionUID = 2815023984535209353L;
	// maps PlayerID to Collection of alliances names
	Map<PlayerID, Collection<String>> m_alliances = new HashMap<PlayerID, Collection<String>>();
	
	public AllianceTracker(final GameData data)
	{
		super(data);
	}
	
	/**
	 * Adds PlayerID player to the alliance specified by allianceName.
	 * 
	 * @param player
	 *            The player to add to the alliance.
	 * @param allianceName
	 *            The alliance to add to.
	 */
	protected void addToAlliance(final PlayerID player, final String allianceName)
	{
		if (!m_alliances.containsKey(player))
		{
			final Collection<String> alliances = new HashSet<String>();
			alliances.add(allianceName);
			m_alliances.put(player, alliances);
		}
		else
		{
			final Collection<String> alliances = m_alliances.get(player);
			alliances.add(allianceName);
		}
	}
	
	/*
	  Removes player from alliance allianceName. Throws an exception if
	  player is not in that alliance. Throws an exception if the player
	  is not in the specified alliance.
	  
	  @param player
	             The player to remove from the alliance.
	  @param allianceName
	             The alliance to remove from.
	*/
	/*
	protected void removeFromAlliance(PlayerID player, String allianceName)
	{
		if (!m_alliances.containsKey(player))
		{
			throw new IllegalStateException("Cannot remove player from alliance they are not in.");
		}
		else
		{
			Collection<String> alliances = m_alliances.get(player);
			if (!alliances.contains(allianceName))
			{
				throw new IllegalStateException("Cannot remove player from alliance they are not in.");
			}
			else
			{
				alliances.remove(allianceName);
			}
		}
	}
	*/
	/*
	//Returns whether two players are allied.	isAllied(a,a) returns true.
	public boolean isAllied(PlayerID p1, PlayerID p2)
	{
		if (useRelationshipModel())
		{
			return Matches.RelationshipIsAllied.match((getRelationshipType(p1, p2)));
		}
		
		if (p1 == null || p2 == null)
			throw new IllegalArgumentException("Arguments cannot be null p1:" + p1 + " p2:" + p2);
		
		if (p1.equals(p2))
			return true;
		
		if (!m_alliances.containsKey(p1) || !m_alliances.containsKey(p2))
			return false;
		
		Collection<String> a1 = m_alliances.get(p1);
		Collection<String> a2 = m_alliances.get(p2);
		
		return games.strategy.util.Util.someIntersect(a1, a2);
		
	}
	*/
	/**
	 * 
	 * @return a set of all the games alliances, this will return an empty set if you aren't using alliances
	 */
	public Set<String> getAlliances()
	{
		final Iterator<PlayerID> keys = m_alliances.keySet().iterator();
		final Set<String> rVal = new HashSet<String>();
		while (keys.hasNext())
		{
			rVal.addAll(m_alliances.get(keys.next()));
		}
		return rVal;
	}
	
	/**
	 * Returns the PlayerID's that are members of the alliance
	 * specified by the String allianceName
	 * 
	 * @param allianceName
	 *            Alliance name
	 * @return all the players in the given alliance
	 * 
	 */
	public HashSet<PlayerID> getPlayersInAlliance(final String allianceName)
	{
		final Iterator<PlayerID> keys = m_alliances.keySet().iterator();
		final HashSet<PlayerID> rVal = new HashSet<PlayerID>();
		while (keys.hasNext())
		{
			final PlayerID player = keys.next();
			final Collection<String> alliances = m_alliances.get(player);
			if (alliances.contains(allianceName))
				rVal.add(player);
		}
		return rVal;
	}
	
	public Collection<String> getAlliancesPlayerIsIn(final PlayerID player)
	{
		final Collection<String> rVal = m_alliances.get(player);
		if (rVal == null)
		{
			// throw new IllegalStateException("Player, " + player.getName() + ", is not a member of any alliance!");
			System.out.println("Player, " + player.getName() + ", is not a member of any alliance!");
			return Collections.singleton(player.getName());
		}
		return rVal;
	}
	
	public Map<PlayerID, Collection<String>> getAlliancesMap()
	{
		return m_alliances;
	}
}
