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
	 * Default size in bytes of one packet.<br> The size of a message is
	 * allowed to be be 16, 32, 64 or 128 bytes.
	 */
	public static final int DEFAULT_PACKET_SIZE = 128;
	
	private byte[] messageData;
	private int actualSize;
	private boolean encrypted;
	private int packetSize;
	
	/**
	 * Creates a new message fit to be send to the DE1 for 
	 * encryption/decryption.<br>
	 * 
	 * @param data the data to use
	 * @param actualSize the actual size of the data from index 0, 
	 * useful when the data has already been padded with random data
	 * @param encrypted whether the data is encrypted data or plain text
	 * @param packetSize the packet size to use, when it the data length 
	 * is unequal to  a multiple of this value it will be padded with 
	 * securely-random bytes at the end
	 */
	public Message(byte[] data, int actualSize, boolean encrypted, int packetSize) {
		this.actualSize = actualSize;
		this.encrypted = encrypted;
		this.packetSize = packetSize;
		int missingBytes;
		if ((missingBytes = (packetSize - (data.length % packetSize))) != 0) {
			if (data.length != actualSize) {
				byte[] tempData = data;
				data = new byte[actualSize];
				System.arraycopy(tempData, 0, data, 0, data.length);
			}
			messageData = new byte[data.length + missingBytes];
			System.arraycopy(data, 0, messageData, 0, data.length);
			byte[] randomBuff = Tools.generateRandomBytes(missingBytes);
			System.arraycopy(randomBuff, 0, messageData, data.length, randomBuff.length);
		} else {
			messageData = data;
		}			
	}
	
	/**
	 * Creates a new message fit to be send to the DE1 for 
	 * encryption/decryption with the default packet size.<br>
	 * 
	 * @param data the data to use
	 * @param actualSize the actual size of the data from index 0, 
	 * useful when the data has already been padded with random data
	 * @param encrypted whether the data is encrypted data or plain text
	 */
	public Message(byte[] data, int actualSize, boolean encrypted) {
		this(data, actualSize, encrypted, DEFAULT_PACKET_SIZE);
	}
	
	/**
	 * Creates a new message fit to be send to the DE1 for 
	 * encryption/decryption where <code>data</code> is unpadded.<br>
	 * 
	 * @param data the data to use
	 * @param encrypted whether the data is encrypted data or plain text
	 * @param packetSize the packet size to use, when it the data length 
	 * is unequal to  a multiple of this value it will be padded with 
	 * securely-random bytes at the end
	 */
	public Message(byte[] data, boolean encrypted, int packetSize) {
		this(data, data.length, encrypted, packetSize);
	}
	
	/**
	 * Creates a new message fit to be send to the DE1 for 
	 * encryption/decryption with the default packet size and where
	 * <code>data</code> is unpadded.<br>
	 * 
	 * @param data the data to use
	 * @param encrypted whether the data is encrypted data or plain text
	 */
	public Message(byte[] data, boolean encrypted) {
		this(data, data.length, encrypted, DEFAULT_PACKET_SIZE);
	}
	
	/**
	 * Creates a new message fit to be send to the DE1 for 
	 * encryption/decryption with the default packet size, where
	 * <code>data</code> is unpadded and unencrypted.<br>
	 * 
	 * @param data the data to use
	 */
	public Message(byte[] data) {
		this(data, data.length, false, DEFAULT_PACKET_SIZE);
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
	 * Gets the actual data of the message.
	 * 
	 * @return the actual data
	 */
	public byte[] getActualData() {
		byte[] actualData = new byte[actualSize];
		System.arraycopy(messageData, 0, actualData, 0, actualSize);
		return actualData;
	}
	
	/**
	 * Gets random padding of data if any.
	 */
	public byte[] getPaddingOfData() {
		byte[] padding = new byte[messageData.length - actualSize];
		if (padding.length == 0) {
			return padding;
		}
		System.arraycopy(messageData, actualSize, padding, 0, padding.length);
		return padding;
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
	 * Gets the packet size used for this message.<br>
	 * The size of the data will be a multiple of this value.
	 * 
	 * @return the packet size
	 */
	public int getPacketSize() {
		return packetSize;
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
	 * A string representation of the message.
	 * 
	 * @param compact if true only writes out the length of the data
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
