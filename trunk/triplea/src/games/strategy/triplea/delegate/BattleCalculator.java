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

/*
 * BattleCalculator.java
 *
 * Created on November 29, 2001, 2:27 PM
 */

package games.strategy.triplea.delegate;

import java.util.*;

import games.strategy.triplea.Constants;
import games.strategy.engine.message.Message;
import games.strategy.triplea.delegate.message.*;
import games.strategy.triplea.attatchments.*;
import games.strategy.triplea.util.*;
import games.strategy.util.*;
import games.strategy.engine.delegate.DelegateBridge;
import games.strategy.engine.data.*;

/**
 *
 * @author  Sean Bridges
 * @version 1.0
 *
 * Utiltity class for determing casualties and
 * selecting casualties.  The code was being dduplicated all over the place.
 */
public class BattleCalculator
{

  public static int getAAHits(Collection units, DelegateBridge bridge, int[] dice)
  {
    int attackingAirCount = Match.countMatches(units, Matches.UnitIsAir);

    int hitCount = 0;
    for(int i = 0; i < attackingAirCount; i++)
    {
      if(1 > dice[i])
        hitCount++;
    }
    return hitCount;
  }

  public static Collection fourthEditionAACasualties(Collection planes, DiceRoll dice) 
  {
    Collection casualties = new ArrayList();
    int[] aaRolls = dice.getRolls(1);
    Iterator planesIter = planes.iterator();
    // Number of dice rolls must be equal to number of planes
    if (planes.size() != aaRolls.length) {
      throw new IllegalStateException("Number of aa rolls not equal to number of planes");
    }
    for (int i = 0; i < aaRolls.length; i++)
    {
      Object unit = planesIter.next();
      if (DiceRoll.aaHit(aaRolls[i]))
      {
	casualties.add(unit);
      }
    }
    return casualties;
  }

  public static SelectCasualtyMessage selectCasualties(PlayerID player, Collection targets, DelegateBridge bridge, String text, GameData data, DiceRoll dice, boolean defending)
  {
    return selectCasualties(null, player, targets, bridge, text, data, dice, defending);
  }


  public static SelectCasualtyMessage selectCasualties(String step, PlayerID player, Collection targets, DelegateBridge bridge, String text, GameData data, DiceRoll dice, boolean defending)
  {
    int hits = dice.getHits();
    if(hits == 0)
      return new SelectCasualtyMessage(Collections.EMPTY_LIST, Collections.EMPTY_LIST, false);

    Map dependents = getDependents(targets,data);

    // If all targets are one type and not two hit then
    // just remove the appropriate amount of units of that type.
    // Sets the appropriate flag in the select casualty message
    // such that user is prompted to continue since they did not
    // select the units themselves.
    if (allTargetsOneTypeNotTwoHit(targets, dependents)) {
      List killed = new ArrayList();
      Iterator iter = targets.iterator();
      for (int i = 0; i < hits; i++) {
	killed.add(iter.next());
      }
      return new SelectCasualtyMessage(killed, Collections.EMPTY_LIST, true);
    }


    // Create production cost map, Maybe should do this elsewhere, but in case prices
    // change, we do it here.
    IntegerMap costs = getCosts(player, data);

    List defaultCasualties = getDefaultCasualties(targets, hits, defending, player, costs);
    Message msg = new SelectCasualtyQueryMessage(step, targets, dependents, dice.getHits(), text, dice, player, defaultCasualties);
    Message response = bridge.sendMessage(msg, player);
    if(!(response instanceof SelectCasualtyMessage))
      throw new IllegalStateException("Message of wrong type:" + response);

    SelectCasualtyMessage casualtySelection = (SelectCasualtyMessage) response;
    List killed = casualtySelection.getKilled();
    List damaged = casualtySelection.getDamaged();

    //check right number
    if ( ! (killed.size()  + damaged.size() == dice.getHits()) )
    {
      bridge.sendMessage( new StringMessage("Wrong number of casualties selected", true), player);
      return selectCasualties(player, targets, bridge,text, data, dice, defending);
    }
    //check we have enough of each type
    if(!targets.containsAll(killed) || ! targets.containsAll(damaged))
    {
      bridge.sendMessage( new StringMessage("Cannot remove enough units of those types", true), player);
      return selectCasualties(player, targets, bridge,text, data, dice, defending);
    }
    return casualtySelection;
  }

