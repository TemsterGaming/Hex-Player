/*
 *Credit goes to Appzgear for the images on the buttons
 *http://www.flaticon.com/authors/appzgear
 */

package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import javazoom.jl.decoder.Equalizer;
import misc.Slider;
import misc.TImageButton;
import misc.ToggleButton;

public class Main
{
	FilenameFilter mp3Filter = new FilenameFilter() // Used to filter out files/folders that don't end in ".mp3"
	{
		public boolean accept(File f, String s)
		{
			if (s.endsWith(".mp3"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	FilenameFilter plFilter = new FilenameFilter() // Used to filter out files/folders that don't end in ".pl"
	{
		public boolean accept(File f, String s)
		{
			if (s.endsWith(".pl"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	private VolumeChanger volChanger; // equalizer/volume window
	private static Frame frame; // main window
	private static Panel mainPanel;
	private static ButtonPanel buttons; // panel for the buttons on the left
	private static SongPanel songPanel; // panel for song selection, seek bar and song info
	private Slider slider; // seek bar
	private Player player; // The music player
	private List[] playlistListMenu; // list of songs in a playlist
	private static TextField playlistNameField; // textfield for setting a playlist's name when creating it
	private static BufferedImage[] buttonPng = new BufferedImage[8]; // all the images for the buttons
	private static TImageButton tPlay, tPause, tStop, tNext, tPrev, tLoop, shuffle, equalizerButton; // all the buttons on the left
	private static List songList, playlistList, playlistNewList, deleteConfirmMenu; // lists for songs/playlists, creating playlists and confirming deletion of a playlist
	private File[] files; // songs
	private static int[] textScroll; // used to make the text of the current playing song scroll across the screen
	private File[] playlistFiles; // all the .pl files
	private TPlaylist[] playlist; // all the playlists
	private File dir, playlistDir; // songs folder and playlist folder
	private File eqFile; // equalizer.eq in the songs folder
	private int selectedForDeletion; // used to keep track of which playlist was selected for deletion
	private static boolean frameReady = false; // used to stop the graphics breaking

	public static void main(String args[])
	{
		new Main();
	}

	public Main()
	{
		frame = new Frame("Hex Player v2.5");
		mainPanel = new Panel();
		init();
		volChanger = new VolumeChanger();
	}

	public void init()
	{
		try
		{
			frame.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("icon.png"))); // sets the icon for the window
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent windowevent) // updates the equalizer and closes the program upon clicked the close button
			{
				volChanger.updateEqFile();
				System.exit(0);
			}
		});
		frame.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e) // resizes all the components when the window changes size
			{
				buttons.setLocation(0, 0);
				buttons.resizePanel((int) (mainPanel.getWidth() / 14.222), mainPanel.getHeight());
				songPanel.setBounds(buttons.getWidth(), 0, mainPanel.getWidth() - buttons.getWidth(), mainPanel.getHeight());
			}
		});

		try
		{
			String dirString = "Songs";
			System.out.println("Setting Directory to: " + dirString);
			dir = new File(dirString);
			if (!dir.exists())
			{
				System.out.println("Songs Folder Not Found, Trying Test Folder"); // using the songs folder in my usb does not work in eclipse, this is a workaround
				dirString = "V:/Songs";
				dir = new File(dirString);
				if (!dir.exists()) // TODO should probably change this to creating the songs directory and display no songs
				{
					System.exit(0);
				}
			}
			playlistDir = new File(dirString + "/Playlists"); // sets the location of the playlists to (jar location)/Songs/Playlists
			if (!playlistDir.exists())
			{
				playlistDir.mkdir(); // creates the playlists folder if it doesn't exist
			}
		}
		catch (java.lang.NullPointerException e)
		{
			System.err.println("ERROR! Directory Not Found!");
			System.exit(0);
		}
		catch (java.security.AccessControlException e)
		{
			System.err.println("ERROR! Access Denied!");
			System.err.println("Either move the program to that directory or choose another directory");
			System.exit(0);
		}

		files = dir.listFiles(mp3Filter); // checks for all .mp3 files
		eqFile = new File(dir.getPath() + "/equalizer.eq"); // sets the location of equalizer.eq to (jar location)/Songs
		try
		{
			eqFile.createNewFile(); // tries to create the equalizer file if it doesn't exist
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}

		try // reads the .png files for the buttons from inside the jar file
		{
			buttonPng[0] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("play.png"));
			buttonPng[1] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("stop.png"));
			buttonPng[2] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("next.png"));
			buttonPng[3] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("prev.png"));
			buttonPng[4] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("loop.png"));
			buttonPng[5] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("shuffle.png"));
			buttonPng[6] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pause.png"));
			buttonPng[7] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("eq.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException e1) // can't remember why this is here, probably if a file is missing
		{
			e1.printStackTrace();
			System.exit(0);
		}

		// creating the buttons and adding the images to them
		tPlay = new TImageButton(0, 0, 45, 45, buttonPng[0], null, Color.black);
		tStop = new TImageButton(0, 0, 45, 45, buttonPng[1], null, Color.black);
		tNext = new TImageButton(0, 0, 45, 45, buttonPng[2], null, Color.black);
		tPrev = new TImageButton(0, 0, 45, 45, buttonPng[3], null, Color.black);
		tLoop = new TImageButton(0, 0, 45, 45, buttonPng[4], null, Color.black);
		shuffle = new TImageButton(0, 0, 45, 45, buttonPng[5], null, Color.black);
		tPause = new TImageButton(0, 0, 45, 45, buttonPng[6], null, Color.black);
		equalizerButton = new TImageButton(0, 0, 45, 45, buttonPng[7], null, Color.black);

		// Setting up player
		System.out.println("Starting Player");
		player = new Player(files);

		// creating and preparing all of the panels
		buttons = new ButtonPanel();
		songPanel = new SongPanel();
		songPanel.setLayout(null);
		mainPanel.setLayout(null);
		frame.add(mainPanel);
		mainPanel.add(buttons);
		mainPanel.add(songPanel);
		frame.pack();
		frame.setSize(656, 399);
		buttons.setPreferredSize(new Dimension(45, 0));
		frame.setVisible(true);
		frameReady = true;

		// loading the playlists
		reloadPlaylists();
	}

	public void frameRate(int num)
	{
		try
		{
			Thread.sleep(1000 / num);
		}
		catch (InterruptedException e)
		{
			System.err.println("Sleep Exception");
		}
	}

	public void reloadPlaylists() // updates the playlists
	{
		playlistFiles = playlistDir.listFiles(plFilter);
		playlist = new TPlaylist[playlistDir.listFiles(plFilter).length];
		playlistListMenu = new List[playlistDir.listFiles(plFilter).length];
		for (int i = 0; i < playlist.length; i++)
		{
			playlist[i] = new TPlaylist(playlistDir.listFiles(plFilter)[i], false);
			playlistListMenu[i] = new List();
			playlistListMenu[i].add("/Back/");
			for (int a = 0; a < playlist[i].getSongCount(); a++)
			{
				playlistListMenu[i].add(playlist[i].getSongNames()[a]);
			}
			playlistListMenu[i].add("/Delete/");
			playlist[i].close();
		}

		playlistList.removeAll();
		playlistList.add("/Back/");
		for (File i : playlistFiles)
		{
			playlistList.add(i.getName());
		}
		playlistList.add("/Add/");
		playlistList.add("/Reload/");

		for (int i = 0; i < playlistListMenu.length; i++)
		{
			songPanel.add(playlistListMenu[i]);
			playlistListMenu[i].addItemListener(songPanel);
			playlistListMenu[i].addActionListener(songPanel);
			playlistListMenu[i].setVisible(false);
		}
	}

	public void createPlaylist() // creates a new playlist
	{
		TPlaylist newPlaylist = new TPlaylist(new File(playlistDir.getPath() + "/" + playlistNameField.getText() + ".pl"), true);
		for (int i = 1; i < playlistNewList.getItemCount() - 1; i++)
		{
			if (playlistNewList.isIndexSelected(i))
			{
				System.out.println("Adding " + playlistNewList.getItem(i));
				newPlaylist.addFile(playlistNewList.getItem(i));
			}
		}
		newPlaylist.close();
		reloadPlaylists();
		deselectAll(playlistNewList);
		setUI(playlistList);
	}

	public void setUI(Component ui) // used to switch between all the lists
	{
		if (songList.isVisible())
			songList.setVisible(false);
		if (playlistList.isVisible())
			playlistList.setVisible(false);
		if (playlistNewList.isVisible())
			playlistNewList.setVisible(false);
		if (playlistNameField.isVisible())
			playlistNameField.setVisible(false);
		if (deleteConfirmMenu.isVisible())
			deleteConfirmMenu.setVisible(false);
		for (int i = 0; i < playlistListMenu.length - 1; i++)
		{
			if (playlistListMenu[i].isVisible())
				playlistListMenu[i].setVisible(false);
		}
		ui.setVisible(true);
		if (ui == playlistNewList)
		{
			playlistNameField.setVisible(true);
		}
	}

	public void deselectAll(List list) // deselects anything selected in the list
	{
		if (list.isMultipleMode())
		{
			for (int i = 0; i < list.getItemCount(); i++)
			{
				list.deselect(i);
			}
		}
		else
		{
			list.deselect(list.getSelectedIndex());
		}
	}

	public void prepareDeletion(int num) // updates the deletion confirmation menu with the name of the playlist to delete and selects the playlist to delete
	{
		selectedForDeletion = num;
		deleteConfirmMenu.removeAll();
		deleteConfirmMenu.add("WARNING: You are about to delete the playlist \"" + playlistFiles[selectedForDeletion].getName() + "\" this can NOT be undone! Are you sure?");
		deleteConfirmMenu.add("YES!");
		deleteConfirmMenu.add("NO!");
	}

	public class ButtonPanel extends Panel implements MouseListener // panel for the buttons on the left
	{
		// double buffer
		Graphics buffer;
		BufferedImage offscreen;

		public ButtonPanel() // creating the panel
		{
			super();
			offscreen = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			buffer = offscreen.createGraphics();
			setBackground(Color.LIGHT_GRAY);
			addMouseListener(this);
		}

		@Override
		public void update(Graphics g)
		{
			paint(g);
		}

		public void paint(Graphics g) // drawing the buttons to the screen and updating their location/size
		{
			buffer.setColor(Color.LIGHT_GRAY);
			buffer.fillRect(0, 0, getWidth(), getHeight());
			super.paint(buffer);
			setButtonLocation();
			setButtonSize();
			drawButtons(buffer);
			g.drawImage(offscreen, 0, 0, null);
			frameRate(60);
			super.repaint();
		}

		public void drawButtons(Graphics g) // draws the buttons
		{
			tPlay.drawButton(g);
			tStop.drawButton(g);
			tNext.drawButton(g);
			tPrev.drawButton(g);
			tLoop.drawButton(g);
			shuffle.drawButton(g);
			tPause.drawButton(g);
			equalizerButton.drawButton(g);
		}

		public void setButtonLocation() // sets the buttons locations
		{
			tPlay.setLocation(0, 0);
			tPause.setLocation(0, getWidth());
			tStop.setLocation(0, getWidth() * 2);
			tLoop.setLocation(0, getWidth() * 3);
			tPrev.setLocation(0, getWidth() * 4);
			tNext.setLocation(0, getWidth() * 5);
			shuffle.setLocation(0, getWidth() * 6);
			equalizerButton.setLocation(0, getWidth() * 7);
		}

		public void setButtonSize() // sets the buttons size
		{
			tPrev.setSize(getWidth(), getWidth());
			tPlay.setSize(getWidth(), getWidth());
			tStop.setSize(getWidth(), getWidth());
			tLoop.setSize(getWidth(), getWidth());
			tNext.setSize(getWidth(), getWidth());
			shuffle.setSize(getWidth(), getWidth());
			tPause.setSize(getWidth(), getWidth());
			equalizerButton.setSize(getWidth(), getWidth());
		}

		public void resizePanel(int width, int height) // resizes the button panel to keep all buttons on screen
		{
			if (width * 8 > height)
			{
				setSize(height / 8, height);
			}
			else
			{
				setSize(width, height);
			}
		}

		public void mousePressed(MouseEvent e)
		{
			tPlay.buttonPressed(e.getX(), e.getY());
			tStop.buttonPressed(e.getX(), e.getY());
			tNext.buttonPressed(e.getX(), e.getY());
			tPrev.buttonPressed(e.getX(), e.getY());
			tLoop.buttonPressed(e.getX(), e.getY());
			shuffle.buttonPressed(e.getX(), e.getY());
			tPause.buttonPressed(e.getX(), e.getY());
			equalizerButton.buttonPressed(e.getX(), e.getY());
		}

		public void mouseReleased(MouseEvent e)
		{
			if (tPlay.buttonReleased(e.getX(), e.getY()))
				player.playSelectedSong();
			if (tNext.buttonReleased(e.getX(), e.getY()))
				player.nextSong();
			if (tPrev.buttonReleased(e.getX(), e.getY()))
				if (player.getCurrentMillis() < 5000)
				{
					player.prevSong();
				}
				else
				{
					player.stopSong();
					player.playSong();
				}
			if (tStop.buttonReleased(e.getX(), e.getY()))
				player.stopSong();
			if (tLoop.buttonReleased(e.getX(), e.getY()))
				player.toggleLoop();
			if (shuffle.buttonReleased(e.getX(), e.getY()))
				player.toggleShuffle();
			if (tPause.buttonReleased(e.getX(), e.getY()))
				player.pauseSong();
			if (equalizerButton.buttonReleased(e.getX(), e.getY()))
				volChanger.setVisible(true);
		}

		public void mouseClicked(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
	}

	public class SongPanel extends Panel implements ActionListener, ItemListener, MouseListener
	{
		// Double Buffer
		Graphics buffer;
		BufferedImage offscreen;

		public SongPanel()
		{
			super();
			offscreen = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			buffer = offscreen.createGraphics();
			setBackground(Color.LIGHT_GRAY);

			songList = new List();
			slider = new Slider(1);
			playlistList = new List();
			playlistNewList = new List();
			deleteConfirmMenu = new List();
			playlistNameField = new TextField();
			playlistNewList.add("/Cancel/");
			for (File i : files) // Add songs to choice menu
			{
				songList.add(i.getName());
				playlistNewList.add(i.getName());
			}
			songList.add("/Playlists/");
			playlistNewList.add("/Create/");

			add(songList);
			songList.addItemListener(this);
			songList.addActionListener(this);

			add(playlistList);
			playlistList.addItemListener(this);
			playlistList.addActionListener(this);
			playlistList.setVisible(false);

			add(playlistNameField);
			playlistNameField.addActionListener(this);
			playlistNameField.setVisible(false);

			add(deleteConfirmMenu);
			deleteConfirmMenu.addItemListener(this);
			deleteConfirmMenu.setVisible(false);

			add(playlistNewList);
			playlistNewList.addItemListener(this);
			playlistNewList.addActionListener(this);
			playlistNewList.setVisible(false);
			playlistNewList.setMultipleMode(true);

			add(slider);
			slider.addMouseListener(this);
			slider.setBackground(Color.darkGray);
			slider.setSliderColor(new Color(100, 0, 200));

			textScroll = new int[] { 180, getWidth() - (getHeight() / 3) + 180 };

			new Thread(new Runnable() // Thread for scrolling text
			{
				public void run()
				{
					while (true)
					{
						textScroll[0]--;
						textScroll[1]--;
						if ((textScroll[0] == 0 && getStringLength(player.getCurrentSong()) + 10 < getWidth() - (getHeight() / 3))
								|| (textScroll[1] < -getStringLength(player.getCurrentSong()) && getStringLength(player.getCurrentSong()) + 10 > getWidth() - (getHeight() / 3)))
						{
							textScroll[1] = getWidth() - (getHeight() / 3);
						}
						if ((textScroll[1] == 0 && getStringLength(player.getCurrentSong()) + 10 < getWidth() - (getHeight() / 3))
								|| (textScroll[0] < -getStringLength(player.getCurrentSong()) && getStringLength(player.getCurrentSong()) + 10 > getWidth() - (getHeight() / 3)))
						{
							textScroll[0] = getWidth() - (getHeight() / 3);
						}
						if (player.canSeek()) // prevents the seek bar from updating while it's being dragged
						{
							slider.setTotal(player.getTotalDuration() * 1000);
							if (!slider.isDragging())
							{
								slider.setValue(player.getCurrentMillis());
							}
						}
						frameRate(60);
					}
				}
			}).start();
		}

		@Override
		public void update(Graphics g)
		{
			paint(g);
		}

		public void paint(Graphics g)
		{
			if (frameReady) // used so the graphics don't break when the program starts
			{
				// updates the size and location of the lists and seek bar
				setUILocation();
				setUISize();
				// draws the black background behind the scrolling text
				buffer.setColor(Color.BLACK);
				buffer.fillRect(0, 0, getWidth(), getHeight());
				buffer.setColor(Color.WHITE);
				// draws the scrolling text, it will only draw one if the 2nd one can't fit
				buffer.drawString(player.getCurrentSong(), textScroll[0], (int) (getHeight() / 6));
				if (buffer.getFontMetrics().stringWidth(player.getCurrentSong()) + 10 < getWidth())
				{
					buffer.drawString(player.getCurrentSong(), textScroll[1], (int) (getHeight() / 6));
				}
				// shows the time the song is at or the time the seek bar is being dragged to
				if (!slider.isDragging())
				{
					buffer.drawString(player.formatDuration(player.getCurrentMillis()), getWidth() - (getHeight() / 3) - getStringLength(player.formatDuration(player.getCurrentMillis())) - 2, (int) (getHeight() / 3) - 5);
				}
				else
				{
					if (slider.getValue() / 1000 % 60 > 9) // prints an extra 0 in seconds if seconds is less than 10
					{
						buffer.drawString(player.formatDuration((int) slider.getValue()), getWidth() - (getHeight() / 3) - getStringLength(player.formatDuration(player.getCurrentMillis())) - 2, (int) (getHeight() / 3) - 5);
					}
				}
				// draws the album art
				buffer.drawImage(player.getResizedArtwork(getHeight() / 3, getHeight() / 3), getWidth() - (getHeight() / 3), 0, getHeight() / 3, getHeight() / 3, null);
				// draws the shuffle and repeat icons next to the time
				buffer.drawImage(player.getShuffleIcon(), getWidth() - (getHeight() / 3) - getStringLength(player.formatDuration(player.getCurrentMillis())) - 25, (int) (getHeight() / 3) - 23, null);
				buffer.drawImage(player.getRepeatIcon(), getWidth() - (getHeight() / 3) - getStringLength(player.formatDuration(player.getCurrentMillis())) - 50, (int) (getHeight() / 3) - 23, null);
				// displays the name of the selected song for 5 seconds
				if (player.isPopup())
				{
					buffer.drawString(player.getSelectedSongName(), 5, (int) (getHeight() / 3) - 10);
				}
				// draws everything to the screen
				g.drawImage(offscreen, 0, 0, null);
				frameRate(60);
				super.repaint();
			}
		}

		public void setUILocation() // updates the location of the lists and seek bar
		{
			songList.setLocation(0, getHeight() / 3);
			playlistList.setLocation(0, getHeight() / 3);
			playlistNameField.setLocation(0, getHeight() / 3);
			deleteConfirmMenu.setLocation(0, getHeight() / 3);
			playlistNewList.setLocation(0, (getHeight() / 3) + (getHeight() / 15));
			for (int i = 0; i < playlistListMenu.length; i++)
			{
				playlistListMenu[i].setLocation(0, getHeight() / 3);
			}
			slider.setLocation(5, 5);
		}

		public void setUISize() // updates the size of the lists and seek bar
		{
			songList.setSize(getWidth(), getHeight() - (getHeight() / 3));
			playlistList.setSize(getWidth(), getHeight() - (getHeight() / 3));
			deleteConfirmMenu.setSize(getWidth(), getHeight() - (getHeight() / 3));
			playlistNameField.setSize(getWidth(), (getHeight() / 15));
			playlistNewList.setSize(getWidth(), getHeight() - (getHeight() / 3) - (getHeight() / 15));
			for (int i = 0; i < playlistListMenu.length; i++)
			{
				playlistListMenu[i].setSize(getWidth(), getHeight() - (getHeight() / 3));
			}
			slider.setSize(getWidth() - (getHeight() / 3) - 10, 20);
		}

		public int getStringLength(String text) // Gets the pixel width of a string
		{
			return buffer.getFontMetrics().stringWidth(text);
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == songList && songList.getSelectedIndex() < songList.getItemCount() - 1)
			{
				player.stopSong();
				if (player.getID() != 0)
				{
					player = new Player(files);
					player.selectSong(songList.getSelectedIndex());
				}
				player.playSelectedSong();
			}

			for (int i = 0; i < playlistListMenu.length; i++)
			{
				if (e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedIndex() > 0)
				{
					player.stopSong();
					if (player.getID() != i + 1)
					{
						player = new Player(playlist[i].getSongFiles(), i + 1);
					}
					player.selectSong(playlistListMenu[i].getSelectedIndex() - 1);
					player.playSelectedSong();
				}
			}
		}

		public void itemStateChanged(ItemEvent e)
		{
			if (e.getSource() == songList && songList.getSelectedIndex() < songList.getItemCount() - 1 && player.getID() == 0)
			{
				player.selectSong(songList.getSelectedIndex());
			}
			else if (e.getSource() == songList && songList.getSelectedIndex() == songList.getItemCount() - 1)
			{
				deselectAll(songList);
				setUI(playlistList);
			}

			if (e.getSource() == playlistList)
			{
				if (playlistList.getSelectedItem().equals("/Back/"))
				{
					deselectAll(playlistList);
					setUI(songList);
				}
				else if (playlistList.getSelectedIndex() < playlistList.getItemCount() - 2)
				{
					setUI(playlistListMenu[playlistList.getSelectedIndex() - 1]);
					deselectAll(playlistList);
				}
				else if (playlistList.getSelectedItem().equals("/Reload/"))
				{
					reloadPlaylists();
				}
				else if (playlistList.getSelectedItem().equals("/Add/"))
				{
					deselectAll(playlistList);
					setUI(playlistNewList);
				}
			}

			for (int i = 0; i < playlistListMenu.length; i++)
			{
				if (e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedIndex() > 0 && player.getID() == i + 1)
				{
					player.selectSong(playlistListMenu[i].getSelectedIndex() - 1);
					player.startPopup();
				}
			}

			if (e.getSource() == playlistNewList)
			{
				if (playlistNewList.isIndexSelected(0))
				{
					deselectAll(playlistNewList);
					setUI(playlistList);
				}
				else if (playlistNewList.isIndexSelected(playlistNewList.getItemCount() - 1))
				{
					createPlaylist();
				}
			}

			for (int i = 0; i < playlistListMenu.length; i++)
			{
				if (e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedItem().equals("/Back/"))
				{
					deselectAll(playlistListMenu[i]);
					setUI(playlistList);
				}
				else if (e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedItem().equals("/Delete/"))
				{
					deselectAll(playlistListMenu[i]);
					prepareDeletion(i);
					setUI(deleteConfirmMenu);
				}
			}

			if (e.getSource() == deleteConfirmMenu)
			{
				if (deleteConfirmMenu.getSelectedIndex() == 1)
				{
					playlistFiles[selectedForDeletion].delete();
					reloadPlaylists();
					deselectAll(deleteConfirmMenu);
					setUI(playlistList);
				}
				else if (deleteConfirmMenu.getSelectedIndex() == 2)
				{
					deselectAll(deleteConfirmMenu);
					setUI(playlistList);
				}
			}
		}

		public void mouseClicked(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
			if (player.isPlaying())
			{
				player.stopSong();
				player.playSongAtTime((int) slider.getValue() / 26);
			}
			slider.stopDragging();
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
	}

	public class VolumeChanger implements MouseListener, MouseMotionListener
	{
		private Frame volFrame;
		private Panel volPanel;
		private Slider[] sliders = new Slider[33];
		private ToggleButton[] toggles = new ToggleButton[2];
		private Equalizer equalizer;

		public VolumeChanger()
		{
			volFrame = new Frame();
			init();
		}

		public void init()
		{
			volFrame.pack();
			volFrame.setSize(600, 400);
			volFrame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent windowevent)
				{
					setVisible(false);
				}
			});
			volFrame.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					if (volFrame.isVisible())
					{
						for (int i = 0; i < sliders.length; i++)
						{
							sliders[i].setLocation((int) (float) (((float) volPanel.getWidth() / (float) sliders.length) * i), 0);
							sliders[i].setSize((int) (float) ((float) volPanel.getWidth() / 32), volPanel.getHeight() - 20);
						}
						toggles[0].setLocation(0, volPanel.getHeight() - 20);
						toggles[0].setSize((int) (float) ((float) volPanel.getWidth() / 32), 20);
						toggles[1].setLocation((int) (float) ((float) volPanel.getWidth() / (float) sliders.length), volPanel.getHeight() - 20);
						toggles[1].setSize((int) (float) ((float) volPanel.getWidth() - (volPanel.getWidth() / 32)), 20);
					}
				}
			});

			volPanel = new Panel();
			volPanel.setLayout(null);
			volPanel.setBackground(Color.black);
			volFrame.add(volPanel);
			equalizer = new Equalizer();
			for (int i = 0; i < sliders.length; i++)
			{
				sliders[i] = new Slider(3);
				volPanel.add(sliders[i]);
				sliders[i].addMouseListener(this);
				sliders[i].addMouseMotionListener(this);
				sliders[i].setVertical(true);
				if (i > 0)
				{
					sliders[i].setTotal(2);
					sliders[i].setValue(1);
					sliders[i].setLeftOnly(true);
					sliders[i].setDesc("Equalizer");
				}
			}
			for (int i = 0; i < toggles.length; i++)
			{
				toggles[i] = new ToggleButton(3);
				toggles[i].setState(true);
				volPanel.add(toggles[i]);
				toggles[i].addMouseListener(this);
			}
			sliders[0].setTotal(1);
			sliders[0].setValue(0.10f);
			sliders[0].setDesc("Volume");
			player.setVolume(0.10f);
			readEqFile();
		}

		public void readEqFile()
		{
			try
			{
				BufferedReader bIn = new BufferedReader(new InputStreamReader(new FileInputStream(eqFile), StandardCharsets.UTF_8));
				for (int i = 0; i < toggles.length; i++)
				{
					String boolString = bIn.readLine();
					if (boolString != null && boolString.toLowerCase().equals("false"))
					{
						toggles[i].setState(false);
					}
					else
					{
						toggles[i].setState(true);
					}
				}
				for (int i = 0; i < sliders.length; i++)
				{
					String valueString = bIn.readLine();
					float value;
					if (valueString == null)
					{
						if (i == 0)
						{
							value = 0.10f;
						}
						else
						{
							value = 1f;
						}
					}
					else
					{
						value = Float.valueOf(valueString);
					}
					sliders[i].setValue(value);
					if (i == 0)
					{
						if (toggles[0].getState())
						{
							player.setVolume(value);
						}
						else
						{
							player.setVolume(0f);
						}
					}
					else
					{
						if (toggles[1].getState())
						{
							equalizer.setBand(i - 1, value - 1);
							player.setEqualizer(equalizer);
						}
						else
						{
							equalizer.setBand(i - 1, 0.0f);
							player.setEqualizer(equalizer);
						}
					}
				}
				bIn.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public void updateEqFile()
		{
			try
			{
				BufferedWriter bOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(eqFile), StandardCharsets.UTF_8));
				for (int i = 0; i < toggles.length; i++)
				{
					bOut.write(String.valueOf(toggles[i].getState()));
					bOut.newLine();
				}
				for (int i = 0; i < sliders.length; i++)
				{
					bOut.write(String.valueOf(sliders[i].getValue()));
					bOut.newLine();
				}
				bOut.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		public void setVisible(boolean bool)
		{
			volFrame.setVisible(bool);
		}

		public void mouseClicked(MouseEvent e)
		{
			if (e.getSource() == toggles[0])
			{
				if (toggles[0].getState())
				{
					player.setVolume(sliders[0].getValue());
				}
				else
				{
					player.setVolume(0f);
				}
			}
			else if (e.getSource() == toggles[1])
			{
				if (toggles[1].getState())
				{
					for (int i = 1; i < sliders.length; i++)
					{
						equalizer.setBand(i - 1, sliders[i].getValue() - 1);
						player.setEqualizer(equalizer);
					}
				}
				else
				{
					for (int i = 1; i < sliders.length; i++)
					{
						equalizer.setBand(i - 1, 0.0f);
						player.setEqualizer(equalizer);
					}
				}
			}
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				if (e.getSource() == sliders[0])
				{
					player.setVolume(sliders[0].getValue());
				}
			}
			for (int i = 1; i < sliders.length; i++)
			{
				if (e.getSource() == sliders[i])
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						if (toggles[1].getState())
						{
							equalizer.setBand(i - 1, sliders[i].getValue() - 1);
							player.setEqualizer(equalizer);
						}
					}
					else
					{
						sliders[i].setValue(1);
						equalizer.setBand(i - 1, 0.0f);
						player.setEqualizer(equalizer);
					}
				}
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
			if (e.getSource() == sliders[0])
			{
				if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Button1"))
				{
					player.setVolume(sliders[0].getValue());
				}
			}
			for (int i = 1; i < sliders.length; i++)
			{
				if (e.getSource() == sliders[i])
				{
					if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Button1"))
					{
						equalizer.setBand(i - 1, sliders[i].getValue() - 1);
						player.setEqualizer(equalizer);
					}
					else if (MouseEvent.getMouseModifiersText(e.getModifiersEx()).equals("Button3"))
					{
						sliders[i].setValue(1);
						equalizer.setBand(i - 1, 0.0f);
						player.setEqualizer(equalizer);
					}
				}
			}
		}
	}
}