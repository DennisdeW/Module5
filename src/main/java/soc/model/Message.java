package soc.model;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Represents an entire file which can be send to the DE1 or is received
 * from the DE1.<br> The size will be a multiple of 128 bits.
 * 
 * @author rvemous
 *
 */
public class Message {

	/**
	 * The size in bytes of one packet. The size of a message must be a
	 * multiple of this size.
	 */
	public static final int PACKET_SIZE = 16; // 16 x 8 = 128 bits
	
	private byte[] messageData;
	private int actualSize;
	private boolean encrypted;
	
	/**
	 * Creates a new message from some data which does not have to be a 
	 * multiple of <code>PACKET_SIZE</code> in size.<br>
	 * It will add random bytes to the end to make it exactly 
	 * <code>PACKET_SIZE</code> times x in size.
	 * 
	 * @param data the data to use
	 * @param encrypted whether the data is encrypted 
	 */
	public Message(byte[] data, boolean encrypted) {
		actualSize = data.length;
		int missingBytes;
		if ((missingBytes = (PACKET_SIZE - (data.length % PACKET_SIZE))) != 0) {
			messageData = new byte[data.length + missingBytes];
			System.arraycopy(data, 0, messageData, 0, data.length);
			byte[] randomBuff = generateRandomBytes(missingBytes);
			System.arraycopy(randomBuff, 0, messageData, data.length, randomBuff.length);
		} else {
			messageData = data;
		}			
	}
	
	/**
	 * Creates a new message from some data which <b>must</b> be a 
	 * multiple of <code>PACKET_SIZE</code> in size.<br>
	 * Because the original data could have been shorter this size must be
	 * provided.
	 * 
	 * @param data the data to use
	 * @param actualSize the actual size of usufull data (starting from 
	 * the first index)
	 * @param encrypted whether the data is encrypted 
	 */
	public Message(byte[] data, int actualSize, boolean encrypted) {
		messageData = data;	
		this.actualSize = actualSize;
		this.encrypted = encrypted;
	}

	/**
	 * Gets the actual data 
	 * @return
	 */
	public byte[] getData() {
		return messageData;
	}
	
	public int getActualSize() {
		return actualSize;
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}
	
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
	@Override
	public String toString() {
		return "Message - encrypted: " + encrypted + 
				", actual size: " + actualSize + 
				", data: " + Arrays.toString(messageData);
	}
	
	/**
	 * Generates a secure random array of bytes.
	 * 
	 * @param amount of bytes to generate
	 * @return the random bytes
	 */
	private byte[] generateRandomBytes(int amount) {
		byte[] buff = new byte[amount];
		byte[] seed = (System.nanoTime() + "").getBytes();
		SecureRandom random = new SecureRandom(seed);
		random.nextBytes(buff);
		return buff;
	}
	
}
