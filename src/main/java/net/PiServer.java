package net;

import global.Logger;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.jna.Platform;

import soc.controller.SocManager;

public class PiServer extends Thread {
	public static final SSLServerSocket SOCKET;
	private static PiServer instance;

	private final Set<PiSession> activeSessions;

	private boolean stop;

	static {
		System.setProperty("javax.net.ssl.keyStore", "keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "picloudkeypass");
		Logger.init();
		if (Platform.isLinux())
			SocManager.init();
		File storage = new File("storage"), temp = new File("temp");
		if (!storage.isDirectory())
			storage.mkdir();
		if (!temp.isDirectory())
			temp.mkdir();
		SSLServerSocket t = null;
		Logger.log("Starting server on port 20022");
		try {
			SSLServerSocketFactory ssl = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			t = (SSLServerSocket) ssl.createServerSocket(20022);
		} catch (IOException e) {
			Logger.logError("Could not create Server Socket!\n" + e);
			System.exit(1);
		}
		SOCKET = t;
		try {
			SOCKET.setSoTimeout(1000);
		} catch (SocketException e) {
			Logger.logError("Could not set socket timeout: " + e);
		}
		Logger.log("Socket created.");
	}

	@Override
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
			if (client != null) {
				Logger.log("New client accepted: " + client.getInetAddress());
				PiSession session = new PiSession(this, client);
				activeSessions.add(session);
				session.start();
			}
		}
		Logger.log("Server stopped.");
	}

	public static void stopServer() {
		Logger.log("Stopping Server.");
		instance.activeSessions.forEach(s -> s.stopSession());
		instance.stop = true;
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

	public static void main(String[] args) throws IOException {
		instance = new PiServer();
		instance.start();
	}
}
