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
 * MapPanel.java
 * 
 * Created on October 30, 2001, 2:02 PM
 */
package games.strategy.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * 
 * @author Sean Bridges
 * @version 1.0
 * 
 *          An image that can be zoomed in and out.
 * 
 *          Warning not finished. Thats why its not public.
 *          Development was abandoned when it turned out when it appeared that zooming would be
 *          too slow.
 * 
 */
class ZoomableImage extends JComponent
{
	private static final long serialVersionUID = 3324558973854619020L;
	private double m_zoom = 1.0;
	private final Image m_original;
	private Image m_current;
	
	/** Creates new MapPanel */
	public ZoomableImage(final Image anImage)
	{
		ensureImageLoaded(anImage);
		m_original = anImage;
		m_current = anImage;
		final Dimension dim = new Dimension(m_current.getWidth(this), m_current.getHeight(this));
		this.setPreferredSize(dim);
	}
	
	private void ensureImageLoaded(final Image anImage)
	{
		if (anImage.getWidth(this) != -1)
			return;
		final MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(anImage, 1);
		try
		{
			tracker.waitForAll(1);
		} catch (final InterruptedException ie)
		{
			ie.printStackTrace();
			System.err.println("<<<<<<<<<<<<<<<<<<<<<<>trying again to load Image");
			ensureImageLoaded(anImage);
		}
	}
	
	public double getZoom()
	{
		return m_zoom;
	}
	
	public void setZoom(final double newZoom)
	{
		if (newZoom <= 1.0)
			throw new IllegalArgumentException("Zoom must be > 1.  Got:" + newZoom);
		m_zoom = newZoom;
		stretchImage();
		final Dimension dim = new Dimension(m_current.getWidth(this), m_current.getHeight(this));
		this.setPreferredSize(dim);
		this.invalidate();
	}
	
	private void stretchImage()
	{
		final int width = (int) (m_original.getWidth(this) * m_zoom);
		final int height = (int) (m_original.getHeight(this) * m_zoom);
		m_current = m_original.getScaledInstance(width, height, Image.SCALE_FAST);
	}
	
	@Override
	public void paint(final Graphics g)
	{
		// draw the image
		g.drawImage(m_current, 0, 0, this);
		final Rectangle rect = g.getClipBounds();
		if (rect.getWidth() > m_current.getWidth(this) || rect.getHeight() > m_current.getHeight(this))
		{
			// we are being asked to draw on a canvas bigger than the image
			// clear the rest of the canvas
			// TODO deal with this.
		}
	}
	
	public void addComponent(final Component comp)
	{
		throw new IllegalStateException("Cannot add componenets to an image panel");
	}
}
