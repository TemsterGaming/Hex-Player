package misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;

public class TImageButton extends TBasicButton
{
	private BufferedImage image, resizedImage;

	public TImageButton(int startX, int startY, int endX, int endY, BufferedImage image)
	{
		super(startX, startY, endX, endY);

		this.image = image;
		try
		{
			if(height > width)
			{
				resizedImage = Thumbnails.of(this.image).size(height - ((lineGap + 1) * 2), height - ((lineGap + 1) * 2)).asBufferedImage();
			}
			else
			{
				resizedImage = Thumbnails.of(this.image).size(height - ((lineGap + 1) * 2), height - ((lineGap + 1) * 2)).asBufferedImage();
			}
		}
		catch(IOException | IllegalArgumentException e)
		{
			System.err.println("Resizing Failed! The Size of The Button Has To Be At Least 5x5 Pixels When Created");
			try
			{
				resizedImage = Thumbnails.of(this.image).size(512, 512).asBufferedImage();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public TImageButton(int startX, int startY, int endX, int endY, BufferedImage image, Color bgColor)
	{
		this(startX, startY, endX, endY, image);
		this.bgColor = bgColor;
	}

	public TImageButton(int startX, int startY, int endX, int endY, BufferedImage image, Color bgColor, Color outlineColor)
	{
		this(startX, startY, endX, endY, image, bgColor);
		this.outlineColor = outlineColor;
	}

	public void drawButton(Graphics g)
	{
		super.drawButton(g);
			g.drawImage(getResizedImage(), x[0] + (lineGap + 1), y[0] + (lineGap + 1), width - ((lineGap + 1) * 2), height - ((lineGap + 1) * 2), null);
	}

	public BufferedImage getResizedImage()
	{
		if(resizedImageSize[0] != width && resizedImageSize[1] != height)
		{
			try
			{
				resizedImage = Thumbnails.of(image).size(width - ((lineGap + 1) * 2), height - ((lineGap + 1) * 2)).asBufferedImage();
				resizedImageSize[0] = width;
				resizedImageSize[1] = height;
				return resizedImage;
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return resizedImage;
			}
		}
		else
		{
			return resizedImage;
		}
	}
}