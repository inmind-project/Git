import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class StreamAudioClient {

	private static String ipAddr = "128.2.209.220";
	private static int port = 50005;
	public static DatagramSocket socket;

	static int minBufSize = 2048;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		socket = new DatagramSocket();
		Random randomGenerator = new Random();

		Path path = Paths.get(URI.create("file:///C:/InMind/temp/TellMeAboutYourFamily.raw"));
		byte[] data = Files.readAllBytes(path);

		for (int i = 0; i < data.length; i += minBufSize) {
			int bytesLeft = data.length - i;
			int bytesToSend = bytesLeft < minBufSize ? bytesLeft:minBufSize;
			byte[] buffer = new byte[bytesToSend];
			System.arraycopy(data, i, buffer, 0, bytesToSend);

			if (randomGenerator.nextDouble() < 0.1)//simulate a dropped packet
				continue;
			DatagramPacket packet;

			final InetAddress destination = InetAddress.getByName(ipAddr);

			// putting buffer in the packet
			packet = new DatagramPacket(buffer, buffer.length, destination,
					port);

			socket.send(packet);
			System.out.println("Send_Packet: " + bytesToSend);
			Thread.sleep(10);
		}

	}

}
