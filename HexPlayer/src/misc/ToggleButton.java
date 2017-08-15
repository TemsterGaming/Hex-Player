package misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ToggleButton extends Panel implements MouseListener
{
	private boolean state;
	private int outline;
	private Color buttonOnColor;
	private Color buttonOffColor;

	public ToggleButton()
	{
		addMouseListener(this);
		setOnColor(Color.green);
		setOffColor(Color.gray);
		setBackground(Color.black);
	}
	
	public ToggleButton(int outline)
	{
		this();
		this.outline = outline;
	}
	
	public ToggleButton(int outline,Color buttonOnColor,Color buttonOffColor)
	{
		this(outline);
		setOnColor(buttonOnColor);
		setOffColor(buttonOffColor);
	}
	
	public ToggleButton(int outline,Color buttonOnColor,Color buttonOffColor,Color bg)
	{
		this(outline,buttonOnColor,buttonOffColor);
		setBackground(bg);
	}

	public void paint(Graphics g)
	{
		if(state)
		{
			g.setColor(buttonOnColor);
		}
		else
		{
			g.setColor(buttonOffColor);
		}
		g.fillRect(outline, outline, getWidth() - (outline * 2), getHeight() - (outline * 2));
	}
	
	public boolean getState()
	{
		return state;
	}

	public void toggle()
	{
		state = !state;
	}
	
	public void setOnColor(Color color)
	{
		buttonOnColor = color;
	}
	
	public void setOffColor(Color color)
	{
		buttonOffColor = color;
	}

	public void mouseClicked(MouseEvent e)
	{
		toggle();
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}
}
