package com.github.euwoyne.enigma_edit.control;

import java.util.concurrent.LinkedBlockingQueue;

class UpdateThread
{
	private Thread                          thread;
	private LinkedBlockingQueue<Updateable> queue;
	
	UpdateThread()
	{
		queue = new LinkedBlockingQueue<Updateable>();
	}
	
	public void stop()                       {thread.interrupt();} 
	public void scheduleUpdate(Updateable u) {queue.offer(u);}
	
	public void start() throws IllegalThreadStateException
	{
		if (thread == null)
		{
			thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Updateable[] buffer = new Updateable[8];
					for (;;)
					{
						try
						{
							buffer[0] = queue.take();
							for (int i = 1; i < buffer.length; ++i)
							{
								if ((buffer[i] = queue.poll()) == null)
								{
									break;
								}
								
								for (int j = 0; j < i; ++j)
								{
									if (buffer[i] == buffer[j])
									{
										--i;
										break;
									}
								}
							}
							for (int i = 0; i < buffer.length; ++i)
							{
								if (buffer[i] == null) break;
								buffer[i].update();
								buffer[i] = null;
							}
						}
						catch (InterruptedException e)
						{
							break;
						}
					}
				}
			});
		}
		thread.start();
	}
}

