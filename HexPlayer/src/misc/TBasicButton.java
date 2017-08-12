package misc;

import java.awt.Color;
import java.awt.Graphics;

public class TBasicButton extends TBlankButton
{
	protected Color bgColor, outlineColor;

	public TBasicButton(int startX, int startY, int endX, int endY)
	{
		this(startX, startY, endX, endY, null);
	}

	public TBasicButton(int startX, int startY, int endX, int endY, Color bgColor)
	{
		super(startX, startY, endX, endY);
		this.bgColor = bgColor;
		this.outlineColor = null;
	}

	public TBasicButton(int startX, int startY, int endX, int endY, Color bgColor, Color outlineColor)
	{
		this(startX, startY, endX, endY, bgColor);
		this.outlineColor = outlineColor;
	}

	public void drawButton(Graphics g)
	{
		final Color colour = g.getColor();
		if(bgColor != null)
		{
			g.setColor(bgColor);
			g.fillRect(x[0], y[0], width, height);
		}
		if(outlineColor!=null)
		{
			g.setColor(outlineColor);
			g.drawRect(x[0] + lineGap, y[0] + lineGap, width - (lineGap * 2) - 1, height - (lineGap * 2) - 1);
		}
		g.setColor(colour);
	}
	
	private void updateButton()
	{
		if(pressed)
		{
			lineGap = 2;
		}
		else
		{
			lineGap = 1;
		}
		x[1] = x[0] + width;
		y[1] = y[0] + height;
	}
	
	public void buttonPressed(int x,int y)
	{
		super.buttonPressed(x,y);
		updateButton();
	}
	
	public boolean buttonReleased(int x,int y)
	{
		pressed = false;
		updateButton();
		return super.buttonReleased(x,y);
	}
	
	public void setSize(int x,int y)
	{
		super.setSize(x,y);
		updateButton();
	}
}