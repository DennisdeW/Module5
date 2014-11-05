package soc.controller;

import files.Crypto;
import global.Logger;
import global.Timer;
import global.Tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import soc.model.GPIOSettings;
import soc.model.Message;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.wiringpi.Spi;

import files.Crypto;
import global.Logger;
import global.Timer;
import global.Tools;

/**
 * Represents the connection between the Pi and the DE1 SoC.<br>
 * It can be used to encrypt or decrypt data and in the future also for
 * compressing and decompressing the data.
 *
 * @author rvemous
 */
public class SocManager implements Crypto {

	private static final int DEFAULT_CONNECTION_SPEED = 32000000;
	private static final int DEFAULT_BUFFER_SIZE = 1000;
	private static final int DEFAULT_TIMEOUT = 2000;
	
	private SecureRandom secure;
	
	public static SocManager instance;

	private static ReentrantLock lock;
	private static GpioController GPIO;

	private GPIOSettings settings;

	/**
	 * Creates a new gpio connection.
	 *
	 * @param settings
	 *            the settings to use
	 * @param connectionSpeed
	 *            the speed in bits/sec of the connection, this can be between
	 *            3.000 and 32.000.000 bits a second
	 * @param bufferSize
	 *            the size in KB of the SPI receive buffer
	 */
	public SocManager(GPIOSettings settings, int connectionSpeed, int bufferSize) {
		if (lock == null)
			lock = new ReentrantLock();
		if (GPIO == null) {
			Timer timer = new Timer(3000, true);
			while (Spi.wiringPiSPISetup(0, connectionSpeed) == -1) {
				try {
					Runtime.getRuntime().exec("gpio load spi " + bufferSize);
				} catch (IOException e) {
					Logger.logError("Could not load SPI with specified "
							+ "buffer: " + bufferSize);
					break;
				}
				Tools.waitForMs(100);
				if (timer.hasExpired())
					Logger.logError("Could not set up connection: "
							+ "timed out");
			}
			GPIO = GpioFactory.getInstance();
		}
		this.settings = settings;
		secure = new SecureRandom();
	}

	/**
	 * Gets the gpio settings.
	 *
	 * @return the settings
	 */
	public GPIOSettings getSettings() {
		return settings;
	}

	/**
	 * Sets the gpio settings.
	 *
	 * @param settings
	 *            to use
	 */
	public synchronized void setSettings(GPIOSettings settings) {
		this.settings = settings;
	}

