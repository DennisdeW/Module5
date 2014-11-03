package net.packets;

import global.Logger;

import java.io.IOException;

import ssh.DefaultExitCallback;
import ssh.command.PiCommand;
import ssh.command.PiCommand.PiCommandType;

public class SingleCommandPacket extends CommandPacket {

	private final PiCommand command;
	private final String commandString;

	SingleCommandPacket(byte[] data) {
		super(data.length - 6);
		int commandLength = data.length - 6;
		byte[] command = new byte[commandLength];
		System.arraycopy(data, 6, command, 0, data.length - 6);
		this.commandString = new String(command);
		this.command = PiCommandType.getCommand(commandString, null, null,
				null, new DefaultExitCallback());
	}

	@Override
	public PiPacketType getType() {
		return PiPacketType.SINGLE_COMMAND;
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

	public static PiPacket build(byte[] packet) {
		return new SingleCommandPacket(packet);
	}

	@Override
	public byte[] getData() {
		return commandString.getBytes();
	}

	@Override
	public String toString() {
		return "[SC|" + commandString + "]";
	}
}
