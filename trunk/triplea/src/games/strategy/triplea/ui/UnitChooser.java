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
* UnitChooser.java
*
* Created on December 3, 2001, 7:32 PM
 */

package games.strategy.triplea.ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import games.strategy.ui.*;
import games.strategy.util.IntegerMap;
import games.strategy.engine.data.*;
import games.strategy.triplea.image.*;
import games.strategy.triplea.image.*;
import games.strategy.triplea.util.*;

/**
 *
 * @author  Sean Bridges
 * @version 1.0
 */
public class UnitChooser extends JPanel
{
  private List m_entries = new ArrayList();
  private Map m_dependents = new HashMap();
  private JTextArea m_title;
  private int m_total = -1;
  private JLabel m_leftToSelect = new JLabel();
  private GameData m_data;
  private boolean m_allowTwoHit = false;

  /** Creates new UnitChooser */
  public UnitChooser(Collection units, Map dependent, IntegerMap movement, GameData data)
  {
    m_dependents = dependent;
    m_data = data;
    createEntries(units, dependent, movement);
    layoutEntries();
  }

  public UnitChooser(Collection units, Map dependent, GameData data, boolean allowTwoHit)
  {
    m_dependents = dependent;
    m_data = data;
    m_allowTwoHit = allowTwoHit;
    createEntries(units, dependent, null);
    layoutEntries();
  }

  /**
   * Set the maximum number of units that we can choose.
   */
  public void setMax(int max)
  {
    m_total = max;
    m_textFieldListener.changedValue(null);
  }

  public void setTitle(String title)
  {
    m_title.setText(title);
  }

  private void updateLeft()
  {
    if(m_total == -1)
      return;

    int selected = 0;
    Iterator iter = m_entries.iterator();
    while(iter.hasNext())
    {
      ChooserEntry entry = (ChooserEntry) iter.next();
      selected += entry.getTotalHits();
    }

    m_leftToSelect.setText("Left to select:" + (m_total - selected));

    iter = m_entries.iterator();
    while(iter.hasNext())
    {
      ChooserEntry entry = (ChooserEntry) iter.next();
      entry.setLeftToSelect(m_total - selected);
    }

    m_leftToSelect.setText("Left to select:" + (m_total - selected));

  }



  private void createEntries(Collection units, Map dependent, IntegerMap movement)
  {
    Collection categories = UnitSeperator.categorize(units, dependent, movement);
    Iterator iter = categories.iterator();
    while(iter.hasNext())
    {
      UnitCategory category = (UnitCategory) iter.next();
      addCategory(category);
    }
  }

  private void addCategory(UnitCategory category)
  {
    ChooserEntry entry = new ChooserEntry(category, m_textFieldListener, m_data, m_allowTwoHit);
    m_entries.add(entry);


  }

  private void layoutEntries()
  {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    m_title = new JTextArea("Choose units");
    m_title.setBackground(this.getBackground());
    m_title.setEditable(false);
    m_title.setColumns(15);
    m_title.setWrapStyleWord(true);
    this.add(m_title);
    Iterator iter = m_entries.iterator();
    while(iter.hasNext())
    {
      addEntry( (ChooserEntry) iter.next());
    }
    JPanel leftToSelectPanel = new JPanel();
    leftToSelectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    leftToSelectPanel.add(m_leftToSelect);
    add(leftToSelectPanel);
  }


  private void addEntry(ChooserEntry entry)
  {
      Box box = new Box(BoxLayout.X_AXIS);

      box.add(entry);
      box.add(box.createHorizontalGlue());
      add(box);

  }

  public Collection getSelected()
  {
    return getSelected(true);
  }

  /**
   * get the units selected.
   * If units are two hit enabled, returns those with two hits.
   */
  public List getSelected(boolean selectDependents)
  {
    List selectedUnits = new ArrayList();

    Iterator entries = m_entries.iterator();
    while(entries.hasNext())
    {
      ChooserEntry chooserEntry = (ChooserEntry) entries.next();
      if(chooserEntry.isTwoHit())
          addToCollection(selectedUnits, chooserEntry,  chooserEntry.getSecondHits(), selectDependents);
      else
          addToCollection(selectedUnits, chooserEntry, chooserEntry.getFirstHits(), selectDependents);

    }
    return selectedUnits;
  }

  /**
   * Only applicable if this dialog was constructed using twoHits
   */
  public List getSelectedFirstHit()
  {
      List selectedUnits = new ArrayList();

      Iterator entries = m_entries.iterator();
      while(entries.hasNext())
      {
        ChooserEntry chooserEntry = (ChooserEntry) entries.next();
        if(chooserEntry.isTwoHit())
            addToCollection(selectedUnits, chooserEntry, chooserEntry.getFirstHits(), false);
      }
      return selectedUnits;
  }

