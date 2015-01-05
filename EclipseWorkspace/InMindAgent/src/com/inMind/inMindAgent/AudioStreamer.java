package com.inMind.inMindAgent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Amos Azaria on 31-Dec-14.
 */
public class AudioStreamer {

	AudioRecord recorder;

	public byte[] buffer;
	public static DatagramSocket socket;

	private int sampleRate = 44100;
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;  
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	private boolean status = false;

	String ipAddr; 
	int portNum;
	private Handler userNotifierHandler;

	public AudioStreamer(String ipAddr, int portNum, Handler userNotifierHandler)
	{
		this.ipAddr = ipAddr;
		this.portNum = portNum;		
		this.userNotifierHandler = userNotifierHandler;
	}
	
	@Override
	protected
	void finalize()
	{
		stopStreaming();
	}

	public void stopStreaming()
	{
		status = false;
		if (recorder != null)
		{
			recorder.release();
			recorder = null;
		}
		Log.d("VS","Recorder released");
		Message msgNotRecording = new Message();
		msgNotRecording.arg1 = 0;
		userNotifierHandler.sendMessage(msgNotRecording); //set not recording image.
	}

	public void startStreaming() {
		status = true;


		Thread streamThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.d("VS", "Before Creating socket");
					socket = new DatagramSocket();
					Log.d("VS", "Socket Created.");
					int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

					Log.d("VS", "minBufSize:" + minBufSize);
					byte[] buffer = new byte[minBufSize];

					Log.d("VS","Buffer created of size " + minBufSize);
					DatagramPacket packet;

					final InetAddress destination = InetAddress.getByName(ipAddr);
					Log.d("VS", "Address retrieved");


					recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
					Log.d("VS", "Recorder initialized");

					recorder.startRecording();

					//Added to remove initial noise which Android (or at least Nexus 5) seems to have at the beginning. //can try using noise removal algorithms instead, look at Audacity.
//					Message msgWait = new Message();
//					msgWait.arg1 = 1;
//					msgWait.obj = "Wait!";
//					toasterHandler.sendMessage(msgWait);
//
//					Thread.sleep(1600);

					Message msgTalk = new Message();
					msgTalk.arg1 = 1;
					msgTalk.arg2 = 1; //important toast
					msgTalk.obj = "Talk!";
					userNotifierHandler.sendMessage(msgTalk);
					//Message msgPlayTone = new Message();
					//msgPlayTone.arg1 = 2;					
					//toasterHandler.sendMessage(msgPlayTone);

//					Thread.sleep(300);
//					//couldn't find a better way to clear buffer.
//					byte[] tmpbuffer = new byte[minBufSize*1000];
//					recorder.read(tmpbuffer, 0, minBufSize*1000);
//					tmpbuffer=null;

					while(status == true) {


						//reading data from MIC into buffer
						minBufSize = recorder.read(buffer, 0, buffer.length);

						//putting buffer in the packet
						packet = new DatagramPacket (buffer, buffer.length, destination, portNum);

						socket.send(packet);
						System.out.println("Send_Packet: " +minBufSize);
					}

					socket.close();
					Log.d("VS", "Socket Closed");


				} catch(UnknownHostException e) {
					Log.e("VS", "UnknownHostException");
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("VS", "IOException");
				} //catch (InterruptedException e) {
					//e.printStackTrace();
					//Log.e("VS", "InterruptedException");
				//} 

			}

		});
		streamThread.start();
	}


}
