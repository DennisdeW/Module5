package net.packets;

import global.Logger;

import java.io.IOException;

import ssh.command.CompoundCommand;
import ssh.command.PiCommand.PiCommandType;

public class MultiCommandPacket extends CommandPacket {

	private final CompoundCommand command;
	private final String commandString;

	MultiCommandPacket(byte[] data) {
		super(data.length - 6);
		byte[] rawCommand = new byte[data.length - 6];
		System.arraycopy(data, 0, rawCommand, 0, data.length - 6);
		commandString = new String(rawCommand);
		CompoundCommand cc = null;
		try {
			cc = (CompoundCommand) PiCommandType.getCommand(commandString);
		} catch (ClassCastException e) {
			Logger.logError("Command " + commandString
					+ " was accepted as compound, but isn't");
		}
		command = cc;
	}

	@Override
	public AnswerPacket run() {
		try {
			command.start(null);
			String message = command.getResult();
			return new AnswerPacket(message);
		} catch (IOException e) {
			Logger.logError(e);
		}
		return null;
	}

	@Override
	public PiPacketType getType() {
		return PiPacketType.COMPOUND_COMMAND;
	}

	@Override
	public byte[] getData() {
		return commandString.getBytes();
	}

	@Override
	public String toString() {
		return "[MC|" + commandString + "]";
	}
}
