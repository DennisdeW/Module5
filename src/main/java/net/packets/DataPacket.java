package net.packets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataPacket extends PiPacket {

	private final byte[] file;
	
	DataPacket(byte[] data) {
		file = new byte[data.length - 6];
		System.arraycopy(data, 0, file, 0, file.length);
	}
	
	@Override
	public byte[] toArray() {
		byte[] header = getHeader(file.length);
		byte[] packet = new byte[header.length + file.length];
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(file, 0, packet, header.length, file.length);
		return packet;
	}

	@Override
	public byte[] getData() {
		return file;
	}

	@Override
	public PiPacketType getType() {
		return PiPacketType.FILE;
	}
	
	public File saveToFile(String path) throws IOException {
		File file = new File(path);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(this.file);
		fos.close();
		return file;
	}
	
	public static DataPacket fromFile(File source) throws IOException {
		if (!source.canRead())
			throw new IOException("Can't read source file!");
		FileInputStream fis = new FileInputStream(source);
		byte[] data = new byte[(int) source.length()];
		fis.read(data);
		fis.close();
		return new DataPacket(data);
	}

	@Override
	public String toString() {
		return "[D|" + file.length + "]";
	}
}
