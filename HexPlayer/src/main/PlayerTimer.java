package main;

public class PlayerTimer
{
	private long startTime;
	private int currentDuration;
	private boolean running = true;
	private boolean paused = false;

	public PlayerTimer()
	{
		this(0);
	}

	public PlayerTimer(int duration)
	{
		startTimer(duration);
	}
	
	public void startTimer(int duration)
	{
		startTime = System.currentTimeMillis() - duration;
		currentDuration = duration;
		System.out.println("Starting Timer");
		running = true;
		new Thread(new Runnable()
		{
			public void run()
			{
				while(running)
				{
					frameRate(1000);
					if(!paused)
					{
						currentDuration = (int) (System.currentTimeMillis() - startTime);
					}
				}
			}
		}).start();
	}

	public int getSeconds()
	{
		return (currentDuration / 1000) % 60;
	}

	public int getMinutes()
	{
		return (currentDuration / 1000) / 60;
	}

	public void stopTimer()
	{
		System.out.println("Stopping Timer");
		running = false;
	}

	public void pauseTimer()
	{
		System.out.println("Pausing Timer");
		paused = true;
	}

	public void resumeTimer()
	{
		System.out.println("Resuming Timer");
		startTime = System.currentTimeMillis() - currentDuration;
		paused = false;
	}

	public int getMilliseconds()
	{
		return currentDuration;
	}

	public void setTime(int num)
	{
		currentDuration = num;
	}
	
	public boolean isRunning()
	{
		return running;
	}

	void frameRate(int num)
	{
		try
		{
			Thread.sleep(1000 / num);
		}
		catch(InterruptedException e)
		{
			System.err.println("Sleep Error!");
		}
	}
}