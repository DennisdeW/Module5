package net;

import global.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class PiServer extends Thread {
	public static final SSLServerSocket SOCKET;

	private final Set<PiSession> activeSessions;

	private boolean stop;

	static {
		SSLServerSocket t = null;
		Logger.log("Starting server on port 20022");
		try {
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			t = (SSLServerSocket) factory.createServerSocket(20022, 0,
					InetAddress.getLocalHost());
		} catch (IOException e) {
			Logger.logError("Could not create Server Socket!\n" + e);
			System.exit(1);
		}
		SOCKET = t;
		SOCKET.setWantClientAuth(true);
		try {
			SOCKET.setSoTimeout(1000);
		} catch (SocketException e) {
			Logger.logError("Could not set socket timeout: " + e);
		}
		Logger.log("Socket created.");
	}

	public void run() {
		Logger.log("Starting server loop.");
		while (!stop) {
			SSLSocket client = null;
			try {
				client = (SSLSocket) SOCKET.accept();
			} catch (SocketTimeoutException e) {
				// do nothing, this is normal
				continue;
			} catch (IOException e) {
				Logger.logError("Exception accepting connection: " + e);
			}
			Logger.log("New client accepted: " + client.getInetAddress());
			PiSession session = new PiSession(this, client);
			activeSessions.add(session);
			session.start();
		}
		Logger.log("Server stopped.");
	}

	public void stopServer() {
		Logger.log("Stopping Server.");
		activeSessions.forEach(s -> s.stopSession());
		stop = true;
		try {
			SOCKET.close();
		} catch (IOException e) {
			Logger.logError("Could not close server socket: " + e);
		}
	}

	void unregister(PiSession session) {
		activeSessions.remove(session);
	}

	private PiServer() {
		stop = false;
		activeSessions = new HashSet<>();
		setName("PiServer");
	}

	public static void main(String[] args) {
		new PiServer().start();
	}
}