	@Override
	public File decrypt(byte[] cipher) {
		Message msg = new Message(cipher);
		Message plain = msg;//sendAndReceiveData(msg, false, 2000);
		File file = new File("storage/" + (secure.nextLong() & 0x7FFFFFFFFFFFFFFFL));
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(plain.getData());
		} catch (IOException e) {
			Logger.logError(e);
		}
		return file;
	}

	@Override
	public File encrypt(byte[] plain) {
		Message msg = new Message(plain);
		Message cipher = msg;//sendAndReceiveData(msg, true, 2000);
		File file = new File("storage/" + (secure.nextLong() & 0x7FFFFFFFFFFFFFFFL));
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(cipher.getData());
		} catch (IOException e) {
			Logger.logError(e);
		}
		return file;
	}
	
	/**
	 * Sends the data to the DE1 SoC for encryption/decryption.
	 *
	 * @param data
	 *            to send
	 * @param encrypt
	 *            whether the DE1 should encrypt or decrypt the data
	 * @param timeout
	 *            the max time in ms to wait for the DE1 to answer
	 * @return the response message from the DE1
	 */
	public synchronized Message sendAndReceiveData(Message msg,
			boolean encrypt, long timeout) {
		return sendAndReceiveData(msg, encrypt, timeout, false);
	}

	/**
	 * Sends the data to the DE1 SoC for encryption/decryption or sends to self
	 * (MOSI connected to MISO).<br>
	 * If it sends to self the return message will be equal to <code>msg</code>.
	 *
	 * @param data
	 *            to send
	 * @param encrypt
	 *            whether the DE1 should encrypt or decrypt the data
	 * @param timeout
	 *            the max time in ms to wait for the DE1 to answer
	 * @param selfSending
	 *            whether the communication is done with the Pi itself or the
	 *            DE1
	 * @return the response message from the DE1
	 */
	public synchronized Message sendAndReceiveData(Message msg,
			boolean encrypt, long timeout, boolean selfSending) {
		if (!getLock(timeout))
			return null;
		boolean fail = false;
		int maxBuff = msg.getPacketSize();
		// wait till DE1 is not sending
		if (!waitForPin(settings.getDe1ActivePin(GPIO), false, timeout))
			System.out.println("Timeout");
		// set encryption/decryption command pin
		settings.getCommandPin(GPIO).setState(!encrypt);
		// tell the DE1 the Pi is going to send
		settings.getPiActivePin(GPIO).high();
		// send data to DE1 for encryption/decryption and receive data
		System.out.println("Sending MSG: " + Arrays.toString(msg.getData()));
		System.out.println("MSG len: " + msg.getData().length);
		System.out.println("Max buffer: " + maxBuff);
		System.out.println("");
		int readIndex = 0;
		byte[] buffer = new byte[maxBuff];
		byte[] allZeros = new byte[maxBuff];
		byte[] receivedBytes = new byte[msg.getData().length];
		Arrays.fill(allZeros, (byte) 0);
		Timer timer = new Timer(timeout, true);
		for (int i = 0; i < msg.getData().length; i += maxBuff) {
			try {
				System.arraycopy(msg.getData(), i, buffer, 0, maxBuff);
			} catch (ArrayIndexOutOfBoundsException e) {
				Logger.logError(e);
			}
			System.out.print("Send data (" + i + " to " + (i + maxBuff)
					+ "): [");
			for (byte b : buffer)
				System.out.print(Integer.toBinaryString(b & 0xFF) + ", ");
			System.out.println("]");
			// try to send data
			boolean sendSuccesfull = true;
			if (Spi.wiringPiSPIDataRW(0, buffer, maxBuff) == -1) {
				System.out.println(true);
				Logger.logError("Could not send data to DE1: "
						+ Arrays.toString(buffer));
				i -= maxBuff;
				sendSuccesfull = false;
			}
			if (sendSuccesfull) {
				System.out.println("success 1");
				System.out.println("High: "
						+ settings.getDe1ActivePin(GPIO).isHigh());
				System.out.println("Zero: "
						+ Tools.allEqualTo(buffer, (byte) 0));
				// check for response from DE1
				if (!Tools.allEqualTo(buffer, (byte) 0)
						&& (selfSending || settings.getDe1ActivePin(GPIO)
								.isHigh())) {
					System.out.println("Selfs: " + selfSending);
					System.out.print("Got data (" + readIndex + " to "
							+ (readIndex + maxBuff) + "): [");
					for (byte b : buffer)
						System.out.print(Integer.toBinaryString(b & 0xFF)
								+ ", ");
					System.out.println("]");
					System.arraycopy(buffer, 0, receivedBytes, i, maxBuff);
					readIndex += maxBuff;
					timer.restart();
				}
			}
			// check for time-out
			if (timer.hasExpired()) {
				System.out.println("Expired 1");
				Logger.logError("Timed out: " + timer.toString()
						+ Arrays.toString(buffer));
				// tell the DE1 the Pi is done sending
				settings.getPiActivePin(GPIO).low();
				releaseLock();
				return null;
			}
		}
		// tell the DE1 the Pi is done sending
		settings.getPiActivePin(GPIO).low();
		// receive pending packages from DE1
		timer.restart();
		for (; readIndex < msg.getData().length;) {
			try {
				System.arraycopy(allZeros, 0, buffer, 0, maxBuff);
			} catch (ArrayIndexOutOfBoundsException e) {
				Logger.logError(e);
			}
			Spi.wiringPiSPIDataRW(0, buffer, maxBuff);
			// check for response from DE1
			if (!Tools.allEqualTo(buffer, (byte) 0)
					&& (selfSending || settings.getDe1ActivePin(GPIO).isHigh())) {
				System.out.println("success 2");
				System.out.print("Got data late (" + readIndex + " to "
						+ (readIndex + maxBuff) + "): [");
				for (byte b : buffer)
					System.out.print(Integer.toBinaryString(b & 0xFF) + ", ");
				System.out.println("]");
				System.arraycopy(buffer, 0, receivedBytes, readIndex, maxBuff);
				readIndex += maxBuff;
				timer.restart();
			} else if (settings.getDe1ActivePin(GPIO).isLow()) {
				System.out.println("low 1");
				Logger.logError("Did not get whole message from DE1.");
				fail = true;
				break;
			} else if (timer.hasExpired()) {
				System.out.println("Expired 2");
				Logger.logError("Timed out: " + timer.toString()
						+ Arrays.toString(buffer));
				fail = true;
				break;
			}
		}
		releaseLock();
		if (fail) {
			System.out.println("Fail");
			return null;
		} else {
			Message rec = new Message(receivedBytes, msg.getActualSize(),
					encrypt, msg.getPacketSize());
			System.out.println("Received MSG: "
					+ Arrays.toString(rec.getData()));
			System.out.println("-----------------------");
			return rec;
		}
	}

	/**
	 * Waits for the value of the specified pin to turn high or low.
	 *
	 * @param pin
	 *            to read
	 * @param waitforHigh
	 *            whether to wait for the pin to turn high or low
	 * @param timeout
	 *            the max time to wait for the event
	 * @return true if the event has happened, false if the time-out has expired
	 */
	private boolean waitForPin(GpioPinDigitalInput pin, boolean waitforHigh,
			long timeout) {
		Timer timer = new Timer(timeout, true);
		while (waitforHigh ? !pin.isHigh() : !pin.isLow()) {
			Tools.waitForMs(10);
			if (timer.hasExpired())
				return false;
		}
		return true;
	}

	/**
	 * Tries to get the lock.<br>
	 * It returns only when the lock has been acquired or the time-out expires.
	 *
	 * @param timeout
	 *            the time-out to use
	 * @return whether the lock has been aqcuired
	 */
	private boolean getLock(long timeout) {
		boolean hasLock = false;
		Timer timer = new Timer(timeout, true);
		while (!hasLock && !timer.hasExpired()) {
			Tools.waitForMs(10);
			hasLock = lock.tryLock();
		}
		return hasLock;
	}

	/**
	 * Releases the lock if this thread is holding it.
	 */
	private void releaseLock() {
		if (lock.isHeldByCurrentThread())
			lock.unlock();
	}

	/**
	 * Safely shuts down all GPIO services.
	 */
	public void shutdown() {
		settings.reset();
		GPIO.shutdown();
	}

	// for testing only
	public static void main(String[] args) {
		int sendSpeed = args.length > 0 ? Integer.parseInt(args[0]) : 32000000;
		int packetSize = args.length > 1 ? Integer.parseInt(args[1]) : 16;
		int fileSize = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
		int bufferSize = args.length > 3 ? Integer.parseInt(args[3]) : 1000;
		int timeOut = args.length > 4 ? Integer.parseInt(args[4]) : 2000;
		boolean selfTest = args.length > 5 ? Boolean.parseBoolean(args[5])
				: false;
		SocManagerTest.testSocManager(sendSpeed, packetSize, fileSize,
				bufferSize, timeOut, selfTest);
	}

	public static void init() {
		GPIOSettings settings = new GPIOSettings();
		instance = new SocManager(settings, DEFAULT_CONNECTION_SPEED,
				DEFAULT_BUFFER_SIZE);
	}

}
