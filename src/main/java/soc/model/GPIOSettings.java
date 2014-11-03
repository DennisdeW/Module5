package soc.model;

import java.lang.reflect.Field;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Settings to use when communicating via the GPIO interface.
 * 
 * @author rvemous
 */
public class GPIOSettings {

	private int commandPinNr;
	private int piActivePinNr;
	private int de1ActivePinNr;
	private long piActiveWaitTime; //ns;
	
	private GpioPinDigitalOutput commandPin;
	private GpioPinDigitalOutput piActivePin;
	private GpioPinDigitalInput de1ActivePin;
	
	/**
	 * Creates new GPIO settings.
	 * 
	 * @param commandPin the pin used tell the DE1 to encrypt or decrypt the data
	 * @param piActivePin the pin used to tell the DE1 the Pi is sending
	 * @param de1ActivePin the pin used to tell the Pi the DE1 is sending
	 * @param piActiveWaitTime time the Pi waits after setting the piActivePin
	 */
	public GPIOSettings(int commandPinNr, int piActivePinNr, int de1ActivePinNr, long piActiveWaitTime) {
		this.commandPinNr = commandPinNr;
		this.piActivePinNr = piActivePinNr;
		this.de1ActivePinNr = de1ActivePinNr;
		this.piActiveWaitTime = piActiveWaitTime;
	}
	
	/**
	 * Creates new GPIO setting which is a copy of the provided settings.
	 * 
	 * @param settings to copy
	 */
	public GPIOSettings(GPIOSettings settings) {
		this( 
			settings.commandPinNr,
			settings.piActivePinNr,
			settings.de1ActivePinNr,
			settings.piActiveWaitTime
			);
	}
	
	/**
	 * Loads default GPIO settings.
	 */
	public GPIOSettings() {	
		commandPinNr = 5;
		piActivePinNr = 1;
		de1ActivePinNr = 4;
		piActiveWaitTime = 100; //0.1 ms
	}
	
	/**
	 * Gets the pin number used for the command pin.
	 * 
	 * @return the pin number
	 */
	public int getCommandPinNr() {
		return commandPinNr;
	}
	
	/**
	 * Gets the pin used tell the DE1 whether to encrypt or decrypt the 
	 * data.<br>
	 * On the first usage the pin will be created.
	 * 
	 * @param gpio the gpio object used
	 * @return the pin
	 */
	public GpioPinDigitalOutput getCommandPin(GpioController gpio) {
		if (commandPin == null) {
			commandPin = getOutPin(gpio, commandPinNr);
		}
		return commandPin;
	}

	/**
	 * Gets the pin number used for the Pi active pin.
	 * 
	 * @return the pin number
	 */
	public int getPiActivePinNr() {
		return piActivePinNr;
	}

	/**
	 * Gets the pin used tell the DE1 whether the Pi is sending.<br>
	 * On the first usage the pin will be created.
	 * 
	 * @param gpio the gpio object used
	 * @return the pin
	 */
	public GpioPinDigitalOutput getPiActivePin(GpioController gpio) {
		if (piActivePin == null) {
			piActivePin = getOutPin(gpio, piActivePinNr);
		}
		return piActivePin;
	}

	/**
	 * Gets the pin number used for the DE1 active pin.
	 * 
	 * @return the pin number
	 */
	public int getDe1ActivePinNr() {
		return de1ActivePinNr;
	}

	/**
	 * Gets the pin used tell the Pi whether the DE1 is sending.<br>
	 * On the first usage the pin will be created.
	 * 
	 * @param gpio the gpio object used
	 * @return the pin
	 */
	public GpioPinDigitalInput getDe1ActivePin(GpioController gpio) {
		if (de1ActivePin == null) {
			try {
				de1ActivePin = gpio.provisionDigitalInputPin(
						(Pin)getPinField(de1ActivePinNr).get(null));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				return null;
			}
		}
		return de1ActivePin;
	}
	
	/**
	 * Gets the time in ns the Pi waits after setting the value of the 
	 * PiActivePin to high, until it sends the data.
	 * 
	 * @return the time in ns
	 */
	public long getPiActiveWaitTime() {
		return piActiveWaitTime;
	}
	
	/**
	 * Sets the time in ns the Pi waits after setting the value of the 
	 * PiActivePin to high, until it sends the data.
	 * 
	 * @return piActiveWaitTime the time in ns
	 */	
	public void setPiActiveWaitTime(long piActiveWaitTime) {
		this.piActiveWaitTime = piActiveWaitTime;
	}
	
	/**
	 * Resets the state of the settings and unloads all pins.
	 */
	public void reset() {
		commandPin.setShutdownOptions(true, PinState.LOW);
		piActivePin.setShutdownOptions(true, PinState.LOW);
		de1ActivePin.setShutdownOptions(true, PinState.LOW);
		de1ActivePin.setMode(PinMode.DIGITAL_OUTPUT);
		commandPin = null;
		piActivePin = null;
		de1ActivePin = null;
	}
	
	/**
	 * Gets the output pin belonging to the provided number.
	 * 
	 * @param pinNr the number of the pin
	 * @return the pin
	 */		
	private synchronized final GpioPinDigitalOutput getOutPin(GpioController gpio, int pinNr) {
		try {
			return gpio.provisionDigitalOutputPin((Pin)getPinField(pinNr).get(null));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	/**
	 * Gets the output pin belonging to the provided number.
	 * 
	 * @param pinNr the number of the pin
	 * @param setting the default pin settings (high or low)
	 * @return the pin
	 */	
	private synchronized final GpioPinDigitalOutput getOutPin(GpioController gpio, int pinNr, PinState setting) {
		try {
			return gpio.provisionDigitalOutputPin((Pin)getPinField(pinNr).get(null), setting);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	/**
	 * Gets the input pin belonging to the provided number.
	 * 
	 * @param pinNr the number of the pin
	 * @return the pin
	 */		
	private synchronized final GpioPinDigitalInput getInPin(GpioController gpio, int pinNr) {
		try {
			return gpio.provisionDigitalInputPin((Pin)getPinField(pinNr).get(null));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	/**
	 * Gets the input pin belonging to the provided number.
	 * 
	 * @param pinNr the number of the pin
	 * @param setting the default pin setting
	 * @return the pin
	 */
	private synchronized final GpioPinDigitalInput getInPin(GpioController gpio, int pinNr, PinPullResistance setting) {
		try {
			return gpio.provisionDigitalInputPin((Pin)getPinField(pinNr).get(null), setting);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	
	/**
	 * Gets the static field within the RaspiPin class which represents
	 * this pin. 
	 * 
	 * @param pinNr the number of the pin
	 * @return the field
	 */
	private synchronized Field getPinField(int pinNr) {
		try {
			return RaspiPin.class.getDeclaredField("GPIO_" + (pinNr < 10 ? "0" : "") + pinNr);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}
	
}
