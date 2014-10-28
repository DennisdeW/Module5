package soc.model;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Settings to use when communicating via the GPIO interface.
 * 
 * @author rvemous
 */
public class GPIOSettings {
	
	private Pin commandPin;
	private Pin piActivePin;
	private Pin de1ActivePin;
	private long piActiveWaitTime; //ns;
	
	/**
	 * Creates new GPIO settings.
	 * 
	 * @param commandPin the pin used tell the DE1 to encrypt or decrypt the data
	 * @param piActivePin the pin used to tell the DE1 the Pi is sending
	 * @param de1ActivePin the pin used to tell the Pi the DE1 is sending
	 * @param piActiveWaitTime time the Pi waits after setting the piActivePin
	 */
	public GPIOSettings(Pin commandPin, Pin piActivePin, Pin de1ActivePin, long piActiveWaitTime) {
		this.commandPin = commandPin;
		this.piActivePin = piActivePin;
		this.de1ActivePin = de1ActivePin;
		this.piActiveWaitTime = piActiveWaitTime;
	}
	
	/**
	 * Creates new GPIO setting which is a copy of the provided settings.
	 * 
	 * @param settings to copy
	 */
	public GPIOSettings(GPIOSettings settings) {
		this( 
			settings.commandPin,
			settings.piActivePin,
			settings.de1ActivePin,
			settings.piActiveWaitTime
			);
	}
	
	/**
	 * Loads default GPIO settings.
	 */
	public GPIOSettings() {	
		commandPin = RaspiPin.GPIO_00;
		piActivePin = RaspiPin.GPIO_01;
		de1ActivePin = RaspiPin.GPIO_02;
		piActiveWaitTime = 100; //0.1 ms
	}
	
	/**
	 * Gets the pin used tell the DE1 whether to encrypt or decrypt the 
	 * data.
	 * 
	 * @return the pin
	 */
	public Pin getCommandPin() {
		return commandPin;
	}
	
	/**
	 * Sets the pin used tell the DE1 whether to encrypt or decrypt the 
	 * data.
	 * 
	 * @param commandPin the pin to use
	 */	
	public void setCommandPin(Pin commandPin) {
		this.commandPin = commandPin;
	}

	/**
	 * Gets the pin used tell the DE1 whether the Pi is sending.
	 * 
	 * @return the pin
	 */
	public Pin getPiActivePin() {
		return piActivePin;
	}
	
	/**
	 * Sets the pin used tell the DE1 whether the Pi is sending. 
	 * 
	 * @param piActivePin the pin to use
	 */		
	public void setPiActivePin(Pin piActivePin) {
		this.piActivePin = piActivePin;
	}

	/**
	 * Gets the pin used tell the Pi whether the DE1 is sending.
	 * 
	 * @return the pin
	 */
	public Pin getDe1ActivePin() {
		return de1ActivePin;
	}
	
	/**
	 * Sets the pin used tell the Pi whether the DE1 is sending. 
	 * 
	 * @param de1ActivePin the pin to use
	 */	
	public void setDe1ActivePin(Pin de1ActivePin) {
		this.de1ActivePin = de1ActivePin;
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
	
}
