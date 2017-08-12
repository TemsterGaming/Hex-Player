package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class TPlaylist
{
	private BufferedReader bIn;
	private BufferedWriter bOut;
	private File file;
	private int totalSongs;
	private boolean writing;

	private File[] songFile;
	private String[] songName;

	public TPlaylist(File file, boolean writing)
	{
		if(writing)
		{
			this.writing = writing;
			resetWriter(file);
		}
		else
		{
			resetReader(file);
			int num = 0;
			for(int i = 0; i < 200; i++)
			{
				try
				{
					String string = bIn.readLine();
					if(string != null && !string.equals("") && !string.equals(" "))
					{
						num++;
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			totalSongs = num / 2;
			songFile = new File[num / 2];
			songName = new String[num / 2];
			resetReader(this.file);
			String sFile = "";
			String sName = "";
			for(int i = 0; i < num / 2; i++)
			{
				try
				{
					sFile = bIn.readLine();
					songFile[i] = new File(sFile);
					sName = bIn.readLine();
					songName[i] = sName;
				}
				catch(NullPointerException e2)
				{
					System.err.println("ERROR! Unable to find File " + sFile);
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
				}
			}
			System.out.println("Loaded "+file.getName());
		}
	}

	private void resetReader(File file)
	{
		try
		{
			bIn.close();
		}
		catch(NullPointerException e2)
		{
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
		this.file = file;
		try
		{
			bIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public void resetWriter(File file)
	{
		if(writing)
		{
			try
			{
				bOut.close();
			}
			catch(NullPointerException e)
			{
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			this.file = file;
			try
			{
				bOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("This Playlist is Read-Only!");
		}
	}

	public String[] getSongNames()
	{
		return songName;
	}

	public File[] getSongFiles()
	{
		return songFile;
	}

	public int getSongCount()
	{
		return totalSongs;
	}

	public void close()
	{
		if(writing)
		{
			try
			{
				bOut.flush();
				bOut.close();
			}
			catch(NullPointerException | IOException e)
			{
				if(writing)
				{
					System.err.println("Error Closing Writer");
				}
			}
		}
		else
		{
			try
			{
				bIn.close();
			}
			catch(NullPointerException | IOException e)
			{
				System.err.println("Error Closing Reader");
			}
		}
	}

	public void addFile(String fileName)
	{
		if(writing)
		{
			char[] fileChar = fileName.toCharArray();
			try
			{
				bOut.write(fileName);
				bOut.newLine();
				for(int i = 0; i < fileChar.length - 4; i++)
				{
					bOut.write(fileChar[i]);
				}
				bOut.newLine();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
