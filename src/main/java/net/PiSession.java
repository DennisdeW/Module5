package net;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLSocket;

import packets.AnswerPacket;
import packets.CommandPacket;
import packets.PiPacket;

public class PiSession extends Thread {

	private static volatile int index;

	private final SSLSocket socket;
	private final PiServer server;
	private boolean stop;

	public PiSession(PiServer piServer, SSLSocket socket) {
		this.server = piServer;
		this.socket = socket;
		index = index + 1 % (Integer.MAX_VALUE - 1);
		setName("Session " + index);
		stop = false;
	}

	public void stopSession() {
		stop = true;
		server.unregister(this);
	}

	public void run() {
		InputStream is = null;
		try {
			is = socket.getInputStream();
		} catch (IOException e1) {
			Logger.logError(e1);
			return;
		}
		while (!stop) {
			try {
				if (is.available() > 0) {
					byte[] header = readHeader(is);
					int datalen = PiPacket.getPacketLength(header);
					byte[] raw = new byte[6 + datalen];
					System.arraycopy(header, 0, raw, 0, 6);
					is.read(raw, 6, datalen);
					
					PiPacket packet = PiPacket.readPacket(raw);
					
					switch (packet.getType()) {
					case ANSWER:
						Logger.log("Answer Packet Received:\n\t\t\t" + new String(packet.getData()));
						break;
					case COMPOUND_COMMAND:
					case SINGLE_COMMAND:
						Logger.log("Command packet received: " + new String(packet.getData()));
						AnswerPacket answer = ((CommandPacket) packet).run();
						sendPacket(answer);
						break;
					case FILE:
						//TODO
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
			} catch (IOException e) {
				Logger.logError(e);
			}
		}
	}

	private void sendPacket(AnswerPacket answer) {
		// TODO Auto-generated method stub
		
	}

	private byte[] readHeader(InputStream is) throws IOException {
		int read = 0;
		byte[] header = new byte[6];
		while (read < 6) {
			header[read] = (byte) is.read();
		}
		return header;
	}

}
