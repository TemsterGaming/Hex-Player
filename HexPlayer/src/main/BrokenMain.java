/*
 *Credit goes to Appzgear for the images on the buttons
 *http://www.flaticon.com/authors/appzgear
 */

package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import misc.TImageButton;

public class BrokenMain implements ItemListener, MouseListener, ActionListener
{
	FilenameFilter mp3Filter = new FilenameFilter() // Used to filter out
	{											   // files/folders that don't
		public boolean accept(File f, String s)	  // end in ".mp3"
		{
			if(s.endsWith(".mp3"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	FilenameFilter plFilter = new FilenameFilter() // Used to filter out
	{											  // files/folders that don't
		public boolean accept(File f, String s)  // end in ".pl"
		{
			if(s.endsWith(".pl"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	// Double Buffer
	private static Image offscreen;
	private static Graphics buffer;

	// Variables
	private static Insets insets;
	private static boolean frameReady;
	private static File dir, playlistDir;
	private static int selectedForDeletion;

	// Arrays
	private static File[] files;
	private static int[] textScroll;
	private static File[] playlistFiles;
	private static TPlaylist[] playlist;
	private static BufferedImage[] buttonPng = new BufferedImage[7];
	private static List[] playlistListMenu; // Temporary name, can't think
	// of one at the moment

	// Classes
	private static Player player;
	private static TextField playlistNameField;
	private static TImageButton tPlay, tPause, tStop, tNext, tPrev, tLoop, shuffle;
	private static List songList, playlistList, playlistNewList, deleteConfirmMenu;

	// Frame stuff
	private static Frame frame;
	private static Panel mainPanel;
	private static Panel menuPanel;
	private static Panel songInfoPanel;
	private static ButtonPanel buttonPanel;

	public static void main(String[] args)
	{
		new BrokenMain();
	}

	public BrokenMain()
	{
		frame = new Frame("Hex Player");
		mainPanel = new Panel();
		buttonPanel = new ButtonPanel();
		menuPanel = new Panel();
		songInfoPanel = new Panel();
		init();
	}

	public void init()
	{
		frame.setLayout(new BorderLayout());
		// add(mainPanel);
		frame.add(buttonPanel,BorderLayout.WEST);
		//add(menuPanel, BorderLayout.SOUTH);
		// add(songInfoPanel,BorderLayout.NORTH);
		buttonPanel.setPreferredSize(new Dimension((int) (mainPanel.getWidth() / 14.222), mainPanel.getHeight()));
		try
		{
			frame.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("icon.png")));
		}
		catch(IOException e2)
		{
			e2.printStackTrace();
		}
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent windowevent)
			{
				System.exit(0);
			}
		});

		insets = frame.getInsets();

		// Double Buffer

		buttonPanel.addMouseListener(this);

		textScroll = new int[] { 180, songInfoPanel.getWidth() - (songInfoPanel.getHeight() / 3) + 180 };

		songList = new List();
		playlistList = new List();
		playlistNewList = new List();
		deleteConfirmMenu = new List();
		playlistNameField = new TextField();

		System.out.println("Please Enter Directory of Songs");
		System.out.println("Example: V:/Songs");
		try
		{
			String dirString = "Songs";
			System.out.println("Setting Directory to: " + dirString);
			dir = new File(dirString);
			if(!dir.exists())
			{
				System.out.println("Songs Folder Not Found, Trying Test Folder");
				dirString = "V:/Songs";
				dir = new File(dirString);
				if(!dir.exists())
				{
					System.exit(0);
				}
			}
			playlistDir = new File(dirString + "/Playlists");
			if(!playlistDir.exists())
			{
				playlistDir.mkdir();
			}
		}
		catch(java.lang.NullPointerException e)
		{
			System.err.println("ERROR! Directory Not Found!");
			System.exit(0);
		}
		catch(java.security.AccessControlException e)
		{
			System.err.println("ERROR! Access Denied!");
			System.err.println("Either move the program to that directory or choose another directory");
			System.exit(0);
		}
		files = dir.listFiles(mp3Filter);

		playlistNewList.add("/Cancel/");
		for(File i : files) // Add songs to choice menu
		{
			songList.add(i.getName());
			playlistNewList.add(i.getName());
		}
		songList.add("/Playlists/");
		playlistNewList.add("/Create/");

		reloadPlaylists();

		menuPanel.add(songList);
		songList.addItemListener(this);
		songList.addActionListener(this);

		menuPanel.add(playlistList);
		playlistList.addItemListener(this);
		playlistList.addActionListener(this);
		playlistList.setVisible(false);

		menuPanel.add(playlistNameField);
		playlistNameField.addActionListener(this);
		playlistNameField.setVisible(false);

		menuPanel.add(deleteConfirmMenu);
		deleteConfirmMenu.addItemListener(this);
		deleteConfirmMenu.setVisible(false);

		menuPanel.add(playlistNewList);
		playlistNewList.addItemListener(this);
		playlistNewList.addActionListener(this);
		playlistNewList.setVisible(false);
		playlistNewList.setMultipleMode(true);

		try
		{
			buttonPng[0] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("play.png"));
			buttonPng[1] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("stop.png"));
			buttonPng[2] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("next.png"));
			buttonPng[3] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("prev.png"));
			buttonPng[4] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("loop.png"));
			buttonPng[5] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("shuffle.png"));
			buttonPng[6] = ImageIO.read(getClass().getClassLoader().getResourceAsStream("pause.png"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(NullPointerException e1)
		{
			e1.printStackTrace();
			System.exit(0);
		}

		tPlay = new TImageButton(0, 0, 45, 45, buttonPng[0], null, Color.black);
		tStop = new TImageButton(0, 0, 45, 45, buttonPng[1], null, Color.black);
		tNext = new TImageButton(0, 0, 45, 45, buttonPng[2], null, Color.black);
		tPrev = new TImageButton(0, 0, 45, 45, buttonPng[3], null, Color.black);
		tLoop = new TImageButton(0, 0, 45, 45, buttonPng[4], null, Color.black);
		shuffle = new TImageButton(0, 0, 45, 45, buttonPng[5], null, Color.black);
		tPause = new TImageButton(0, 0, 45, 45, buttonPng[6], null, Color.black);

		// Setting up player
		System.out.println("Starting Player");
		player = new Player(files);

		frame.pack();
		frame.setSize(700, 450);
		offscreen = frame.createImage(1920, 1080);
		buffer = offscreen.getGraphics();
		new Thread(new Runnable() // Thread for scrolling text
		{
			public void run()
			{
				while(true)
				{
					textScroll[0]--;
					textScroll[1]--;
					if((textScroll[0] == 0 && getStringLength(player.getCurrentSong()) + 10 < songInfoPanel.getWidth() - songInfoPanel.getHeight())
							|| (textScroll[1] < -getStringLength(player.getCurrentSong()) && getStringLength(player.getCurrentSong()) + 10 > songInfoPanel.getWidth() - songInfoPanel.getHeight()))
					{
						textScroll[1] = songInfoPanel.getWidth() - songInfoPanel.getHeight();
					}
					if((textScroll[1] == 0 && getStringLength(player.getCurrentSong()) + 10 < songInfoPanel.getWidth() - songInfoPanel.getHeight())
							|| (textScroll[0] < -getStringLength(player.getCurrentSong()) && getStringLength(player.getCurrentSong()) + 10 > songInfoPanel.getWidth() - songInfoPanel.getHeight()))
					{
						textScroll[0] = songInfoPanel.getWidth() - songInfoPanel.getHeight();
					}
					frameRate(60);
				}
			}
		}).start();
		menuPanel.setVisible(true);
		songInfoPanel.setVisible(true);
		buttonPanel.setVisible(true);
		mainPanel.setVisible(true);
		frame.setVisible(true);
		frameReady = true;
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g)
	{
		if(frameReady)
		{
			buffer.clearRect(0, 0, getWidth(), getHeight());
			buffer.fillRect((int) (double) (getWidth() / 14.222), 0, getWidth(), getHeight() / 3);

			buffer.setColor(Color.WHITE);
			buffer.drawString(player.getCurrentSong(), textScroll[0], (int) (getHeight() / 6));
			if(buffer.getFontMetrics().stringWidth(player.getCurrentSong()) + 10 < getWidth())
			{
				buffer.drawString(player.getCurrentSong(), textScroll[1], (int) (getHeight() / 6));
			}
			buffer.drawString(player.getFormattedDuration(), getWidth() - (getHeight() / 3) - getStringLength(player.getFormattedDuration()) - 2, (int) (getHeight() / 3) - 5);
			buffer.drawImage(player.getResizedArtwork((getHeight() - getYInsets()) / 3, (getHeight() - getYInsets()) / 3), getWidth() - (getHeight() / 3), 0, ((getHeight() - getYInsets()) / 3), getHeight() / 3, null);
			if(player.isPopup())
			{
				buffer.drawString(player.getSelectedSongName(), (int) (double) (getWidth() / 14.222) + 5, (int) (getHeight() / 3) - 10);
			}
			buffer.setColor(Color.BLACK);

			setUILocation();
			setUISize();
			//drawButtons(buffer);

			g.drawImage(offscreen, insets.left, insets.top, null);
		}
		frameRate(60);
		repaint();
	}

	public void drawButtons(Graphics g)
	{
		Color temp = g.getColor();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, (int) (double) (getWidth() / 14.222), getHeight());
		tPlay.drawButton(buffer);
		tStop.drawButton(buffer);
		tNext.drawButton(buffer);
		tPrev.drawButton(buffer);
		tLoop.drawButton(buffer);
		shuffle.drawButton(buffer);
		tPause.drawButton(buffer);
		g.setColor(temp);
	}

	public void setUILocation()
	{
		songList.setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + insets.top);
		playlistList.setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + insets.top);
		playlistNameField.setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + insets.top);
		deleteConfirmMenu.setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + insets.top);
		playlistNewList.setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + (getHeight() / 15) + insets.top);
		for(int i = 0; i < playlistListMenu.length; i++)
		{
			playlistListMenu[i].setLocation((int) (double) (getWidth() / 14.222) + insets.right, (getHeight() / 3) + insets.top);
		}
		tPlay.setLocation(0, 0);
		tPause.setLocation(0, (getHeight() / 8));
		tStop.setLocation(0, (getHeight() / 8) * 2);
		tLoop.setLocation(0, (getHeight() / 8) * 3);
		tPrev.setLocation(0, (getHeight() / 8) * 4);
		tNext.setLocation(0, (getHeight() / 8) * 5);
		shuffle.setLocation(0, (getHeight() / 8) * 6);
	}

	public void setUISize()
	{
		songList.setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), getHeight() - (getHeight() / 3) - getYInsets());
		playlistList.setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), getHeight() - (getHeight() / 3) - getYInsets());
		playlistNameField.setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), (getHeight() / 15) - getYInsets());
		deleteConfirmMenu.setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), getHeight() - (getHeight() / 3) - getYInsets());
		playlistNewList.setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), getHeight() - (getHeight() / 3) - (getHeight() / 15) - getYInsets());
		for(int i = 0; i < playlistListMenu.length; i++)
		{
			playlistListMenu[i].setSize(getWidth() - (int) (double) (getWidth() / 14.222) - getXInsets(), getHeight() - (getHeight() / 3) - getYInsets());
		}
		tPrev.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		tPlay.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		tStop.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		tLoop.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		tNext.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		shuffle.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
		tPause.setSize((int) (double) (getWidth() / 14.222), (getHeight() / 8));
	}

	public int getStringLength(String text) // Gets the pixel width of a string
	{
		return buffer.getFontMetrics().stringWidth(text);
	}

	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource() == songList && songList.getSelectedIndex() < songList.getItemCount() - 1 && player.getID() == 0)
		{
			player.selectSong(songList.getSelectedIndex());
			player.startPopup();
		}
		else if(e.getSource() == songList && songList.getSelectedIndex() == songList.getItemCount() - 1)
		{
			deselectAll(songList);
			setUI(playlistList);
		}

		if(e.getSource() == playlistList)
		{
			if(playlistList.getSelectedItem().equals("/Back/"))
			{
				deselectAll(playlistList);
				setUI(songList);
			}
			else if(playlistList.getSelectedIndex() < playlistList.getItemCount() - 2)
			{
				setUI(playlistListMenu[playlistList.getSelectedIndex() - 1]);
				deselectAll(playlistList);
			}
			else if(playlistList.getSelectedItem().equals("/Reload/"))
			{
				reloadPlaylists();
			}
			else if(playlistList.getSelectedItem().equals("/Add/"))
			{
				deselectAll(playlistList);
				setUI(playlistNewList);
			}
		}

		for(int i = 0; i < playlistListMenu.length; i++)
		{
			if(e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedIndex() > 0 && player.getID() == i + 1)
			{
				player.selectSong(playlistListMenu[i].getSelectedIndex() - 1);
				player.startPopup();
			}
		}

		if(e.getSource() == playlistNewList)
		{
			if(playlistNewList.isIndexSelected(0))
			{
				deselectAll(playlistNewList);
				setUI(playlistList);
			}
			else if(playlistNewList.isIndexSelected(playlistNewList.getItemCount() - 1))
			{
				createPlaylist();
			}
		}

		for(int i = 0; i < playlistListMenu.length; i++)
		{
			if(e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedItem().equals("/Back/"))
			{
				deselectAll(playlistListMenu[i]);
				setUI(playlistList);
			}
			else if(e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedItem().equals("/Delete/"))
			{
				deselectAll(playlistListMenu[i]);
				prepareDeletion(i);
				setUI(deleteConfirmMenu);
			}
		}

		if(e.getSource() == deleteConfirmMenu)
		{
			if(deleteConfirmMenu.getSelectedIndex() == 1)
			{
				playlistFiles[selectedForDeletion].delete();
				reloadPlaylists();
				deselectAll(deleteConfirmMenu);
				setUI(playlistList);
			}
			else if(deleteConfirmMenu.getSelectedIndex() == 2)
			{
				deselectAll(deleteConfirmMenu);
				setUI(playlistList);
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{
		tPlay.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		tStop.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		tNext.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		tPrev.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		tLoop.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		shuffle.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
		tPause.buttonPressed(e.getX() - insets.left, e.getY() - insets.top);
	}

	public void mouseReleased(MouseEvent e)
	{
		if(tPlay.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.playSelectedSong();
		}

		if(tNext.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.nextSong();
		}

		if(tPrev.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.prevSong();
		}

		if(tStop.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.stopSong();
		}

		if(tLoop.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.toggleLoop();
		}

		if(shuffle.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.toggleShuffle();
		}

		if(tPause.buttonReleased(e.getX() - insets.left, e.getY() - insets.top))
		{
			player.pauseSong();
		}
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

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == songList && songList.getSelectedIndex() < songList.getItemCount() - 1)
		{
			if(player.getID() != 0)
			{
				player.stopSong();
				player = new Player(files);
				player.selectSong(songList.getSelectedIndex());
			}
			player.playSelectedSong();
		}

		for(int i = 0; i < playlistListMenu.length; i++)
		{
			if(e.getSource() == playlistListMenu[i] && playlistListMenu[i].getSelectedIndex() > 0)
			{
				if(player.getID() != i + 1)
				{
					player.stopSong();
					player = new Player(playlist[i].getSongFiles(), i + 1);
				}
				player.selectSong(playlistListMenu[i].getSelectedIndex() - 1);
				player.playSelectedSong();
			}
		}
	}

	public void reloadPlaylists()
	{
		playlistFiles = playlistDir.listFiles(plFilter);
		playlist = new TPlaylist[playlistDir.listFiles(plFilter).length];
		playlistListMenu = new List[playlistDir.listFiles(plFilter).length];
		for(int i = 0; i < playlist.length; i++)
		{
			playlist[i] = new TPlaylist(playlistDir.listFiles(plFilter)[i], false);
			playlistListMenu[i] = new List();
			playlistListMenu[i].add("/Back/");
			for(int a = 0; a < playlist[i].getSongCount(); a++)
			{
				playlistListMenu[i].add(playlist[i].getSongNames()[a]);
			}
			playlistListMenu[i].add("/Delete/");
			playlist[i].close();
		}

		playlistList.removeAll();
		playlistList.add("/Back/");
		for(File i : playlistFiles)
		{
			playlistList.add(i.getName());
		}
		playlistList.add("/Add/");
		playlistList.add("/Reload/");

		for(int i = 0; i < playlistListMenu.length; i++)
		{
			frame.add(playlistListMenu[i]);
			playlistListMenu[i].addItemListener(this);
			playlistListMenu[i].addActionListener(this);
			playlistListMenu[i].setVisible(false);
		}
	}

	public void createPlaylist()
	{
		TPlaylist newPlaylist = new TPlaylist(new File("Playlists/" + playlistNameField.getText() + ".pl"), true);
		for(int i = 1; i < playlistNewList.getItemCount() - 1; i++)
		{
			if(playlistNewList.isIndexSelected(i))
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

	public void setUI(Component ui)
	{
		if(songList.isVisible())
			songList.setVisible(false);
		if(playlistList.isVisible())
			playlistList.setVisible(false);
		if(playlistNewList.isVisible())
			playlistNewList.setVisible(false);
		if(playlistNameField.isVisible())
			playlistNameField.setVisible(false);
		if(deleteConfirmMenu.isVisible())
			deleteConfirmMenu.setVisible(false);
		for(int i = 0; i < playlistListMenu.length - 1; i++)
		{
			if(playlistListMenu[i].isVisible())
				playlistListMenu[i].setVisible(false);
		}
		ui.setVisible(true);
		if(ui == playlistNewList)
		{
			playlistNameField.setVisible(true);
		}
	}

	public void deselectAll(List list)
	{
		if(list.isMultipleMode())
		{
			for(int i = 0; i < list.getItemCount(); i++)
			{
				list.deselect(i);
			}
		}
		else
		{
			list.deselect(list.getSelectedIndex());
		}
	}

	public void prepareDeletion(int num)
	{
		selectedForDeletion = num;
		deleteConfirmMenu.removeAll();
		deleteConfirmMenu.add("WARNING: You are about to delete the playlist \"" + playlistFiles[selectedForDeletion].getName() + "\" this can NOT be undone! Are you sure?");
		deleteConfirmMenu.add("YES!");
		deleteConfirmMenu.add("NO!");
	}

	public int getYInsets()
	{
		return insets.top + insets.bottom;
	}

	public int getXInsets()
	{
		return insets.left + insets.right;
	}

	public void frameRate(int num)
	{
		try
		{
			Thread.sleep(1000 / num);
		}
		catch(InterruptedException e)
		{
			System.err.println("Sleep Exception");
		}
	}

	public class ButtonPanel extends Panel implements MouseListener
	{
		public ButtonPanel()
		{
			super();
			setBackground(Color.LIGHT_GRAY);
		}
		
		public void paint(Graphics g)
		{
			super.paint(g);
			drawButtons(g);
			frameRate(60);
			super.repaint();
		}

		public void drawButtons(Graphics g)
		{
			tPlay.drawButton(g);
			tStop.drawButton(g);
			tNext.drawButton(g);
			tPrev.drawButton(g);
			tLoop.drawButton(g);
			shuffle.drawButton(g);
			tPause.drawButton(g);
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
		}

		public void mouseReleased(MouseEvent e)
		{
			if(tPlay.buttonReleased(e.getX(), e.getY()))
				player.playSelectedSong();
			if(tNext.buttonReleased(e.getX(), e.getY()))
				player.nextSong();
			if(tPrev.buttonReleased(e.getX(), e.getY()))
				player.prevSong();
			if(tStop.buttonReleased(e.getX(), e.getY()))
				player.stopSong();
			if(tLoop.buttonReleased(e.getX(), e.getY()))
				player.toggleLoop();
			if(shuffle.buttonReleased(e.getX(), e.getY()))
				player.toggleShuffle();
			if(tPause.buttonReleased(e.getX(), e.getY()))
				player.pauseSong();
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
}