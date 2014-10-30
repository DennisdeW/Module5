package soc.controller;

import global.Logger;
import global.Timer;
import global.Tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import soc.model.GPIOSettings;
import soc.model.Message;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;

/**
 * Represents the connection between the Pi and the DE1 SoC.<br>
 * It can be used to encrypt or decrypt data and in the future also for
 * compressing and decompressing the data.
 * 
 * @author rvemous
 */
public class SocManager {
    
	private static ReentrantLock lock;
	private static GpioController GPIO;
	
	private GPIOSettings settings;
	
	/**
	 * Creates a new gpio connection.
	 *  
	 * @param settings the settings to use
	 * @param connectionSpeed the speed in bits/sec of the connection,
	 * this can be between 3.000 and 32.000.000 bits a second
	 * @param bufferSize the size in KB of the SPI receive buffer
	 */
	public SocManager(GPIOSettings settings, int connectionSpeed, int bufferSize) {
		if (lock == null) {
			lock = new ReentrantLock();
		}
		if (GPIO == null) {
			Timer timer = new Timer(1000);
			while (Spi.wiringPiSPISetup(0, connectionSpeed) == -1) {
				try {
					Runtime.getRuntime().exec("gpio load spi " + bufferSize);
				} catch (IOException e) {
					Logger.logError("Could not load SPI with specified "
							+ "buffer: " + bufferSize);
					break;
				}
				Tools.waitForMs(100);
				if (timer.hasExpired()) {
					Logger.logError("Could not set up connection: "
							+ "timed out");
				}
			}
			GPIO = GpioFactory.getInstance();
		}
		this.settings = settings;
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
	 * @param settings to use
	 */
	public synchronized void setSettings(GPIOSettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Sends the data to the DE1 SoC for encryption/decryption.
	 * 
	 * @param data to send
	 * @param encrypt whether the DE1 should encrypt or decrypt the data
	 * @param timeout the max time in ms to wait for the DE1 to answer
	 * @return the response message from the DE1
	 */
	public synchronized Message sendAndReceiveData(Message msg, boolean encrypt, long timeout) {
		return sendAndReceiveData(msg, encrypt, timeout, false);
	}

	/**
	 * Sends the data to the DE1 SoC for encryption/decryption or sends to
	 * self (MOSI connected to MISO).<br>
	 * If it sends to self the return message will be equal to
	 * <code>msg</code>.
	 * 
	 * @param data to send
	 * @param encrypt whether the DE1 should encrypt or decrypt the data
	 * @param timeout the max time in ms to wait for the DE1 to answer
	 * @param selfSending whether the communication is done with the Pi 
	 * itself or the DE1
	 * @return the response message from the DE1
	 */
	public synchronized Message sendAndReceiveData(Message msg, boolean encrypt, long timeout, boolean selfSending) {
		if (!getLock(timeout)) {
			return null;
		}
		boolean fail = false;
		int maxBuff = Message.PACKET_SIZE;
		// wait till DE1 is not sending
		waitForPin(settings.getDe1ActivePin(GPIO), false, timeout);
		// set encryption/decryption command pin		
		settings.getCommandPin(GPIO).setState(!encrypt);
		// tell the DE1 the Pi is going to send
		settings.getPiActivePin(GPIO).high();
		Runtime rt = Runtime.getRuntime();
		// send data to DE1 for encryption/decryption and receive data		
		int readIndex = 0;
		byte[] buffer = new byte[maxBuff];
		byte[] allZeros = new byte[maxBuff];
		byte[] receivedBytes = new byte[msg.getData().length];
		Arrays.fill(allZeros, (byte)0);
		Timer timer = new Timer(timeout, true);
		for (int i = 0; i < receivedBytes.length; i += maxBuff) {
			System.arraycopy(msg.getData(), i, buffer, 0, maxBuff);
			// try to send data
			boolean sendSuccesfull = true;
			if (Spi.wiringPiSPIDataRW(0, buffer, maxBuff) == -1) {
				Logger.logError("Could not send data to DE1: " + Arrays.toString(buffer));
				i -= maxBuff;
				sendSuccesfull = false;
			}
			if (sendSuccesfull) {
				timer.restart();
				// check for response from DE1
				if (!Tools.allEqualTo(buffer, (byte)0) && (selfSending || settings.getDe1ActivePin(GPIO).isHigh())) {
					System.arraycopy(buffer, 0, receivedBytes, i, maxBuff);
					readIndex += maxBuff;
				}
			}
			// check for time-out
			if (timer.hasExpired()) {
				Logger.logError("Timed out" + Arrays.toString(buffer));	
				// tell the DE1 the Pi is done sending
				settings.getPiActivePin(GPIO).low();
		        releaseLock();
		        return null;
			}
		}	
		// receive pending packages from DE1
		timer.restart();
		for (; readIndex < msg.getData().length;) {	
			System.arraycopy(allZeros, 0, buffer, 0, maxBuff);
			Spi.wiringPiSPIDataRW(0, buffer, maxBuff);
			// check for response from DE1
			if (!Tools.allEqualTo(buffer, (byte)0) && (selfSending || settings.getDe1ActivePin(GPIO).isHigh())) {
				System.arraycopy(buffer, 0, receivedBytes, readIndex, maxBuff);
				readIndex += maxBuff;
			} else if (settings.getDe1ActivePin(GPIO).isLow()) {
				Logger.logError("Did not get whole message from DE1.");
		        fail = true;
		        break;
			} else if (timer.hasExpired()) {
				Logger.logError("Timed out" + Arrays.toString(buffer));	
		        fail = true;
		        break;
			}
		}	
		// tell the DE1 the Pi is done sending
		System.out.println("Set pi active pin to 0");
		settings.getPiActivePin(GPIO).low();
        System.out.println("Done.");
        releaseLock();
        if (fail) {
        	return null;
        } else {
    		System.out.println("Message " + (encrypt ? "encryption " : "decryption ") + "done");
        	return new Message(receivedBytes, msg.getActualSize(), encrypt);
        }
	}	
	
	/**
	 * Waits for the value of the specified pin to turn high or low.
	 * 
	 * @param pin to read
	 * @param waitforHigh whether to wait for the pin to turn high or low
	 * @param timeout the max time to wait for the event
	 * @return true if the event has happened, false if the time-out has
	 * expired
	 */
	private boolean waitForPin(GpioPinDigitalInput pin, boolean waitforHigh, long timeout) {
        Timer timer = new Timer(timeout, true);
		while (waitforHigh ? !pin.isHigh() : !pin.isLow()) {
			Tools.waitForMs(10);
			if (timer.hasExpired()) {
				return false;
			}
		}	
		return false;
	}
	
	/**
	 * Tries to get the lock.<br>
	 * It returns only when the lock has been acquired or the
	 * time-out expires.
	 * 
	 * @param timeout the time-out to use
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
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
		}
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
		SocManager gpio = new SocManager(new GPIOSettings(), Integer.parseInt(args[0]), 1000);
		//byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9};
		File f = new File("bigfile.txt");	
		System.out.println("Loading input file: " + f.getName());
		if (!f.exists()) {
			System.err.println("No input file available");
		}
		String newLine = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(f)) {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine());
				sb.append(newLine);
			}
		} catch (IOException e) {
			System.err.println("IOException on reading input file");			
		}
		Message msg = new Message(sb.toString().getBytes(), false);
		System.out.println("Done.");
		Timer timer = new Timer(100000, true);
		Message response = gpio.sendAndReceiveData(msg, true, 250);
		System.out.println("---- Took: " + (100000 - timer.timeLeft()) + " ms");
		try (FileOutputStream fos = new FileOutputStream(new File("bigfile_copy.txt"))) {
			fos.write(response.getData(), 0, response.getData().length);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.err.println("IOException on writing output file");			
		}				
		gpio.shutdown();
	}

}
