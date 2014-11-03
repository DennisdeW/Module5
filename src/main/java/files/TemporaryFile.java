package files;

import global.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Set;

public class TemporaryFile extends File {

	private static final long serialVersionUID = 4807843399170809945L;
	public static final Set<TemporaryFile> ACTIVE_TEMPS = new HashSet<>();
	private final long killTime;
	private final FileDeleter deleter;

	public TemporaryFile(String pathname, int lifetime) {
		super(pathname);
		this.killTime = System.currentTimeMillis() + (lifetime * 1000);
		deleter = new FileDeleter();
		deleter.start();
		ACTIVE_TEMPS.add(this);
	}

	private void exit() {
		deleter.exit();
	}

	public void remove() throws IOException {
		ACTIVE_TEMPS.remove(this);
		try {
		Files.delete(toPath().toAbsolutePath());
		} catch (NoSuchFileException e) {
			Logger.logError("Tried to delete absent temp file " + getAbsolutePath());
		}
		try {
			deleter.interrupt();
			deleter.join();
		} catch (InterruptedException e) {
		}
	}

	public static void cleanTemporaryFiles() {
		ACTIVE_TEMPS.forEach(t -> t.exit());
	}

	private final class FileDeleter extends Thread {

		private boolean done = false;

		private void exit() {
			Logger.log("Force-removing leftover temporary file "
					+ getAbsolutePath());
			try {
				remove();
			} catch (IOException e) {
				Logger.logError(e);
			}
			done = true;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("FileDeleter-" + getName());
			Logger.log("Running FileDeleter on " + getAbsolutePath());
			while (!done && isFile()) {
				if (System.currentTimeMillis() >= killTime) {
					Logger.log("Removing TemporaryFile " + getAbsolutePath());
					try {
						remove();
					} catch (IOException e) {
						Logger.logError(e);
						deleteOnExit();
					}
					done = true;
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
			}
		}
	}
}