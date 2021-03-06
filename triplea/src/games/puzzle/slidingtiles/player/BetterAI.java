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
package games.puzzle.slidingtiles.player;

import games.puzzle.slidingtiles.attachments.Tile;
import games.strategy.common.player.ai.AIAlgorithm;
import games.strategy.common.player.ai.GameState;
import games.strategy.engine.data.GameMap;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.gamePlayer.IPlayerBridge;
import games.strategy.grid.delegate.remote.IGridPlayDelegate;
import games.strategy.grid.player.GridAbstractAI;
import games.strategy.grid.ui.GridPlayData;
import games.strategy.grid.ui.IGridPlayData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

/**
 * AI player for n-puzzle.
 * 
 * Capable of playing using depth-limited iterative deepening depth-first search algorithm.
 * 
 * @author Lane Schwartz
 * @version $LastChangedDate$
 */
public class BetterAI extends GridAbstractAI
{
	private int m_xDimension;
	private int m_yDimension;
	
	
	/** Algorithms available for use in BetterAI */
	public enum Algorithm
	{
		DFS
	}
	
	
	/** Heuristic */
	public enum Heuristic
	{
		NUMBER_OF_MISPLACED_TILES, MANHATTAN_DISTANCE
	}
	
	// The algorithm to be used
	private final Algorithm m_algorithm;
	//
	private final Heuristic m_heuristic;
	private Stack<Move> m_moves;
	
	public BetterAI(final String name, final String type, final Algorithm algorithm, final Heuristic heuristic)
	{
		super(name, type);
		m_algorithm = algorithm;
		m_heuristic = heuristic;
		m_moves = null;
	}
	
	@Override
	public void initialize(final IPlayerBridge bridge, final PlayerID id)
	{
		super.initialize(bridge, id);
		m_xDimension = getGameData().getMap().getXDimension();
		m_yDimension = getGameData().getMap().getYDimension();
	}
	
	@Override
	protected void play()
	{
		final IGridPlayDelegate iPlayDelegate = (IGridPlayDelegate) getPlayerBridge().getRemoteDelegate();
		final PlayerID me = getPlayerID();
		if (m_moves == null)
		{
			iPlayDelegate.signalStatus("Thinking...");
			try
			{
				if (m_algorithm.equals(Algorithm.DFS))
				{
					// GameProperties properties = m_bridge.getGameData().getProperties();
					int numberOfShuffles = 1;// Integer.valueOf((String) properties.get("Difficulty Level"));
					while (m_moves == null || m_moves.isEmpty())
					{
						m_moves = AIAlgorithm.depthFirstSearch(getInitialState(), numberOfShuffles++);
					}
					iPlayDelegate.signalStatus("Solvable in " + (numberOfShuffles - 1) + " moves");
					System.out.println("Solvable in " + (numberOfShuffles - 1) + " moves");
					iPlayDelegate.signalStatus("Done Thinking...");
				}
				else
					throw new RuntimeException("Invalid algorithm");
			} catch (final OutOfMemoryError e)
			{
				System.out.println("Ran out of memory while searching for next move: " + counter + " moves examined.");
				System.exit(-1);
			}
		}
		else
		{
			// Unless the triplea.ai.pause system property is set to false,
			// pause for 0.8 seconds to give the impression of thinking
			pause();
		}
		if (m_moves == null || m_moves.isEmpty())
		{
			iPlayDelegate.signalStatus("Too hard to solve!");
		}
		else
		{
			iPlayDelegate.signalStatus(" ");
			final Move move = m_moves.pop();
			final Territory start = getGameData().getMap().getTerritoryFromCoordinates(move.getStart().getFirst(), move.getStart().getSecond());
			final Territory end = getGameData().getMap().getTerritoryFromCoordinates(move.getEnd().getFirst(), move.getEnd().getSecond());
			final IGridPlayData play = new GridPlayData(start, end, me);
			iPlayDelegate.play(play);
			// if (playDel.play(start,end)==null)
			// System.out.println("Moving from " + start + " to " + end);
			// else
			// System.out.println("Illegal move from " + start + " to " + end);
		}
	}
	
	private State getInitialState()
	{
		return new State(getGameData().getMap());
	}
	
	public static int counter = 0;
	
	
	class State extends GameState<Move>
	{
		private final int[][] m_data;
		private final int m_depth;
		private final Move m_move;
		private int m_blankX;
		private int m_blankY;
		
		public State()
		{
			m_depth = 0;
			m_move = null;
			m_data = new int[m_xDimension][m_yDimension];
			m_data[0][0] = 3;
			m_data[1][0] = 1;
			m_data[2][0] = 2;
			m_data[0][1] = 4;
			m_data[1][1] = 0;
			m_data[2][1] = 5;
			m_data[0][2] = 6;
			m_data[1][2] = 7;
			m_data[2][2] = 8;
		}
		
