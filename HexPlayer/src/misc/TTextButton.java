package misc;

import java.awt.Color;
import java.awt.Graphics;

public class TTextButton extends TBasicButton
{
	String text;
	Color textColor;
	
	public TTextButton(int startX,int startY,int endX,int endY,String text)
	{
		super(startX,startY,endX,endY);
		this.text = text;
		this.textColor = Color.black;
	}
	
	public TTextButton(int startX,int startY,int endX,int endY,String text,Color textColor)
	{
		this(startX,startY,endX,endY,text);
		this.textColor = textColor;
	}
	public TTextButton(int startX,int startY,int endX,int endY,String text,Color textColor,Color bgColor)
	{
		super(startX,startY,endX,endY,bgColor);
		this.text = text;
		this.textColor = textColor;
	}
	public TTextButton(int startX,int startY,int endX,int endY,String text,Color textColor,Color bgColor,Color outlineColor)
	{
		super(startX,startY,endX,endY,bgColor,outlineColor);
		this.text = text;
		this.textColor = textColor;
	}
	
	public void drawButton(Graphics g)
	{
		super.drawButton(g);
		final Color colour = g.getColor();
		g.setColor(textColor);
		g.drawString(text, (x[0] + (width / 2)) - (getTextWidth(g) / 2), y[0] + (height / 2) + (getTextHeight(g) / 2));
		g.setColor(colour);
	}
	
	private int getTextWidth(Graphics g)
	{
		return g.getFontMetrics().stringWidth(text);
	}

	private int getTextHeight(Graphics g)
	{
		return g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent();
	}
}
