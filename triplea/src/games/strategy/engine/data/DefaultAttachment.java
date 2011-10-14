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
 * Attatchment.java
 *
 * Created on November 8, 2001, 3:09 PM
 */

package games.strategy.engine.data;

import java.lang.reflect.Field;



/**
 * Contains some utility methods that subclasses can use to make writing attatchments easier
 *
 * @author  Sean Bridges
 */
public class DefaultAttachment implements IAttachment
{
    
    private GameData m_data;
    private Attachable m_attatchedTo;
    private String m_name;
    
    /**
     * Throws an error if format is invalid.
     */
    protected 	static int getInt(String aString)
    {
        int val = 0;
        try
        {
            val = Integer.parseInt(aString);
        } catch( NumberFormatException nfe)
        {
            throw new IllegalArgumentException(aString + " is not a valid int value");
        }
        return val;
    }
    
    /**
     * Throws an error if format is invalid.  Must be either true or false ignoring case.
     */
    protected static boolean getBool(String aString)
    {
        if(aString.equalsIgnoreCase("true") )
            return true;
        else if(aString.equalsIgnoreCase("false"))
            return false;
        else
            throw new IllegalArgumentException(aString + " is not a valid boolean");
    }

	public String getRawProperty(String property) {
		String s = "";
		try {
			Field field = getClass().getDeclaredField("m_" + property);
			field.setAccessible(true);
			s += field.get(this);
		} catch (Exception e) {
			throw new IllegalStateException("No such Property: m_" + property);
		}
		return s;
	}
    
    @Override
	public void setData(GameData data)
    {
        m_data = data;
    }
    
    protected GameData getData()
    {
        return m_data;
    }
    
    /**
     * Called after the attatchment is created.
     */
    @Override
	public void validate(GameData data) throws GameParseException
    {
    }
    
    @Override
	public Attachable getAttatchedTo()
    {
        return m_attatchedTo;
    }
    
    @Override
	public void setAttatchedTo(Attachable attatchable)
    {
        m_attatchedTo = attatchable;
    }
    
    
    /** Creates new Attatchment */
    public DefaultAttachment()
    {
        
    }
    @Override
	public String getName()
    {
        return m_name;
    }
    
    @Override
	public void setName(String aString)
    {
        m_name = aString;
    }
    
    
    @Override
	public String toString()
    {
        return getClass().getSimpleName() + " attched to:" + m_attatchedTo + " with name:" + m_name;
    }
    
    
}
