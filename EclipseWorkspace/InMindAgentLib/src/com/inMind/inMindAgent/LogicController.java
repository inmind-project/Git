package com.inMind.inMindAgent;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import InMind.Consts;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * This class is in-charge of all connections to the server. 
 * It first connects to the server (via TCP), authentication etc.
 * Then receives a port number and connects to it via UDP to stream the audio.
 * 
 * 
 * Created by Amos Azaria on 31-Dec-14.
 */
public class LogicController {


	TCPClient tcpClient;
	AudioStreamer audioStreamer;

	String tcpIpAddr  = "128.2.209.220";
	int tcpIpPort = Consts.serverPort;
	String udpIpAddr;
	int udpIpPort;

	private enum StateSM {Initialized, ConnectedToTCP, StreamingAudio, Stopped };

	private Handler userNotifierHandler; 
	private Handler talkHandler;
	private Handler launchHandler;
	private boolean needToReconnect;
	
	private MessageController messageController;

	public LogicController(Handler userNotifierHandler, Handler talkHandler, Handler launchHandler)
	{
		this.userNotifierHandler = userNotifierHandler;	
		this.talkHandler = talkHandler;
		this.launchHandler = launchHandler;
		messageController = new MessageController();
	}
	
	public void ConnectToServer(String sendThisText)
	{
		closeConnection();
		new connectTask().execute(Consts.sendingText + Consts.commandChar + sendThisText);				
	}

	public void ConnectToServer()
	{
		//if is currently streaming, ignore request.
		if (tcpClient != null && audioStreamer != null && audioStreamer.isStreaming())
			return;
		closeConnection();
		new connectTask().execute(Consts.requestSendAudio+Consts.commandChar);
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
		audioStreamer = new AudioStreamer(udpIpAddr,udpIpPort,userNotifierHandler);
		audioStreamer.startStreaming(); //TODO: must be async!!!
	}

	private void dealWithMessage(String message)
	{
		Log.d("ServerConnector", "Dealing with message:" + message);
		Pattern p = Pattern.compile(Consts.messagePattern);
		Matcher m = p.matcher(message);
		boolean found = m.find();
		Log.d("ServerConnector", "found:" + found);
		if (found)
		{
			if (m.group(1).equalsIgnoreCase(Consts.closeConnection))
			{
				closeConnection();
			}
			if (m.group(1).equalsIgnoreCase(Consts.startNewConnection))
			{
				closeConnection();
				needToReconnect = true;
			}
			if (m.group(1).equalsIgnoreCase(Consts.stopUdp))
			{
				stopStreaming();
				Message msgToast = new Message();
				msgToast.arg1 = 1;
				msgToast.arg2 = 0; //not important
				msgToast.obj = "Wait...";
				userNotifierHandler.sendMessage(msgToast);
			}				
			else if (m.group(1).equalsIgnoreCase(Consts.connectUdp))
			{
				udpIpPort = 0;
				try	{
					udpIpAddr = tcpIpAddr;
					Log.d("ServerConnector", "found:" + found);
					//String protocol = m.group(1);
					udpIpPort = Integer.parseInt(m.group(2).trim());
					Log.d("ServerConnector", "Got port:" + udpIpPort);
				} catch (Exception e)
				{
					Log.e("ServerConnector", "Error parsing message from server...");
				}
				if (udpIpPort > 0)
					openAudioStream();
			}
			else if (m.group(1).equalsIgnoreCase(Consts.sayCommand))
			{
				Log.d("ServerConnector", "saying:" + m.group(2));
				Message msgTalk = new Message();
				msgTalk.arg1 = 1;
				msgTalk.obj = m.group(2).trim();
				talkHandler.sendMessage(msgTalk);
			}
			else if (m.group(1).equalsIgnoreCase(Consts.launchCommand))
			{
				Message msgLaunch = new Message();
				msgLaunch.arg1 = 1;
				msgLaunch.obj = m.group(2).trim();
				launchHandler.sendMessage(msgLaunch);					
			}
			
			String command = m.group(1);
			String args = null;
			if (m.groupCount() > 1)
				args = m.group(2);
			try
			{
				messageController.dealWithMessage(command,args);
			} catch (Exception ex)
			{
				Log.e("messageController.dealWithMessage","command="+command+" args="+args + " " +ex.toString());
				//ex.printStackTrace();
			}
		}
	}


/// connects to TCP server and sends the string as the first messages to send (use: obj.execute(message)).
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
			
			
			try {
				tcpClient.run(message);
			} catch (IOException e) {
				
				Message msgNotConnect = new Message();
				msgNotConnect.arg1 = 1;
				msgNotConnect.arg2 = 1; //important message.
				msgNotConnect.obj = "Could not connect!";
				userNotifierHandler.sendMessage(msgNotConnect);
				
				Log.e("LogicControl", "C: Could not Connect!");
			}

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


	/*
	 * returns whether is reconnecting now.
	 */
	public boolean reconnectIfNeeded() {
		Log.d("LogicControl", "Reconnecting if needed");

		boolean isReconnecting = needToReconnect; 		
		if (needToReconnect)
		{
			needToReconnect = false;
			ConnectToServer();
		}
		return isReconnecting;		
	}


}
