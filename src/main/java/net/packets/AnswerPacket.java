package net.packets;

public class AnswerPacket extends PiPacket {

	private final String message;

	AnswerPacket(String message) {
		this.message = message;
	}

	@Override
	public byte[] toArray() {
		byte[] header = getHeader(message.length());
		byte[] packet = new byte[6 + message.length()];
		System.arraycopy(header, 0, packet, 0, 6);
		byte[] command = getData();
		System.arraycopy(command, 0, packet, 6, command.length);
		return packet;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public byte[] getData() {
		return message.getBytes();
	}

	@Override
	public PiPacketType getType() {
		return PiPacketType.ANSWER;
	}

	@Override
	public String toString() {
		return "[A|" + message + "]";
	}

	public static AnswerPacket getPacket(String message) {
		return new AnswerPacket(message);
	}
}