  private static List getDefaultCasualties(Collection targets, int hits, boolean defending, PlayerID player, IntegerMap costs) {
    // Remove two hit bb's selecting them first for default casualties
    ArrayList defaultCasualties = new ArrayList();
    int numSelectedCasualties = 0;
    Iterator targetsIter = targets.iterator();
    while (targetsIter.hasNext()) {
      // Stop if we have already selected as many hits as there are targets
      if (numSelectedCasualties >= hits) {
	return defaultCasualties;
      }
      Unit unit = (Unit)targetsIter.next();
      UnitAttatchment ua = UnitAttatchment.get(unit.getType());
      if (ua.isTwoHit() && (unit.getHits() == 0)) {
	numSelectedCasualties++;
	defaultCasualties.add(unit);	
      }
    }
    
    // Sort units by power and cost in ascending order
    List sorted = new ArrayList(targets);
    Collections.sort(sorted,new UnitBattleComparator(defending, player, costs));
    // Select units
    Iterator sortedIter = sorted.iterator();
    while (sortedIter.hasNext()) {
      // Stop if we have already selected as many hits as there are targets
      if (numSelectedCasualties >= hits) {
	return defaultCasualties;
      }
      Unit unit = (Unit) sortedIter.next();
      defaultCasualties.add(unit);
      numSelectedCasualties++;
    }

    return defaultCasualties;
  }

  private static Map getDependents(Collection targets, GameData data)
  {
    //jsut worry about transports
    TransportTracker tracker = DelegateFinder.moveDelegate(data).getTransportTracker();

    Map dependents = new HashMap();
    Iterator iter = targets.iterator();
    while(iter.hasNext())
    {
      Unit target = (Unit) iter.next();
      dependents.put( target, tracker.transportingAndUnloaded(target));
    }
    return dependents;
  }

  /**
   * Return map where keys are unit types and values are ipc costs of that unit type
   * @param player The player to get costs schedule for
   * @param data The game data.
   * @return a map of unit types to ipc cost
   */
  public static IntegerMap getCosts(PlayerID player, GameData data) {
    IntegerMap costs = new IntegerMap();
    Iterator iter = player.getProductionFrontier().getRules().iterator();
    while(iter.hasNext())
    {
      ProductionRule rule = (ProductionRule) iter.next();
      int cost = rule.getCosts().getInt(data.getResourceList().getResource(Constants.IPCS));
      UnitType type = (UnitType)rule.getResults().keySet().iterator().next();
      costs.put(type, cost);
    }
    return costs;
  }

  /**
   * Checks if the given collections target are all of one category as
   * defined by UnitSeperator.categorize and they are not two hit units.
   * @param targets a collection of target units
   * @param dependents map of depend units for target units
   */
  private static boolean allTargetsOneTypeNotTwoHit(Collection targets, Map dependents)
  {
    Set categorized = UnitSeperator.categorize(targets, dependents, null);
    if (categorized.size() == 1) {
      UnitCategory unitCategory = (UnitCategory)categorized.iterator().next();
      if (!unitCategory.isTwoHit() || unitCategory.getDamaged()) {
	return true;
      }
    }

    return false;
  }


  public static int getRolls(Collection units, PlayerID id, boolean defend)
  {
    int count = 0;
    Iterator iter = units.iterator();
    while(iter.hasNext())
    {
      Unit unit = (Unit) iter.next();    
      count+=getRolls(unit,id, defend);
    }
    return count;
  }
  
  public static int getRolls(Unit unit, PlayerID id, boolean defend)
  {
      if(defend)
          return 1;
      return UnitAttatchment.get(unit.getType()).getAttackRolls(id);
      
  }

  //nothing but static
  private BattleCalculator()
  {}
}

class UnitBattleComparator implements Comparator {
  private boolean m_defending;
  private PlayerID m_player;
  private IntegerMap m_costs;

  public UnitBattleComparator(boolean defending, PlayerID player, IntegerMap costs) {
    m_defending = defending;
    m_player = player;
    m_costs = costs;
  }

  public int compare(Object o1, Object o2) {
    Unit u1 = (Unit)o1;
    Unit u2 = (Unit)o2;
    UnitAttatchment ua1 = UnitAttatchment.get(u1.getType());
    UnitAttatchment ua2 = UnitAttatchment.get(u2.getType());
    int rolls1 = BattleCalculator.getRolls(u1, m_player, m_defending);
    int rolls2 = BattleCalculator.getRolls(u2, m_player, m_defending);
    int power1 = m_defending ? ua1.getDefense(m_player) : ua1.getAttack(m_player);
    int power2 = m_defending ? ua2.getDefense(m_player) : ua2.getAttack(m_player);
    int cost1 = m_costs.getInt(u1.getType());
    int cost2 = m_costs.getInt(u2.getType());
    //    System.out.println("Unit 1: " + u1 + " rolls: " + rolls1 + " power: " + power1 + " cost: " + cost1);
    //    System.out.println("Unit 2: " + u2 + " rolls: " + rolls2 + " power: " + power2 + " cost: " + cost2);
    if (rolls1 != rolls2) {
      return rolls1 - rolls2;
    }
    if (power1 != power2) {
      return power1 - power2;
    }
    if (cost1 != cost2) {
      return cost1 - cost2;
    }

    return 0;
  }
}
