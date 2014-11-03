package packets;

public class AnswerPacket extends PiPacket {

	private final String message;
	
	AnswerPacket(String message) {
		this.message = message;
	}
	
	@Override
	public byte[] toArray() {
		byte[] header = getHeader(message.length());
		byte[] packet = new byte[header.length + message.length()];
		System.arraycopy(header, 0, packet, 0, header.length);
		byte[] command = getData();
		System.arraycopy(command, 0, packet, header.length, command.length);
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

}
