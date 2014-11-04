package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import net.packets.AnswerPacket;
import net.packets.SingleCommandPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

	private SSLSocket socket;
	private OutputStream out;
	private InputStream in;

	static {
		System.setProperty("javax.net.ssl.trustStore", "keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "picloudkeypass");
	}
	
	@Before
	public void setUp() throws IOException, GeneralSecurityException {
		PiServer.main(new String[]{});
		SSLSocketFactory ssl = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socket = (SSLSocket) ssl.createSocket(InetAddress.getLocalHost(),
				20022);
		/*SocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(),
				20022);
		socket.connect(addr, 5000);*/
		out = socket.getOutputStream();
		in = socket.getInputStream();
	}

	@After
	public void tearDown() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//PiServer.stopServer();
	}

	@Test
	public void TestSingleCommands() throws IOException {
		out.write(AnswerPacket.getPacket("Hello World!").toArray());
		out.write(SingleCommandPacket.create("login-Dennis-Wachtwoord").toArray());
		out.write(SingleCommandPacket.create("upload-Dennis-500").toArray());
		out.write(SingleCommandPacket.create("logout-Dennis").toArray());
	}
	
}