  private void addToCollection(Collection addTo, ChooserEntry entry, int quantity, boolean addDependents)
  {

    Collection possible = entry.getCategory().getUnits();
    if(possible.size() < quantity)
      throw new IllegalStateException("Not enough units");

    Iterator iter = possible.iterator();
    for(int i = 0; i < quantity; i++)
    {
      Unit current = (Unit) iter.next();
      addTo.add(current);
      if(addDependents)
      {
        Collection dependents = (Collection) m_dependents.get(current);
        if(dependents != null)
          addTo.addAll(dependents);
      }
    }
  }

  private ScrollableTextFieldListener m_textFieldListener = new ScrollableTextFieldListener()
  {
    public void changedValue(ScrollableTextField field)
    {
      updateLeft();
    }
  };
}

class ChooserEntry extends JPanel
{
  private final UnitCategory m_category;
  private ScrollableTextField m_hitText;
  private final ScrollableTextFieldListener m_hitTextFieldListener;
  private final GameData m_data;
  private final boolean m_hasSecondHit;

  private ScrollableTextField m_secondHitText;
  private ScrollableTextFieldListener m_secondHitTextField;

  private JLabel m_secondHitLabel;
  private int m_leftToSelect = 0;


  ChooserEntry(UnitCategory category, ScrollableTextFieldListener listener, GameData data, boolean allowTwoHit)
  {

    m_hitTextFieldListener = listener;
    m_data = data;
    m_category = category;
    m_hasSecondHit = allowTwoHit && category.isTwoHit() && ! category.getDamaged();

    createComponents(category);

  }

  private void createComponents(UnitCategory category)
  {

      add(new UnitChooserEntryIcon(false));
      if (m_category.getMovement() != -1)
          add(new JLabel("mvt " + m_category.getMovement()));

       add(new JLabel("x" + m_category.getUnits().size()));

      m_hitText = new ScrollableTextField(0, category.getUnits().size());
      m_hitText.addChangeListener(m_hitTextFieldListener);
      add(m_hitText);

      if (m_hasSecondHit)
      {
          add(new UnitChooserEntryIcon(true));

          m_secondHitLabel = new JLabel("x0 ");

          m_secondHitText = new ScrollableTextField(0, 0);
          add(m_secondHitLabel);
          add(m_secondHitText);

          m_hitText.addChangeListener(
              new ScrollableTextFieldListener()
          {
              public void changedValue(ScrollableTextField field)
              {
                  m_secondHitLabel.setText("x" + field.getValue());
                  updateLeftToSelect();
              }
          }
          );

           m_secondHitText.addChangeListener(m_hitTextFieldListener);
      }
  }

  public UnitCategory getCategory()
  {
    return m_category;
  }


  public void setLeftToSelect(int leftToSelect)
  {
    m_leftToSelect = leftToSelect;
    updateLeftToSelect();

  }

  private void updateLeftToSelect()
  {
      int newMax = m_leftToSelect + m_hitText.getValue();

      m_hitText.setMax(Math.min(newMax, m_category.getUnits().size()));

      if (m_hasSecondHit)
      {
          int newSecondHitMax = m_leftToSelect + m_secondHitText.getValue();
          m_secondHitText.setMax(Math.min(newSecondHitMax, m_hitText.getValue()));
      }
  }


  public int getTotalHits()
  {
      return getFirstHits() + getSecondHits();
  }

  public int getFirstHits()
  {
      return m_hitText.getValue();
  }

  public int getSecondHits()
  {
      if(!m_hasSecondHit)
          return 0;
      return m_secondHitText.getValue();
  }

  public boolean isTwoHit()
  {
      return m_hasSecondHit;
  }

  class UnitChooserEntryIcon extends JComponent
  {
      private boolean m_forceDamaged;

    UnitChooserEntryIcon(boolean forceDamaged)
    {
        m_forceDamaged = forceDamaged;
    }



    public void paint(Graphics g)
    {
      super.paint(g);
      g.drawImage( UnitIconImageFactory.instance().getImage(m_category.getType(), m_category.getOwner(), m_data, m_forceDamaged || m_category.getDamaged()), 0,0,this);
      Iterator iter = m_category.getDependents().iterator();
      int index = 1;
      while(iter.hasNext())
      {
        UnitOwner holder = (UnitOwner) iter.next();
        int x = UnitIconImageFactory.UNIT_ICON_WIDTH * index;
        Image unitImg = UnitIconImageFactory.instance().getImage(holder.getType(), holder.getOwner(), m_data, false);
        g.drawImage(unitImg, x, 0, this);


        index++;
      }
    }

    public int getWidth()
    {
      //we draw a unit symbol for each dependent
      return UnitIconImageFactory.UNIT_ICON_WIDTH * (1 + m_category.getDependents().size());
    }

    public int getHeight()
    {
      return UnitIconImageFactory.UNIT_ICON_HEIGHT;
    }

    public Dimension getMaximumSize()
    {
      return getDimension();
    }

    public Dimension getMinimumSize()
    {
      return getDimension();
    }

    public Dimension getPreferredSize()
    {
      return getDimension();
    }

    public Dimension getDimension()
    {
      return new Dimension(getWidth(), getHeight());
    }
  }
}

