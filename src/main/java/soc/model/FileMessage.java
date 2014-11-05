package soc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileMessage {

	private final Message msg;
	private final File file;
	private final int id;

	public FileMessage(File file, int id, boolean encrypted) throws IOException {
		if (file.length() > Integer.MAX_VALUE)
			throw new IllegalArgumentException("File too long!");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		this.id = id;
		this.file = file;
		msg = new Message(data, encrypted);
	}

	public Message getMessage() {
		return msg;
	}

	public File getFile() {
		return file;
	}

	public int getId() {
		return id;
	}

	// Note: msg is explicitly not part of equals and hashCode, as one file has
	// both an encrypted and a decrypted form.

	@Override
	public boolean equals(Object o) {
		if (o instanceof FileMessage)
			return ((FileMessage) o).id == id;
		return false;
	}

	@Override
	public int hashCode() {
		return id;
	}

}
