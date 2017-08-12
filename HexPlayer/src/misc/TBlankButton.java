package misc;

public class TBlankButton
{
	protected int[] x = new int[2];
	protected int[] y = new int[2];
	protected int[] clickPos = new int[2];
	protected int width;
	protected int height;
	protected int lineGap = 1;
	protected boolean pressed = false;
	protected int[] resizedImageSize = new int[2];

	public TBlankButton(int startX, int startY, int endX, int endY)
	{
		x[0] = startX;
		x[1] = endX;
		y[0] = startY;
		y[1] = endY;
		width = x[1] - x[0];
		height = y[1] - x[0];
	}

	public boolean isInBounds(int x, int y)
	{
		if(x > this.x[0] && x < this.x[1] && y > this.y[0] && y < this.y[1])
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public int[] getBounds()
	{
		return new int[] { x[0], x[1], y[0], y[1] };
	}

	public void setBounds(int startX, int startY, int endX, int endY)
	{
		x[0] = startX;
		x[1] = endX;
		y[0] = startY;
		y[1] = endY;
		width = x[1] - x[0];
		height = y[1] - x[0];
	}

	public void setLocation(int x, int y)
	{
		this.x[0] = x;
		this.y[0] = y;
	}

	public void setSize(int x, int y)
	{
		width = x;
		height = y;
	}
	
	public void buttonPressed(int x, int y)
	{
		if(x > this.x[0] && x < this.x[1] && y > this.y[0] && y < this.y[1])
		{
			pressed = true;
			clickPos[0] = x;
			clickPos[1] = y;
		}
	}

	public boolean buttonReleased(int x, int y)
	{
		pressed = false;
		if(x > this.x[0] && x < this.x[1] && y > this.y[0] && y < this.y[1])
		{
			if((x < clickPos[0] + 5 && x > clickPos[0] - 5) && (y < clickPos[1] + 5 && y > clickPos[1] - 5))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}

	}
}