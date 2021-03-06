package games.strategy.triplea.ai.proAI.util;

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
import games.strategy.engine.data.ProductionRule;
import games.strategy.util.IntegerMap;

import java.util.logging.Level;

/**
 * Pro AI metrics.
 * 
 * @author Ron Murhammer
 * @since 2014
 */
public class ProMetricUtils
{
	
	private static IntegerMap<ProductionRule> totalPurchaseMap = new IntegerMap<ProductionRule>();
	
	public static void collectPurchaseStats(final IntegerMap<ProductionRule> purchaseMap)
	{
		totalPurchaseMap.add(purchaseMap);
		LogUtils.log(Level.FINER, totalPurchaseMap.toString());
	}
	
}
