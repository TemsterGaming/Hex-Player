package misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Slider extends Panel implements MouseListener, MouseMotionListener
{
	private float progress;
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
		progress = 0;
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
			g.fillRect(outline, getHeight() - (int) (progress / total * getHeight()) + outline, getWidth() - (outline * 2), (int) (progress / total * getHeight()) - (outline * 2));
		}
		else
		{
			g.fillRect(outline, outline, (int) (progress / total * getWidth()) - (outline * 2), getHeight() - (outline * 2));
		}
	}

	public void setProgress(float progress)
	{
		this.progress = progress;
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

	public float getProgress()
	{
		return progress;
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
					progress = (getHeight() - e.getY()) * (total / getHeight());
				}
				else
				{
					progress = e.getX() * (total / getWidth());
				}
				if(progress > total)
				{
					progress = total;
				}
				else if(progress < 0)
				{
					progress = 0;
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
				progress = (getHeight() - e.getY()) * (total / getHeight());
			}
			else
			{
				progress = e.getX() * (total / getWidth());
			}
			if(progress > total)
			{
				progress = total;
			}
			else if(progress < 0)
			{
				progress = 0;
			}
		}
		repaint();
	}
}
