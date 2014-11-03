package net.packets;

public abstract class CommandPacket extends PiPacket {

	private final int size;
	
	protected CommandPacket(int size) {
		this.size = size;
	}
	
	public abstract AnswerPacket run();
	
	@Override
	public byte[] toArray() {
		byte[] header = getHeader(size);
		byte[] packet = new byte[6 + size];
		System.arraycopy(header, 0, packet, 0, header.length);
		byte[] command = getData();
		System.arraycopy(command, 0, packet, header.length, command.length);
		return packet;
	}
	
}
