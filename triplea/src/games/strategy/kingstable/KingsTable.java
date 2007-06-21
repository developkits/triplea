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

package games.strategy.kingstable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import games.strategy.engine.framework.IGame;
import games.strategy.engine.framework.IGameLoader;
import games.strategy.engine.gamePlayer.IGamePlayer;
import games.strategy.engine.message.IChannelSubscribor;
import games.strategy.engine.message.IRemote;
import games.strategy.kingstable.player.BetterAI;
import games.strategy.kingstable.player.IKingsTablePlayer;
import games.strategy.kingstable.player.KingsTablePlayer;
import games.strategy.kingstable.player.RandomAI;
import games.strategy.kingstable.ui.KingsTableFrame;
import games.strategy.kingstable.ui.display.IKingsTableDisplay;
import games.strategy.kingstable.ui.display.KingsTableDisplay;


/**
 * Main class responsible for a Kings Table game.
 *
 * @author Lane Schwartz
 * @version $LastChangedDate$
 */
public class KingsTable implements IGameLoader
{

    // When serializing, do not save transient member variables
    private transient KingsTableDisplay m_display;
    private transient IGame m_game;
    
    private static final String HUMAN_PLAYER_TYPE = "Human";
    private static final String RANDOM_COMPUTER_PLAYER_TYPE = "Random AI";
    private static final String BETTER_COMPUTER_PLAYER_TYPE = "Better AI";
        
    /**
     * @see IGameLoader.createPlayers(playerNames)
     */
    public Set<IGamePlayer> createPlayers(Map playerNames)
    {
        Set<IGamePlayer> players = new HashSet<IGamePlayer>();
        Iterator iter = playerNames.keySet().iterator();
        while (iter.hasNext())
        {
            String name = (String) iter.next();
            String type = (String) playerNames.get(name);
            if (type.equals(HUMAN_PLAYER_TYPE) || type.equals(CLIENT_PLAYER_TYPE))
            {
                KingsTablePlayer player = new KingsTablePlayer(name);
                players.add(player);
            }
            else if (type.equals(RANDOM_COMPUTER_PLAYER_TYPE)) 
            {
                RandomAI ai = new RandomAI(name);
                players.add(ai);
            }
            else if (type.equals(BETTER_COMPUTER_PLAYER_TYPE)) 
            {
                BetterAI ai = new BetterAI(name);
                players.add(ai);
            }
            else
            {
                throw new IllegalStateException("Player type not recognized:" + type);
            }
        }
        return players;
    }
    
    /**
     * Return an array of player types that can play on the server. 
     */
    public String[] getServerPlayerTypes()
    {
        return new String[]
        {HUMAN_PLAYER_TYPE, RANDOM_COMPUTER_PLAYER_TYPE, BETTER_COMPUTER_PLAYER_TYPE};
            
    }

    
    public void shutDown()
    {
        if(m_display != null) {
            m_game.removeDisplay(m_display);
            m_display.shutDown();
        }        
    }
    
    public void startGame(final IGame game, final Set<IGamePlayer> players) throws Exception
    {
        try
        {
            m_game = game;

            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    final KingsTableFrame frame = new KingsTableFrame(game, players);

                    m_display = new KingsTableDisplay(frame);
                    m_game.addDisplay(m_display);
                    frame.setVisible(true);
                    connectPlayers(players, frame);

                    SwingUtilities.invokeLater(
                            new Runnable()
                            {
                                public void run()
                                {
                                    //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                                    frame.toFront();
                                }
                            }
                    );

                }

            });
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            if(e.getCause() instanceof Exception)
                throw (Exception) e.getCause();
            else
            {
                e.printStackTrace();
                throw new IllegalStateException(e.getCause().getMessage());
            }

        }

    }

    private void connectPlayers(Set<IGamePlayer> players, KingsTableFrame frame)
    {
        Iterator<IGamePlayer> iter = players.iterator();
        while (iter.hasNext())
        {
            IGamePlayer player = iter.next();
            if (player instanceof KingsTablePlayer)
                ((KingsTablePlayer) player).setFrame(frame);
        }
    }
    
    /** 
     * @see games.strategy.engine.framework.IGameLoader#getDisplayType()
     */
    public Class<? extends IChannelSubscribor> getDisplayType()
    {
        return IKingsTableDisplay.class;
    }
    
    public Class<? extends IRemote> getRemotePlayerType()
    {
        return IKingsTablePlayer.class;
    }
}
