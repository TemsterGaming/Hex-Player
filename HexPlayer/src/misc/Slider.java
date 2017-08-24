package misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Slider extends Panel implements MouseListener, MouseMotionListener
{
	private float value;
	private float total;
	private boolean dragging;
	private boolean clicked;
	private Color sliderColor;
	private int outline;
	private boolean vertical;
	private boolean leftOnly;

	public Slider()
	{
		outline = 0;
		value = 0;
		total = 100;
		setBackground(Color.black);
		addMouseListener(this);
		addMouseMotionListener(this);
		sliderColor = Color.green;
	}

	public Slider(int outline)
	{
		this();
		this.outline = outline;
	}

	public void paint(Graphics g)
	{
		g.setColor(sliderColor);
		if(vertical)
		{
			g.fillRect(outline, getHeight() - (int) (value / total * getHeight()) + outline, getWidth() - (outline * 2), (int) (value / total * getHeight()) - (outline * 2));
		}
		else
		{
			g.fillRect(outline, outline, (int) (value / total * getWidth()) - (outline * 2), getHeight() - (outline * 2));
		}
	}

	public void setValue(float value)
	{
		this.value = value;
		repaint();
	}

	public void setTotal(float total)
	{
		this.total = total;
		repaint();
	}

	public void stopDragging()
	{
		dragging = false;
	}

	public float getValue()
	{
		return value;
	}

	public boolean isDragging()
	{
		return dragging;
	}

	public boolean isClick()
	{
		return clicked;
	}

	public void setSliderColor(Color color)
	{
		sliderColor = color;
	}

	public void setVertical(boolean vert)
	{
		vertical = vert;
	}

	public void setLeftOnly(boolean left)
	{
		leftOnly = left;
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		clicked = true;
		dragging = true;
	}

	public void mouseReleased(MouseEvent e)
	{
		if(e.getButton() != MouseEvent.BUTTON1 && leftOnly)
		{
		}
		else
		{
			if(isClick())
			{
				if(vertical)
				{
					value = (getHeight() - e.getY()) * (total / getHeight());
				}
				else
				{
					value = e.getX() * (total / getWidth());
				}
				if(value > total)
				{
					value = total;
				}
				else if(value < 0)
				{
					value = 0;
				}
			}
			repaint();
		}
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
		clicked = false;
		if(!MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Button1") && leftOnly)
		{
		}
		else
		{
			if(vertical)
			{
				value = (getHeight() - e.getY()) * (total / getHeight());
			}
			else
			{
				value = e.getX() * (total / getWidth());
			}
			if(value > total)
			{
				value = total;
			}
			else if(value < 0)
			{
				value = 0;
			}
		}
		repaint();
	}
}
