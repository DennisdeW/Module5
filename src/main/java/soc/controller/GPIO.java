package soc.controller;

import global.Logger;
import global.Tools;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import soc.model.GPIOSettings;
import soc.model.Message;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.wiringpi.Spi;

public class GPIO {
    
	private static ReentrantLock lock;
	private static GpioController GPIO;
	
	private GPIOSettings settings;
	
	public GPIO(GPIOSettings settings) {
		if (lock == null) {
			lock = new ReentrantLock();
		}
		if (GPIO == null) {
			Spi.wiringPiSPISetup(0, 32000000);
			GPIO = GpioFactory.getInstance();
		}
		this.settings = settings;
	}
	
	public GPIOSettings getSettings() {
		return settings;
	}
	
	public synchronized void setSettings(GPIOSettings settings) {
		this.settings = settings;
	}	

	/**
	 * Sends the data to the DE1 SoC for encryption/decryption.
	 * 
	 * @param data to send
	 * @param encrypt whether the DE1 should encrypt or decrypt the data
	 * @param timeout the max time in ms to wait for the DE1 to answer
	 * @return false when the DE1 has not responded within 
	 * the time out period
	 */
	public synchronized Message sendDataAndReceive(Message msg, boolean encrypt, long timeout) {
		if (!getLock(timeout)) {
			return null;
		}
		int maxBuff = Message.PACKET_SIZE;
		GpioPinDigitalInput DE1Active = 
				getInPin(settings.getDe1ActivePin());
		// wait till DE1 is not sending
		waitForPin(settings.getDe1ActivePin(), false, timeout);
		// set encryption/decryption command pin
		getOutPin(settings.getCommandPin(), 
				encrypt ? PinState.LOW : PinState.HIGH);		
		// tell the DE1 the Pi is going to send
		getOutPin(settings.getPiActivePin(), PinState.HIGH);	
		// wait for specified time
		Tools.waitForNs(settings.getPiActiveWaitTime());
		// send data to DE1 for encryption/decryption and receive data
		int readIndex = 0;
		byte[] buffer = new byte[maxBuff];
		byte[] allZeros = new byte[maxBuff];
		byte[] receivedBytes = new byte[msg.getData().length];
		Arrays.fill(allZeros, (byte)0);
		Timer timer = new Timer(timeout, true);
		for (int i = 0; i < msg.getData().length + maxBuff; i += maxBuff) {
			System.arraycopy(msg.getData(), i, buffer, 0, maxBuff);
			// try to send data
			if (Spi.wiringPiSPIDataRW(0, buffer, maxBuff) == -1) {
				Logger.logError("Could not send data to DE1: " + Arrays.toString(buffer));
				i -= maxBuff;
			}
			// check for response from DE1
			if (!Tools.allEqualTo(buffer, (byte)0) && DE1Active.isHigh()) {
				System.arraycopy(buffer, 0, receivedBytes, i, maxBuff);
				readIndex += maxBuff;
			}
			// check for time-out
			if (timer.hasExpired() && i < msg.getData().length) {
				Logger.logError("Timed out" + Arrays.toString(buffer));				
				return null;
			}
		}	
		// receive pending packages from DE1
		timer.restart();
		for (; readIndex < msg.getData().length;) {	
			System.arraycopy(allZeros, 0, buffer, 0, maxBuff);
			Spi.wiringPiSPIDataRW(0, buffer, maxBuff);
			// check for response from DE1
			if (!Tools.allEqualTo(buffer, (byte)0) && DE1Active.isHigh()) {
				System.arraycopy(buffer, 0, receivedBytes, readIndex, maxBuff);
				readIndex += maxBuff;
			} else if (DE1Active.isLow()) {
				Logger.logError("Did not get whole message from DE1: "
						+ "\nGood: " + msg + "\nActual: " + receivedBytes);
				// tell the DE1 the Pi is done sending
		        getOutPin(settings.getPiActivePin(), PinState.HIGH);
		        return null;
			} else if (timer.hasExpired()) {
				Logger.logError("Timed out" + Arrays.toString(buffer));	
				// tell the DE1 the Pi is done sending
		        getOutPin(settings.getPiActivePin(), PinState.HIGH);
		        return null;
			}
		}
		// tell the DE1 the Pi is done sending
        getOutPin(settings.getPiActivePin(), PinState.HIGH);
		return new Message(receivedBytes, msg.getActualSize(), encrypt);
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
	public boolean waitForPin(Pin pin, boolean waitforHigh, long timeout) {
        final GpioPinDigitalInput aPin = getInPin(pin, PinPullResistance.OFF);
		Timer timer = new Timer(timeout, true);
		while (waitforHigh ? !aPin.isHigh() : !aPin.isLow()) {
			Tools.waitForMs(10);
			if (timer.hasExpired()) {
				return false;
			}
		}	
		return false;
	}
	
	/**
	 * Gets an input pin.
	 * 
	 * @param pin to get
	 * @param defaultState the default state of the pin
	 * @return the pin
	 */
	private synchronized GpioPinDigitalInput getInPin(Pin pin, PinPullResistance defaultState) {
		return GPIO.provisionDigitalInputPin(pin, defaultState);
	}
	/**
	 * Gets an input pin.
	 * 
	 * @param pin to get
	 * @return the pin
	 */	
	private synchronized GpioPinDigitalInput getInPin(Pin pin) {
		return GPIO.provisionDigitalInputPin(pin);
	}
	
	/**
	 * Gets an output pin.
	 * 
	 * @param pin to get
	 * @param defaultState the default state of the pin
	 * @return the pin
	 */
	private synchronized GpioPinDigitalOutput getOutPin(Pin pin, PinState defaultState) {
		return GPIO.provisionDigitalOutputPin(pin, defaultState);
	}
	
	/**
	 * Gets an output pin.
	 * 
	 * @param pin to get
	 * @return the pin
	 */	
	private synchronized GpioPinDigitalOutput getOutPin(Pin pin) {
		return GPIO.provisionDigitalOutputPin(pin);
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
		Timer timer = new Timer(timeout);
		while (!hasLock && !timer.hasExpired()) {
			try {
				lock.wait(timeout);
				hasLock = lock.tryLock();
			} catch (InterruptedException e) {}
		}
		return hasLock;
	}
	
	/**
	 * Safely shuts down all GPIO services.
	 */
	public void shutdown() {
		GPIO.shutdown();
	}
	
	// for testing only
	public static void main(String[] args) {
		GPIO gpio = new GPIO(new GPIOSettings());
		byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9};
		gpio.sendDataAndReceive(new Message(data, false), false, 1000);
		gpio.shutdown();
	}

}
