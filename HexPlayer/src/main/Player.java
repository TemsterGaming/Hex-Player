package main;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

import javax.imageio.ImageIO;

import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import net.coobird.thumbnailator.Thumbnails;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Images;

public class Player
{
	private boolean playing, songPopup, shuffle, paused;
	boolean seekable;
	private File[] files;
	private int songNum = 0, popupTimer = 0, id = 0, selectedSong;
	private Random rando = new Random();
	private String currentSongName = "No Song Playing";
	private String[] selectedSongName;
	private InputStream sound;
	private AdvancedPlayer ap;
	private PBListener listener;
	private MP3File[] selectedMP3File;
	private Tag currentSongTag;
	private ID3v24Tag currentSongV24Tag;
	private ID3v24Tag[] selectedSongV24Tag;
	private BufferedImage noArt, artwork, savedArtwork, resizedArtwork;
	private int[] artworkSizeInfo = new int[] { 0, 0 };
	private PlayerTimer timer;
	private float gain;
	private Equalizer equalizer;

	public Player(File[] file)
	{
		listener = new PBListener();
		setFiles(file);
		try
		{
			sound = new FileInputStream(files[songNum]);
			ap = new AdvancedPlayer(sound);
			ap.setPlayBackListener(listener);
		}
		catch(FileNotFoundException | JavaLayerException e)
		{
			e.printStackTrace();
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.err.println("ERROR! Can't select any files!");
			e.getStackTrace();
		}
		try
		{
			noArt = ImageIO.read(getClass().getClassLoader().getResourceAsStream("noArt.png"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		selectedSongName = new String[files.length];
		selectedMP3File = new MP3File[files.length];
		selectedSongV24Tag = new ID3v24Tag[files.length];
		updateSelectedSongTags();
		updateCurrentSongTag();
		timer = new PlayerTimer();
		timer.stopTimer();
		gain = 0;
		equalizer = new Equalizer();
	}

	public Player(File[] file, int id)
	{
		this(file);
		this.id = id;
	}
	
	public void playSongAtTime(int num)
	{
		if(!playing) // Used so it doesn't play the song again
		{			 // and make the original unstoppable
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						updateSong();
						System.out.println("Playing Song");
						playing = true;
						paused = false;
						timer.startTimer(num*26);
						ap.setGain(gain);
						ap.setEqualizer(equalizer);
						ap.play(num,Integer.MAX_VALUE); // Thread pauses until song finishes
					}
					catch(Exception e)
					{
						System.err.println("Error Playing Song!");
						e.printStackTrace();
						playing = false;
					}
				}
			}).start();
		}
		else
		{
			System.err.println("Player Error: Already Playing!");
		}
	}

	public void playSong()
	{
		playSongAtTime(0);
	}

	public void playSelectedSong()
	{
		setSong(selectedSong);
		playSong();
	}

	public void setFiles(File[] file)
	{
		files = file;
	}

	public void stopSong()
	{
		System.out.println("Stopping Song");
		try
		{
			ap.close();
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		playing = false;
		if(timer != null)
		{
			stopTimer();
		}
		currentSongName = "No Song Playing";
	}

	public void pauseSong()
	{
		if(!paused)
		{
			System.out.println("Pausing Song");
			paused = true;
			try
			{
				ap.stop();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
			timer.pauseTimer();
			playing = false;
		}
		else
		{
			System.out.println("Resuming Song");
			paused = false;
			try
			{
				sound = new FileInputStream(files[songNum]);
				ap = new AdvancedPlayer(sound);
				ap.setPlayBackListener(listener);
				updateCurrentSongTag();
				new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							playing = true;
							timer.resumeTimer();
							ap.setGain(gain);
							ap.setEqualizer(equalizer);
							ap.play(timer.getMilliseconds() / 26, Integer.MAX_VALUE);
						}
						catch(JavaLayerException e)
						{
							System.err.println("Error Playing Song");
						}
					}
				}).start();
			}
			catch(Exception e)
			{
				System.err.println("Error Resuming Song");
				e.printStackTrace();
				playing = false;
			}
		}
	}

	public void updateSong() // Reloads the player with a new song
	{
		System.out.println("Updating Song");
		sound = null;
		System.out.println("Running Garbage Collector");
		System.gc();
		try
		{
			System.out.println("Setting Song to " + files[songNum].getName());
			sound = new FileInputStream(files[songNum]);
			ap = new AdvancedPlayer(sound);
			ap.setPlayBackListener(listener);
			updateCurrentSongTag();
		}
		catch(IOException | JavaLayerException e)
		{
			e.printStackTrace();
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.err.println("ERROR! Can't select any files!");
		}
	}

	public void nextSong()
	{
		System.out.println("Selecting Next Song");
		boolean playingTemp = playing;
		stopSong();
		if(shuffle)
		{
			songNum = rando.nextInt((files.length - 1) - 0 + 1) + 0;
		}
		else
		{
			if(songNum == files.length - 1)
			{
				songNum = 0;
			}
			else
			{
				songNum++;
			}
		}
		if(playingTemp)
		{
			playSong();
		}
		selectSong(songNum);
		startPopup();
	}

	public void prevSong()
	{
		System.out.println("Selecting Previous Song");
		boolean playingTemp = playing;
		stopSong();
		if(songNum == 0)
		{
			songNum = files.length - 1;
		}
		else
		{
			songNum--;
		}
		if(playingTemp)
		{
			playSong();
		}
		selectSong(songNum);
		startPopup();
	}

	public void toggleLoop()
	{
		listener.loop = !listener.loop;
		System.out.println("Set loop to " + listener.loop);
	}

	public void toggleShuffle()
	{
		shuffle = !shuffle;
		System.out.println("Set shuffle to " + shuffle);
	}

	public void setSong(int num) // Used to send the files in main to the player
	{
		songNum = num;
	}

	public String getCurrentSong() // Return name of the current song
	{
		return currentSongName;
	}

	public String getSelectedSongName()
	{
		return selectedSongName[selectedSong];
	}

	String getFileName(String f) // Converts to char and returns file
	{									// name without file extension
		String ans = "";
		char[] charA = f.toCharArray();
		for(int i = 0; i < charA.length - 4; i++)
		{
			ans = ans + charA[i];
		}
		return ans;
	}

	public void startPopup() // used to show a popup in the corner
	{						 // to show what song is selected
		if(!songPopup)
		{
			songPopup = true;
			popupTimer = 0;
			new Thread(new Runnable()
			{
				public void run()
				{
					while(songPopup && popupTimer < 300)
					{
						popupTimer++;
						frameRate(60);
					}
					stopPopup();
				}
			}).start();
		}
		else
		{
			popupTimer = 0;
		}
	}

	public void stopPopup()
	{
		songPopup = false;
		popupTimer = 0;
	}

	public boolean isPopup()
	{
		return songPopup;
	}

	public void selectSong(int num)
	{
		selectedSong = num;
	}
	
	public boolean canSeek()
	{
		return seekable;
	}

	void frameRate(int num)
	{
		try
		{
			Thread.sleep(1000 / num);
		}
		catch(InterruptedException e)
		{
			System.err.println("Sleep Exception!");
		}
	}

	public BufferedImage getResizedArtwork(int x, int y)
	{
		try
		{
			if((artworkSizeInfo[0] != x && artworkSizeInfo[1] != y) || savedArtwork != artwork)
			{
				System.out.println("Resizing Image");
				resizedArtwork = Thumbnails.of(artwork).size(x, y).asBufferedImage();
				artworkSizeInfo[0] = x;
				artworkSizeInfo[1] = y;
				savedArtwork = artwork;
				return resizedArtwork;
			}
			else
			{
				return resizedArtwork;
			}
		}
		catch(IOException | NullPointerException e)
		{
			return resizedArtwork;
		}
	}

	public void updateSelectedSongTags()
	{
		for(int i = 0; i < files.length; i++)
		{
			try
			{
				selectedMP3File[i] = (MP3File) AudioFileIO.read(files[i]);
			}
			catch(CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e)
			{
				e.printStackTrace();
			}
			if(selectedMP3File[i].hasID3v2Tag())
			{
				selectedSongV24Tag[i] = selectedMP3File[i].getID3v2TagAsv24();
			}
			else
			{
				selectedSongV24Tag[i] = null;
			}
			if(selectedSongV24Tag[i] != null && !(selectedSongV24Tag[i].getFirst(ID3v24FieldKey.ARTIST).equals("") && selectedSongV24Tag[i].getFirst(ID3v24FieldKey.TITLE).equals("")))
			{
				selectedSongName[i] = selectedSongV24Tag[i].getFirst(ID3v24FieldKey.ARTIST) + " - " + selectedSongV24Tag[i].getFirst(ID3v24FieldKey.TITLE);
			}
			else
			{
				selectedSongName[i] = getFileName(files[i].getName());
			}
		}
	}

	void updateCurrentSongTag()
	{
		currentSongV24Tag = selectedSongV24Tag[songNum];
		if(selectedMP3File[songNum].hasID3v1Tag())
		{
			currentSongTag = selectedMP3File[songNum].getTag();
		}
		else
		{
			currentSongTag = null;
		}
		if(currentSongV24Tag != null && !(currentSongV24Tag.getFirst(ID3v24FieldKey.ARTIST).equals("") && currentSongV24Tag.getFirst(ID3v24FieldKey.TITLE).equals("")))
		{
			currentSongName = currentSongV24Tag.getFirst(ID3v24FieldKey.ARTIST) + " - " + currentSongV24Tag.getFirst(ID3v24FieldKey.TITLE);
		}
		else
		{
			currentSongName = getFileName(files[songNum].getName());
		}
		try
		{
			artwork = Images.getImage(currentSongTag.getFirstArtwork());
		}
		catch(NullPointerException | IOException e)
		{
			artwork = noArt;
		}
	}

	public int getID()
	{
		return id;
	}

	public int[] getCurrentDuration()
	{
		if(timer == null)
		{
			return new int[] { 0, 0 };
		}
		else
		{
			return new int[] { timer.getSeconds(), timer.getMinutes() };
		}
	}

	public String getFormattedDuration()
	{
		String temp = "";
		if(timer != null)
		{
			if(timer.getSeconds() > 9)
			{
				temp = timer.getMinutes() + ":" + timer.getSeconds();
				if(getTotalDuration() % 60 > 9)
				{
					temp = temp + "/" + getTotalDuration() / 60 + ":" + getTotalDuration() % 60;
				}
				else
				{
					temp = temp + "/" + getTotalDuration() / 60 + ":0" + getTotalDuration() % 60;
				}
				return temp;
			}
			else
			{
				temp = timer.getMinutes() + ":0" + timer.getSeconds();
				if(getTotalDuration() % 60 > 9)
				{
					temp = temp + "/" + getTotalDuration() / 60 + ":" + getTotalDuration() % 60;
				}
				else
				{
					temp = temp + "/" + getTotalDuration() / 60 + ":0" + getTotalDuration() % 60;
				}
				return temp;
			}
		}
		else
		{
			return "0:00/0:00";
		}
	}
	
	public int getCurrentMillis()
	{
		if(timer.getMilliseconds()!=0)
		return timer.getMilliseconds();
		else
			return 0;
	}

	public void startTimer()
	{
		timer.startTimer(0);
	}

	void stopTimer()
	{
		timer.stopTimer();
	}
	
	public boolean isPlaying()
	{
		return playing;
	}

	public int getTotalDuration()
	{
		return selectedMP3File[songNum].getMP3AudioHeader().getTrackLength();
	}
	
	public void setGain(float gain)
	{
		this.gain = gain;
		ap.setGain(gain);
	}
	
	/**
	 * @param vol 0 = 0%, 1 = 100%
	 */
	public void setVolume(float vol)
	{
		if(vol !=0)
		{
			setGain((float)(20.0f * Math.log10(vol)));
		}
		else
		{
			setGain(-144.0f);
		}
	}
	
	public void setEqualizer(Equalizer eq)
	{
		equalizer = eq;
		ap.setEqualizer(equalizer);
	}

	class PBListener extends PlaybackListener // Triggers when a song ends
	{
		boolean loop = false;
		
		public void playbackStarted(PlaybackEvent e)
		{
			seekable = true;
		}

		public void playbackFinished(PlaybackEvent e)
		{
			System.out.println("Receieved Finish Event");
			seekable = false;
			if(!paused)
			{
				if(loop)
				{
					playing = false;
					if(timer != null)
					{
						stopTimer();
					}
					currentSongName = "No Song Playing";
					playSong();
				}
				else
				{
					currentSongName = "No Song Playing";
					nextSong();
				}
			}
			else if(timer.getMilliseconds() > 49)
			{
				timer.setTime(timer.getMilliseconds() - 50);
			}
		}
	}
}