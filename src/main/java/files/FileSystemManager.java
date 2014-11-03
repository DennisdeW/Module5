package files;

import files.FolderMonitor.Event;
import global.Logger;
import global.PiCloudConstants;

import java.io.File;
import java.nio.file.StandardWatchEventKinds;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import soc.model.FileMessage;

public class FileSystemManager implements Observer, Runnable {

	public static final FileSystemManager INSTANCE;
	private static final Thread THREAD;

	private boolean stop;

	static {
		Logger.log("Starting FileSystemManager...");
		INSTANCE = new FileSystemManager();
		THREAD = new Thread(INSTANCE);
		THREAD.setName("FileSystemManager");
		FolderMonitor.MONITORS.forEach(m -> m.addObserver(INSTANCE));
		THREAD.start();
	}

	private BlockingQueue<FolderMonitor> updatedMonitors;
	private volatile Set<FileMessage> encryptedMessages, decryptedMessages;

	public static void register(String user) {
		FolderMonitor.registerNewDirectory(user);
		FolderMonitor.MONITORS.forEach(m -> m.addObserver(INSTANCE));
	}

	public static void stop() {
		INSTANCE.stop = true;
		// Because the thread is probably blocked by the queue
		THREAD.interrupt();
		FolderMonitor.stopMonitors();
		try {
			THREAD.join();
		} catch (InterruptedException e) {
			Logger.logError(e);
		}
		
		//Remove leftover temporary files
		TemporaryFile.cleanTemporaryFiles();
	}

	@Override
	public void update(Observable monitor, Object ignored) {
		updatedMonitors.offer((FolderMonitor) monitor);
	}

	/**
	 * Tells the FileSystemManager that it should expect to see the file
	 * contained in the given message to appear. If it is not expected, the
	 * FileSystemManager will send it on for encryption.
	 * 
	 * @param msg
	 *            A FileMessage containing the file to expect.
	 */
	public static synchronized void expectDecrypted(FileMessage msg) {
		INSTANCE.decryptedMessages.add(msg);
	}

	/**
	 * Tells the FileSystemManager that it should expect to see the file
	 * contained in the given message to appear. If it is not expected, the
	 * FileSystemManager will send it on for encryption.
	 * 
	 * @param msg
	 *            A FileMessage containing the file to expect.
	 */
	public static synchronized void expectEncrypted(FileMessage msg) {
		INSTANCE.encryptedMessages.add(msg);
	}

	/**
	 * Checks whether the FileSystemManager was told to expect a file.<br>
	 * This method will not return true multiple times without the
	 * expectEncrypted or expectDecrypted methods being called with a
	 * FileMessage containing this file.
	 * 
	 * @param file
	 *            The file to check.
	 * @return true if this file was expected, false otherwise.
	 */
	private boolean wasExpected(File file) {
		Optional<FileMessage> msg = Stream
				.concat(encryptedMessages.stream(), decryptedMessages.stream())
				.filter(fm -> fm.getFile().equals(file)).findAny();
		if (msg.isPresent()) {
			encryptedMessages.remove(msg.get());
			decryptedMessages.remove(msg.get());
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		while (!stop) {
			// get the oldest updated monitor
			try {
				FolderMonitor monitor = updatedMonitors.take();
				Event event = monitor.poll();
				File file = event.getFile();
				if (file == null)
					continue;// Should only be possible for OVERFLOW events,
								// which are not enqueued.
				if (event.getType() == StandardWatchEventKinds.ENTRY_CREATE) {
					Logger.log(file.getAbsolutePath() + " created.");
					if (wasExpected(file)) {
						// Do nothing: we don't want cycles.
					} else if (file.length() > PiCloudConstants.MAX_FILE_SIZE) {
						Logger.logError("File " + file.getName()
								+ " rejected! It's too large!");
						file.delete();
					} else {
						// TODO: get this file over to the DE1
						Logger.log("Sending " + file.getName()
								+ " for encryption...");
					}
				} else if (event.getType() == StandardWatchEventKinds.ENTRY_DELETE) {
					Logger.log(file.getAbsolutePath() + " deleted.");
					// TODO: Something?
				} else if (event.getType() == StandardWatchEventKinds.ENTRY_MODIFY) {
					Logger.log(file.getAbsolutePath() + " modified.");
				}
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Singleton
	 */
	private FileSystemManager() {
		updatedMonitors = new LinkedBlockingQueue<>();
		encryptedMessages = new HashSet<>();
		decryptedMessages = new HashSet<>();
		stop = false;
	}

	/**
	 * Ensure the initialiser is run.
	 */
	public static void init() {

	}
}
