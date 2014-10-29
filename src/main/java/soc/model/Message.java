package soc.model;

import global.Tools;

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
			byte[] randomBuff = Tools.generateRandomBytes(missingBytes);
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
	 * Gets the data of the message.
	 *  
	 * @return the data
	 */
	public byte[] getData() {
		return messageData;
	}
	
	/**
	 * Gets the actual size of the data in bytes.<br>
	 * 
	 * @return the actual size
	 */
	public int getActualSize() {
		return actualSize;
	}
	
	/**
	 * Gets whether the data is encrypted or not.
	 * 
	 * @return whether the data is encrypted or not
	 */
	public boolean isEncrypted() {
		return encrypted;
	}
	
	/**
	 * Sets whether the data is encrypted or not.<br>
	 * Should only be used when it is actually encrypted or decrypted by 
	 * the DE1.
	 * 
	 * @param encrypted whether the data is encrypted or not
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
	/**
	 * A string representation of the message.
	 */
	@Override
	public String toString() {
		return "Message - encrypted: " + encrypted + 
				", actual size: " + actualSize + 
				", data: " + Arrays.toString(messageData);
	}
	
	/**
	 * A string representation of the message which does not write out the
	 * whole array of data.
	 */	
	public String toString(boolean compact) {
		if (compact) {
			return "Message - encrypted: " + encrypted + 
					", actual size: " + actualSize + 
					", data: " + messageData.length + " bytes";
		} else {
			return toString();
		}

	}
	
	
}
