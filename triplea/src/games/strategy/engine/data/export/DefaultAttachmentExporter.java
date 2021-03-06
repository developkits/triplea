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
 * AbstractAttachmentExporter.java
 * 
 * Created on May 29, 2011, 12:00 PM by Edwin van der Wal
 */
package games.strategy.engine.data.export;

import games.strategy.engine.data.IAttachment;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.UnitType;
import games.strategy.util.IntegerMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Base class for all attachment exporter classes, if you create a new attachment extend this class,
 * and configure the AttachmentExporterFactory to include your new exporter. Or if your new Attachment
 * is very standard you can use this one directly. Configure the same in AttachmentExporterFactory
 * 
 * @see AttachmentExporterFactory
 * @author Edwin van der Wal
 * 
 */
public class DefaultAttachmentExporter implements IAttachmentExporter
{
	/* (non-Javadoc)
	 * @see games.strategy.engine.data.export.IAttachmentExporter#getAttachmentOptions(games.strategy.engine.data.IAttachment)
	 */
	public String getAttachmentOptions(final IAttachment attachment)
	{
		final StringBuffer xmlfile = new StringBuffer();
		final Iterator<Field> fields = Arrays.asList(attachment.getClass().getDeclaredFields()).iterator();
		while (fields.hasNext())
		{
			final Field field = fields.next();
			field.setAccessible(true);
			try
			{
				xmlfile.append(printOption(field, attachment));
			} catch (final AttachmentExportException e)
			{
				System.err.println(e);
			}
		}
		return xmlfile.toString();
	}
	
	/**
	 * @param field
	 *            the field indicating the reference in the attachment to the option.
	 * @param attachment
	 *            the attachment this option is in
	 * @return return an xmlline of the option
	 * @throws AttachmentExportException
	 */
	protected String printOption(final Field field, final IAttachment attachment) throws AttachmentExportException
	{
		try
		{
			final String fieldName = field.getName();
			if (fieldName.startsWith("m_is"))
			{
				// Check for Boolean Options
				// Boolean Option with an setIs-setter java: boolean m_isAir & setIsAir(String) xml: isAir
				try
				{
					attachment.getClass().getMethod("setIs" + fieldName.substring(4), java.lang.String.class);
					return printBooleanOption(field, fieldName.substring(2), attachment);
				} catch (final NoSuchMethodException nsme)
				{ /* not this one */
				}
				try
				{
					// Boolean Option with the set-setter java: boolean m_isFemale & setFemale(String) xml: female
					attachment.getClass().getMethod("set" + Character.toUpperCase(fieldName.charAt(4)) + fieldName.substring(5), java.lang.String.class);
					return printBooleanOption(field, "" + Character.toLowerCase(fieldName.charAt(4)) + fieldName.substring(5), attachment);
				} catch (final NoSuchMethodException nsme)
				{ /* not this one */
				}
			}
			else
			{
				if (fieldName.startsWith("m_"))
				{
					try
					{
						attachment.getClass().getMethod("set" + Character.toUpperCase(fieldName.charAt(2)) + fieldName.substring(3), java.lang.String.class);
						if (field.getType().equals(java.lang.String.class))
						{
							return printStringOption(field, fieldName.substring(2), attachment);
						}
						if (field.getType().equals(java.lang.Boolean.class) || field.getType().equals(boolean.class))
						{
							return printBooleanOption(field, fieldName.substring(2), attachment);
						}
						if (field.getType().equals(java.lang.Integer.class) || field.getType().equals(int.class) || field.getType().equals(long.class) || field.getType().equals(java.lang.Long.class))
						{
							return printIntegerOption(field, fieldName.substring(2), attachment);
						}
						if (field.getType().equals(java.lang.String[].class))
						{
							return printStringArrayOption(field, fieldName.substring(2), attachment);
						}
						if (field.getType().equals(PlayerID.class))
						{
							return printPlayerIDOption(field, fieldName.substring(2), attachment);
						}
						if (field.getType().equals(UnitType.class))
						{
							return printUnitTypeOption(field, fieldName.substring(2), attachment);
						}
						throw new AttachmentExportException("unknown handler for field: " + field + " of class: " + field.getType());
					} catch (final NullPointerException npe)
					{
						System.err.println("npe: field: " + fieldName);
						npe.printStackTrace();
					} catch (final NoSuchMethodException e)
					{
						System.err.println("no method: set" + (Character.toUpperCase(fieldName.charAt(2)) + fieldName.substring(3)) + " found on: " + attachment.getClass().getCanonicalName());
					}
				}
			}
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printOption on field: " + field + " on Attachment: " + attachment.getName());
		}
		return "";
	}
	
	private String printUnitTypeOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		try
		{
			final UnitType value = (UnitType) field.get(attachment);
			if (value == null)
				return "";
			return printDefaultOption(option, value.getName());
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printPlayerIDOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printPlayerIDOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
	}
	
	private String printPlayerIDOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		PlayerID value;
		try
		{
			value = (PlayerID) field.get(attachment);
			if (value == null)
				return "";
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printPlayerIDOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printPlayerIDOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
		return printDefaultOption(option, value.getName());
	}
	
