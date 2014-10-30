package files;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import global.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ssh.sftp.PiFileSystemFactory;
import db.UserStatementMaker;

/**
 * Observable class which stores any changes to a certain directory in a queue
 * for processing.
 * 
 * @author Dennis
 *
 */
public class FolderMonitor extends Observable implements Runnable {

	public static final Set<FolderMonitor> MONITORS;
	private static final Set<Thread> THREADS;

	static {
		MONITORS = new HashSet<>();
		THREADS = new HashSet<>();
		initialiseMonitors();
	}

	/**
	 * This should be called when the program starts to put monitors on all
	 * existing users' directories.
	 */
	public static void initialiseMonitors() {
		Logger.log("Starting Monitors...");
		PiFileSystemFactory.init();
		try {
			List<String> names = UserStatementMaker.getUserNameList();
			names.forEach(n -> {
				FolderMonitor mon = new FolderMonitor(n);
				MONITORS.add(mon);
				Thread t = new Thread(mon);
				THREADS.add(t);
				t.start();
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This should be called when a new user is added.
	 */
	public static void registerNewDirectory(String user) {
		FolderMonitor mon = new FolderMonitor(user);
		Thread t = new Thread(mon);
		MONITORS.add(mon);
		THREADS.add(t);
		t.start();
	}

	/**
	 * Stops all monitor threads.
	 */
	public static void stopMonitors() {
		MONITORS.forEach(m -> m.stop());
		MONITORS.clear();
		THREADS.forEach(t -> {
			try {
				t.join();
			} catch (Exception e) {
			}
		});
	}

	private WatchService service;
	private Queue<Event> events;
	private Path basePath;
	private boolean stop = false;

	/**
	 * You should create these using the static methods above.
	 */
	private FolderMonitor(String user) {
		events = new ArrayDeque<>();
		try {
			basePath = new File(PiFileSystemFactory.homeDirs.get(user))
					.toPath();
			service = FileSystems.getDefault().newWatchService();
			basePath.register(service, ENTRY_CREATE, ENTRY_MODIFY,
					ENTRY_DELETE, OVERFLOW);
			Logger.log("Monitor created for " + user + " on " + basePath);
		} catch (IOException e) {
			Logger.logError("Unable to create WatchService for " + user + ":");
			Logger.logError(e);
		}
	}

	/**
	 * Enqueue a new event.
	 * 
	 * @param e
	 *            The new event.
	 */
	private synchronized void offer(Event e) {
		events.offer(e);
	}

	/**
	 * Get the oldest event in the queue.<br>
	 * This should only be called by observers after they are notified.
	 * 
	 * @return The oldest event, or null if there aren't any.
	 */
	public synchronized Event poll() {
		return events.poll();
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				// waits 1 second unless a key is available.
				WatchKey key = service.poll(1, TimeUnit.SECONDS);
				List<WatchEvent<?>> events = key.pollEvents();
				for (WatchEvent<?> ev : events) {
					WatchEvent.Kind<?> type = ev.kind();

					// Overflow means that an event was missed.
					if (type == OVERFLOW) {
						Logger.logError("An overflow occured in one of the FolderMonitors!");
						continue;
					}
					File file = ((Path) ev.context()).toAbsolutePath().toFile();
					offer(new Event(type, file));
					setChanged();
					notifyObservers();
				}
			} catch (InterruptedException | ClosedWatchServiceException e) {
				Logger.logError(e);
				continue;
			}
		}
	}

	private void stop() {
		stop = true;
	}

	/**
	 * Small bean for an event.
	 * 
	 * @author Dennis
	 */
	protected static final class Event {
		private WatchEvent.Kind<?> type;
		private File file;

		private Event(WatchEvent.Kind<?> type, File file) {
			this.type = type;
			this.file = file;
		}

		public WatchEvent.Kind<?> getType() {
			return type;
		}

		public File getFile() {
			return file;
		}
	}
}
