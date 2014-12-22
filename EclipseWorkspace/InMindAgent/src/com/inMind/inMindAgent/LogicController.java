package com.inMind.inMindAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * This class is in-charge of all connections to the server. 
 * It first connects to the server (via TCP), authentication etc.
 * Then receives a port number and connects to it via UDP to stream the audio.
 */
public class LogicController {

	static final String messageSeperator = "\\^";

	TCPClient tcpClient;
	AudioStreamer audioStreamer;

	String tcpIpAddr  = "128.2.209.220";
	int tcpIpPort = 4444;
	String udpIpAddr;
	int udpIpPort;

	private enum StateSM {Initialized, ConnectedToTCP, StreamingAudio, Stopped };

	private Handler toasterHandler; 
	private Handler talkHandler;
	private Handler launchHandler;

	public LogicController(Handler toasterHandler, Handler talkHandler, Handler launchHandler)
	{
		this.toasterHandler = toasterHandler;	
		this.talkHandler = talkHandler;
		this.launchHandler = launchHandler;
	}

	public void ConnectToServer()
	{
		if (tcpClient != null)
		{
			tcpClient.closeConnection();
			tcpClient = null;
		}
		// connect to the server
		new connectTask().execute("");
	}

	public void closeConnection() 
	{
		stopStreaming();
		if (tcpClient != null)
		{
			tcpClient.closeConnection();
			tcpClient = null;
		}
	}

	public void stopStreaming() 
	{
		if (audioStreamer != null)
			audioStreamer.stopStreaming();		
	}

	public void changeInitIpAddr(String newIpAddr)
	{
		closeConnection();
		tcpIpAddr = newIpAddr;	
	}

	public void changeInitPort(int newPort)
	{
		tcpIpPort = newPort;	
	}

	private void openAudioStream()
	{
		audioStreamer = new AudioStreamer(udpIpAddr,udpIpPort,toasterHandler);
		audioStreamer.startStreaming(); //TODO: must be async!!!
	}

	private void dealWithMessage(String message)
	{
		Log.d("ServerConnector", "Dealing with message:" + message);
		Pattern p = Pattern.compile("(\\p{Alpha}*)"+messageSeperator+"(.*)");
		Matcher m = p.matcher(message);
		boolean found = m.find();
		Log.d("ServerConnector", "found:" + found);
		if (found)
		{
			if (m.group(1).equalsIgnoreCase("ConnectUDP"))
			{
				udpIpPort = 0;
				try	{
					udpIpAddr = tcpIpAddr;
					Log.d("ServerConnector", "found:" + found);
					//String protocol = m.group(1);
					udpIpPort = Integer.parseInt(m.group(2));
					Log.d("ServerConnector", "Got port:" + udpIpPort);
				} catch (Exception e)
				{
					Log.e("ServerConnector", "Error parsing message from server...");
				}
				if (udpIpPort > 0)
					openAudioStream();
			}
			else if (m.group(1).equalsIgnoreCase("Say"))
			{
				Log.d("ServerConnector", "saying:" + m.group(2));
				Message msgTalk = new Message();
				msgTalk.arg1 = 1;
				msgTalk.obj = m.group(2).trim();
				talkHandler.sendMessage(msgTalk);
			}
			else if (m.group(1).equalsIgnoreCase("Launch"))
			{
				Message msgLaunch = new Message();
				msgLaunch.arg1 = 1;
				msgLaunch.obj = m.group(2).trim();
				launchHandler.sendMessage(msgLaunch);					
			}

		}
	}



	public class connectTask extends AsyncTask<String,String,TCPClient> {

		@Override
		protected TCPClient doInBackground(String... message) {

			//we create a TCPClient object and
			tcpClient = new TCPClient(tcpIpAddr, tcpIpPort, new TCPClient.OnMessageReceived() {
				@Override
				//here the messageReceived method is implemented
				public void messageReceived(String message) {
					dealWithMessage(message); //TODO: make sure that runs on original thread. (avoid multithread unsafe access).
					//publishProgress(message);//this method calls the onProgressUpdate
				}
			});
			tcpClient.run();

			return null;
		}

		//        @Override
		//        protected void onProgressUpdate(String... values) {
		//            super.onProgressUpdate(values);
		// 
		//            //in the arrayList we add the messaged received from server
		//            arrayList.add(values[0]);
		//            // notify the adapter that the data set has changed. This means that new message received
		//            // from server was added to the list
		//            mAdapter.notifyDataSetChanged();
		//        }
	}


}
