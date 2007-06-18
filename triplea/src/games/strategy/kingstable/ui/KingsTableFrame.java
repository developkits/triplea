package games.strategy.kingstable.ui;

//import games.strategy.common.ui.BasicGameMenuBar;
import games.strategy.common.ui.MacWrapper;
import games.strategy.common.ui.MainGameFrame;
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.framework.ClientGame;
import games.strategy.engine.framework.GameRunner;
import games.strategy.engine.framework.IGame;
import games.strategy.engine.framework.ServerGame;
import games.strategy.engine.framework.startup.ui.MainFrame;
import games.strategy.engine.gamePlayer.IGamePlayer;
import games.strategy.engine.gamePlayer.IPlayerBridge;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class KingsTableFrame extends MainGameFrame
{
    private GameData m_data;
    private IGame m_game;
    
    private MapPanel m_mapPanel;
    private JLabel m_status;
    private JLabel m_error;

    private boolean m_gameOver;

    public KingsTableFrame(IGame game, Set<IGamePlayer> players)
    {
        m_gameOver = false;
        
        m_game = game;
        m_data = game.getData();

        // Get the dimension of the gameboard - specified in the game's xml file.
        int x_dim = m_data.getMap().getXDimension();
        int y_dim = m_data.getMap().getYDimension();

        // The MapData holds info for the map, 
        //    including the dimensions (x_dim and y_dim)
        //    and the size of each square (50 by 50)
        MapData mapData = new MapData(m_data, x_dim, y_dim, 50, 50);
        
        // MapPanel is the Swing component that actually displays the gameboard.
        m_mapPanel = new MapPanel(mapData);

        // This label will display whose turn it is
        m_status = new JLabel(" ");
        m_status.setAlignmentX(Component.CENTER_ALIGNMENT);
        m_status.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // This label will display any error messages
        m_error = new JLabel(" ");
        m_error.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // We need somewhere to put the map panel, status label, and error label
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(m_mapPanel);
        mainPanel.add(m_status);
        mainPanel.add(m_error);        
        this.setContentPane(mainPanel);

        // Set up the menu bar and window title
        this.setJMenuBar(new KingsTableMenu(this));
        //this.setJMenuBar(new BasicGameMenuBar(this));
        this.setTitle(m_game.getData().getGameName());
        
        // If a user tries to close the window, we want to know that
        this.addWindowListener(WINDOW_LISTENER);
        
        // Resize the window, then make it visible
        this.pack();
        this.setVisible(true);
    }
    
    
    //  If a user tries to close the window, treat it as if they have asked to leave the game
    private WindowListener WINDOW_LISTENER = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e)
        {
            leaveGame();
        }
    };
    
    /**
     * Update the user interface based on a game play.
     * 
     * @param start <code>Territory</code> where the moving piece began
     * @param end <code>Territory</code> where the moving piece ended
     * @param captured <code>Collection</code> of <code>Territory</code>s whose pieces were captured during the play
     */
    public void performPlay(Territory start, Territory end, Collection<Territory> captured)
    {
        m_mapPanel.performPlay(start,end,captured);
    }
    
    public PlayData waitForPlay(final PlayerID player, final IPlayerBridge bridge) 
    {
        return m_mapPanel.waitForPlay(player,bridge);
    }
    
    public IGame getGame()
    {
        return m_game;
    }
    
    public void leaveGame() 
    {
        int rVal = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave?\nUnsaved game data will be lost.", "Exit" , JOptionPane.YES_NO_OPTION);
        if(rVal != JOptionPane.OK_OPTION)
            return;
        
        if(m_game instanceof ServerGame)
        {
            ((ServerGame) m_game).stopGame();
        }
        else
        {   
            m_game.getMessenger().shutDown();
            ((ClientGame) m_game).shutDown();
            
            //an ugly hack, we need a better
            //way to get the main frame
            MainFrame.getInstance().clientLeftGame();
        }
    }
    
    public void stopGame()
    {        

        if(GameRunner.isMac()) 
        {
            //this frame should not handle shutdowns anymore
            MacWrapper.unregisterShutdownHandler();
        }
        this.setVisible(false);
        this.dispose();
    
        
        m_game = null;

        if(m_data != null)
            m_data.clearAllListeners();
        m_data = null;
              
        m_mapPanel = null;

        m_status = null;
                
        removeWindowListener(WINDOW_LISTENER);
        WINDOW_LISTENER = null;

    }
   
    public void shutdown()
    {   
        if (!m_gameOver)
        {
            int rVal = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?\nUnsaved game data will be lost.", "Exit" , JOptionPane.YES_NO_OPTION);
            if(rVal != JOptionPane.OK_OPTION)
                return;
        }
        System.exit(0);
    }
    
    public void setGameOver(boolean gameOver)
    {
        m_gameOver = gameOver;
    }
    
    public void notifyError(String error)
    {
        m_error.setText(error);
    }
    
    public void setStatus(String status)
    {
        m_error.setText(" ");
        m_status.setText(status);
    }

}
