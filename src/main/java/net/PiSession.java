package net;

import files.FileDescriptor;
import files.NullCrypto;
import global.Logger;
import global.PiCloudConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLSocket;

import ssh.command.CheckUploadCommand;
import ssh.command.DownloadCommand;
import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;
import net.packets.AnswerPacket;
import net.packets.CommandPacket;
import net.packets.DataPacket;
import net.packets.PiPacket;

public class PiSession extends Thread {

	private static volatile int index;
	private static final Map<String, Boolean> LOGGED_IN;
	private static final ThreadLocal<String> USERNAME = ThreadLocal
			.withInitial(() -> "<<not logged in>>");

	static {
		LOGGED_IN = new HashMap<>();
		try {
			for (String name : UserStatementMaker.getUserNameList())
				LOGGED_IN.put(name, false);
		} catch (SQLException e) {
			Logger.logError(e);
			System.exit(1);
		}
	}

	private final SSLSocket socket;
	private final PiServer server;
	private boolean stop;

	public PiSession(PiServer piServer, SSLSocket socket) {
		this.server = piServer;
		this.socket = socket;
		index = (index + 1) % (Integer.MAX_VALUE - 1);
		setName("Session " + index);
		stop = false;
	}

	public void stopSession() {
		stop = true;
		try {
			sendPacket(AnswerPacket.getPacket("Terminating Session"));
		} catch (IOException e) {
			Logger.logError(e);
		}
		server.unregister(this);
		try {
			socket.close();
		} catch (IOException e) {
			Logger.logError(e);
		}
	}

	public void run() {
		Logger.log("Starting session for " + socket.getInetAddress());
		/*
		 * try { sendPacket(AnswerPacket.getPacket("Hallo Rob!")); } catch
		 * (IOException e2) { e2.printStackTrace(); }
		 */
		InputStream is = null;
		try {
			is = new BufferedInputStream(socket.getInputStream());
		} catch (IOException e1) {
			Logger.logError(e1);
			return;
		}
		while (!stop) {
			try {
				byte[] header = new byte[6];
				if (is.read(header) > 0) {
					int datalen = PiPacket.getPacketLength(header);
					byte[] raw = new byte[6 + datalen];
					System.arraycopy(header, 0, raw, 0, 6);
					int i = 6;
					while (i < datalen
							&& (i += is.read(raw, i, datalen + 6 - i)) != -1) {
					}
					// System.out.println(is.read(raw, 6, datalen));
					PiPacket packet = PiPacket.readPacket(raw);
					switch (packet.getType()) {
					case ANSWER:
						Logger.log("Answer Packet Received:\n\t\t\t"
								+ new String(packet.getData()));
						break;
					case COMPOUND_COMMAND:
					case SINGLE_COMMAND:
						Logger.log("Command packet received: "
								+ new String(packet.getData()));
						AnswerPacket answer = ((CommandPacket) packet).run();
						sendPacket(answer);
						if (DownloadCommand.packet != null) {
							sendPacket(DownloadCommand.packet);
							DownloadCommand.packet = null;
						}
						break;
					case FILE:
						// TODO
						Logger.log("Receiving file...");
						File encrypted = PiCloudConstants.CRYPTO_IMPL
								.encrypt(packet.getData());
						try {
							FileStatementMaker
									.addDescriptor(new FileDescriptor(encrypted
											.getName(), UserStatementMaker
											.getId(USERNAME.get()), packet
											.getData().length));
						} catch (SQLException | UnknownUserException e) {
							Logger.logError(e);
						}
						Logger.log("File Saved! -- " + encrypted.getName());
						sendPacket(AnswerPacket.getPacket(encrypted.getName()));
						break;
					case INVALID:
						Logger.logError("Invalid Packet: " + packet.toString());
						break;
					default:
						throw new Error("Impossible");
					}

				} else {
					try {
						sleep(200);
					} catch (InterruptedException e) {
					}
				}
			} catch (SocketException e) {
				server.unregister(this);
				Logger.log("Client terminated connection.");
				return;
			} catch (IOException e) {
				Logger.logError(e);
				stopSession();
			}
		}
	}

	public static synchronized void logIn(String user) {
		Logger.log(user + " logged in.");
		LOGGED_IN.put(user, true);
		USERNAME.set(user);
	}

	public static synchronized void logOut() {
		Logger.log(USERNAME.get() + " logged out.");
		LOGGED_IN.put(USERNAME.get(), false);
		USERNAME.remove();
	}

	public static synchronized boolean isLoggedIn(String user) {
		return LOGGED_IN.containsKey(user) && LOGGED_IN.get(user);
	}

	private void printPacket(byte[] raw) {
		System.out.print("[");
		for (byte b : raw)
			System.out.print(b + "|");
		System.out.println("]");
	}

	private void sendPacket(PiPacket answer) throws IOException {
		Logger.log("Sending packet: " + answer);
		OutputStream out = socket.getOutputStream();
		out.write(answer.toArray());
	}

	public static synchronized String getUser() {
		return USERNAME.get();
	}
}
