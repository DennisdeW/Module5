package net.packets;

import java.util.Arrays;

public abstract class PiPacket {

	public static final byte[] MAGIC = new byte[] { 80, 73 };

	public abstract byte[] toArray();

	public abstract byte[] getData();

	public abstract PiPacketType getType();

	@Override
	public abstract String toString();

	protected byte[] getHeader(int length) {
		byte[] res = new byte[6];
		System.arraycopy(MAGIC, 0, res, 0, 2);
		res[2] = (byte) (length >> 24 & 0x3F); // Clear two most significant
												// bits
		res[2] |= (getType().getId() & 0xFF) << 6;
		res[3] = (byte) (length >> 16 & 0xFF);
		res[4] = (byte) (length >> 8 & 0xFF);
		res[5] = (byte) (length & 0xFF);
		return res;
	}

	public static PiPacketType getType(byte[] header) {
		byte relevant = header[2];
		relevant >>>= 6;
		relevant &= 3;
		return PiPacketType.forId(relevant);
	}

	public static int getPacketLength(byte[] header) {
		byte[] relevant = new byte[4];
		System.arraycopy(header, 2, relevant, 0, 4);
		int length = relevant[0] & 0x3F;
		length <<= 8;
		length += relevant[1] & 0xFF;
		length <<= 8;
		length += relevant[2] & 0xFF;
		length <<= 8;
		length += relevant[3] & 0xFF;
		return length;
	}

	public static PiPacket readPacket(byte[] data) {
		PiPacketType type = getType(data);
		if (Arrays.equals(new byte[] { data[0], data[1] }, MAGIC))

			return type.getPacket(data);
		return new InvalidPacket(data.length);
	}

	public enum PiPacketType {
		SINGLE_COMMAND(0), COMPOUND_COMMAND(1), FILE(2), ANSWER(3), INVALID(99);

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
				if (data.length > 50)
					return new AnswerPacket("<<Too long>>");
				byte[] message = new byte[data.length - 6];
				System.arraycopy(data, 6, message, 0, message.length);
				return new AnswerPacket(new String(message));
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