		public State(final GameMap map)
		{
			m_depth = 0;
			m_move = null;
			m_data = new int[m_xDimension][m_yDimension];
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					final Territory territory = map.getTerritoryFromCoordinates(x, y);
					final Tile tile = (Tile) territory.getAttachment("tile");
					if (tile == null)
						throw new RuntimeException("Territory " + territory + " does not have an associated tile.");
					else
					{
						m_data[x][y] = tile.getValue();
						// System.out.println("m_data["+x+"]["+y+"]=="+m_data[x][y]);
						if (m_data[x][y] == 0)
						{
							m_blankX = x;
							m_blankY = y;
						}
					}
				}
			}
			// throw new RuntimeException("stop");
		}
		
		@Override
		public int hashCode()
		{
			int code = 0;
			int digit = 1;
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					code += m_data[x][y] * digit;
					digit *= 10;
				}
			}
			return code;
		}
		
		public boolean equals(final GameState<Move> state)
		{
			if (hashCode() == state.hashCode())
				return true;
			else
				return false;
		}
		
		@Override
		public String toString()
		{
			String s = "";
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					s += m_data[x][y] + " ";
				}
				s += "- ";
			}
			return s;
		}
		
		@Override
		public State getSuccessor(final Move move)
		{
			return new State(move, this);
		}
		
		@Override
		public Move getMove()
		{
			return m_move;
		}
		
		private State(final Move move, final State parentState)
		{
			m_move = move;
			m_depth = parentState.m_depth + 1;
			counter++;
			final int startX = move.getStart().getFirst();
			final int startY = move.getStart().getSecond();
			final int endX = move.getEnd().getFirst();
			final int endY = move.getEnd().getSecond();
			m_data = new int[m_xDimension][m_yDimension];
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					m_data[x][y] = parentState.m_data[x][y];
				}
			}
			final int tmp = m_data[startX][startY];
			m_data[startX][startY] = m_data[endX][endY];
			m_data[endX][endY] = tmp;
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					if (m_data[x][y] == 0)
					{
						m_blankX = x;
						m_blankY = y;
					}
				}
			}
		}
		
		@Override
		public Collection<GameState<Move>> successors()
		{
			final Collection<GameState<Move>> successors = new ArrayList<GameState<Move>>();
			final Pair<Integer, Integer> blankTile = new Pair<Integer, Integer>(m_blankX, m_blankY);
			if (m_blankX > 0)
				successors.add(new State(new Move(new Pair<Integer, Integer>(m_blankX - 1, m_blankY), blankTile), this));
			if (m_blankX < m_xDimension - 1)
				successors.add(new State(new Move(new Pair<Integer, Integer>(m_blankX + 1, m_blankY), blankTile), this));
			if (m_blankY > 0)
				successors.add(new State(new Move(new Pair<Integer, Integer>(m_blankX, m_blankY - 1), blankTile), this));
			if (m_blankY < m_yDimension - 1)
				successors.add(new State(new Move(new Pair<Integer, Integer>(m_blankX, m_blankY + 1), blankTile), this));
			return successors;
		}
		
		@Override
		public float getUtility()
		{
			float utility = 0.0f;
			switch (m_heuristic)
			{
				case NUMBER_OF_MISPLACED_TILES:
				{
					int misplacedTiles = 0;
					int value = 0;
					for (int y = 0; y < m_yDimension; y++)
					{
						for (int x = 0; x < m_xDimension; x++)
						{
							if (value != m_data[x][y])
								misplacedTiles++;
							value++;
						}
					}
					utility = -1 * misplacedTiles;
				}
			}
			return utility;
		}
		
		@Override
		public boolean gameIsOver()
		{
			// if (m_data[0][0]==0 && m_data[1][0]==1) return true;
			int value = -1;
			for (int y = 0; y < m_yDimension; y++)
			{
				for (int x = 0; x < m_xDimension; x++)
				{
					if (value < m_data[x][y])
						value = m_data[x][y];
					else
						return false;
				}
			}
			return true;
		}
		
		@Override
		public boolean cutoffTest()
		{
			if (gameIsOver())
				return true;
			// else if (m_depth >= cutoffDepth)
			// return true;
			else
				return false;
		}
	}
	
	
	class Move
	{
		private final Pair<Integer, Integer> m_start;
		private final Pair<Integer, Integer> m_end;
		
		public Move(final Pair<Integer, Integer> start, final Pair<Integer, Integer> end)
		{
			m_start = start;
			m_end = end;
		}
		
		public Pair<Integer, Integer> getStart()
		{
			return m_start;
		}
		
		public Pair<Integer, Integer> getEnd()
		{
			return m_end;
		}
		
		@Override
		public String toString()
		{
			return m_start + " -> " + m_end;
		}
	}
	
	
	class Pair<First, Second>
	{
		private final First m_first;
		private final Second m_second;
		
		Pair(final First first, final Second second)
		{
			m_first = first;
			m_second = second;
		}
		
		First getFirst()
		{
			return m_first;
		}
		
		Second getSecond()
		{
			return m_second;
		}
		
		@Override
		public String toString()
		{
			return "(" + m_first + "," + m_second + ")";
		}
	}
}
