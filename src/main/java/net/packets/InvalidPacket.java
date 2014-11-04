package net.packets;

public class InvalidPacket extends PiPacket {

	private int arg;
	
	public InvalidPacket(int length) {
		arg =length;
	}

	@Override
	public byte[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PiPacketType getType() {
		return PiPacketType.INVALID;
	}

	@Override
	public String toString() {
		return "[I|" + arg + "]";
	}

}