	private String printStringArrayOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		String[] valueArray;
		try
		{
			valueArray = (String[]) field.get(attachment);
			if (valueArray == null)
				return "";
			final Iterator<String> values = Arrays.asList(valueArray).iterator();
			String value = values.next();
			while (values.hasNext())
			{
				value = value + ":" + values.next();
			}
			return printDefaultOption(option, value);
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printStringArrayOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printStringArrayOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
	}
	
	protected String printIntegerOption(final Field field, final String option, final IAttachment attachment, final boolean printDefaultValue) throws AttachmentExportException
	{
		int value;
		try
		{
			value = field.getInt(attachment);
			// don't set default values
			final IAttachment defaultAttachment = attachment.getClass().newInstance();
			final int defaultValue = field.getInt(defaultAttachment);
			if (defaultValue != value || printDefaultValue)
				return printDefaultOption(option, "" + value);
			else
				return "";
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printIntegerOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printIntegerOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final InstantiationException e)
		{
			throw new AttachmentExportException("e: " + e + " for printIntegerOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
	}
	
	protected String printIntegerOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		return printIntegerOption(field, option, attachment, false);
	}
	
	protected String printStringOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		return printStringOption(field, option, attachment, false);
	}
	
	protected String printStringOption(final Field field, final String option, final IAttachment attachment, final boolean printDefaultValue) throws AttachmentExportException
	{
		String value;
		try
		{
			value = (String) field.get(attachment);
			if (value == null)
				return "";
			// don't set default values
			final IAttachment defaultAttachment = attachment.getClass().newInstance();
			final String defaultValue = (String) field.get(defaultAttachment);
			if (value.equals(defaultValue) && !printDefaultValue)
				return "";
			else
				return printDefaultOption(option, "" + value);
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printStringOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printStringOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final InstantiationException e)
		{
			throw new AttachmentExportException("e: " + e + " for printStringOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
	}
	
	protected String printBooleanOption(final Field field, final String option, final IAttachment attachment) throws AttachmentExportException
	{
		return printBooleanOption(field, option, attachment, false);
	}
	
	protected String printBooleanOption(final Field field, final String option, final IAttachment attachment, final boolean printDefaultValue) throws AttachmentExportException
	{
		boolean value = false;
		try
		{
			value = field.getBoolean(attachment);
			// don't set default values
			final IAttachment defaultAttachment = attachment.getClass().newInstance();
			final boolean defaultValue = field.getBoolean(defaultAttachment);
			if (value == defaultValue && !printDefaultValue)
				return "";
			else
				return printDefaultOption(option, "" + value);
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for printBooleanOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for printBooleanOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		} catch (final InstantiationException e)
		{
			throw new AttachmentExportException("e: " + e + " for printBooleanOption on field: " + field + " option: " + option + " on Attachment: " + attachment.getName());
		}
	}
	
	protected String printDefaultOption(final String option, final String value)
	{
		return "            <option name=\"" + option + "\" value=\"" + value + "\"/>\n";
	}
	
	protected String printCountOption(final String option, final String value, final String count)
	{
		return "            <option name=\"" + option + "\" value=\"" + value + "\" count=\"" + count + "\"/>\n";
	}
	
	@SuppressWarnings("unchecked")
	protected String printPlayerList(final Field field, final IAttachment attachment) throws AttachmentExportException
	{
		try
		{
			final ArrayList<PlayerID> playerIds = (ArrayList<PlayerID>) field.get(attachment);
			final Iterator<PlayerID> iplayerIds = playerIds.iterator();
			String returnValue = "";
			if (iplayerIds.hasNext())
				returnValue = iplayerIds.next().getName();
			while (iplayerIds.hasNext())
			{
				returnValue += ":" + iplayerIds.next().getName();
			}
			final String optionName = "" + Character.toLowerCase(field.getName().charAt(2)) + field.getName().substring(3);
			if (returnValue.length() > 0)
				return printDefaultOption(optionName, returnValue);
			return "";
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for mPlayersHandler on field: " + field.getName() + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for mPlayersHandler on field: " + field.getName() + " on Attachment: " + attachment.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected String printUnitIntegerMap(final Field field, final IAttachment attachment) throws AttachmentExportException
	{
		try
		{
			final String optionName = "" + Character.toLowerCase(field.getName().charAt(2)) + field.getName().substring(3);
			final IntegerMap<UnitType> map = (IntegerMap<UnitType>) field.get(attachment);
			String returnValue = "";
			if (map == null)
				return "";
			final Iterator<UnitType> types = map.keySet().iterator();
			while (types.hasNext())
			{
				final UnitType type = types.next();
				final int number = map.getInt(type);
				if (type == null)
					returnValue += printCountOption(optionName, "ANY", "" + number);
				else
					returnValue += printCountOption(optionName, type.getName(), "" + number);
			}
			return returnValue;
		} catch (final IllegalArgumentException e)
		{
			throw new AttachmentExportException("e: " + e + " for mUnitPresenceHandler on field: " + field.getName() + " on Attachment: " + attachment.getName());
		} catch (final IllegalAccessException e)
		{
			throw new AttachmentExportException("e: " + e + " for mUnitPresenceHandler on field: " + field.getName() + " on Attachment: " + attachment.getName());
		} catch (final java.lang.ArrayIndexOutOfBoundsException e)
		{
			throw new AttachmentExportException("e: " + e + " for mUnitPresenceHandler on field: " + field.getName() + " on Attachment: " + attachment.getName());
		}
	}
}
