package net.packets;

public abstract class PiPacket {

	public static final byte[] MAGIC = "PI".getBytes();
	
	public abstract byte[] toArray();
	public abstract byte[] getData();
	public abstract PiPacketType getType();
	public abstract String toString();
	
	protected byte[] getHeader(int length) {
		byte[] res = new byte[6 + length];
		System.arraycopy(MAGIC, 0, res, 0, 2);
		int header = getType().getId();
		header <<= 30;
		header += (length & 0x3FFFFFFF); //Clear two most significant bits
		for (int i = 0; i < 4; i++)
			res[3+i] = (byte) ((header >> (32-(i * 8))) & 0xFF);
		return res;
	}
	
	public static PiPacketType getType(byte[] header) {
		byte relevant = header[2];
		relevant >>= 30;
		return PiPacketType.forId(relevant);
	}
	
	public static int getPacketLength(byte[] header) {
		byte[] relevant = new byte[4];
		System.out.print('[');
		for (byte b : header)
			System.out.print(b +",");
		System.out.println(']');
		System.arraycopy(header, 2, relevant, 0, 4);
		int length = relevant[0] & 0x3F;
		length <<= 8;
		length += ((int) relevant[1] & 0xFF);
		length <<= 8;
		length += ((int) relevant[1] & 0xFF);
		length <<= 8;
		length += ((int) relevant[1] & 0xFF);
		return length;
	}
	
	public static PiPacket readPacket(byte[] data) {
		PiPacketType type = getType(data);
		return type.getPacket(data);
	}
	
	public enum PiPacketType {
		SINGLE_COMMAND(0), COMPOUND_COMMAND(1), FILE(2), ANSWER(3);
		
		private final int id;
		
		private PiPacketType(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public PiPacket getPacket(byte[] data) {
			switch (this) {
			case ANSWER:
				return new AnswerPacket(new String(data));
			case COMPOUND_COMMAND:
				return new MultiCommandPacket(data);
			case FILE:
				return new DataPacket(data);
			case SINGLE_COMMAND:
				return new SingleCommandPacket(data);
			default:
				throw new Error("Impossible");			
			}
		}
		
		public static PiPacketType forId(int id) {
			for (PiPacketType t : values())
				if (t.id == id)
					return t;
			return null;
		}
	}
}
