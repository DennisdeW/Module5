package files;

import global.Logger;

import java.io.File;

public class TemporaryFile extends File {

	private static final long serialVersionUID = 4807843399170809945L;
	private final long killTime;

	public TemporaryFile(String pathname, int lifetime) {
		super(pathname);
		this.killTime = System.currentTimeMillis() + (lifetime * 1000);
		new Thread(new FileDeleter()).start();
	}

	public boolean remove() {
		return this.delete();
	}

	private final class FileDeleter implements Runnable {
		@Override
		public void run() {
			boolean done = false;
			while (!done) {
				if (System.currentTimeMillis() >= killTime) {
					remove();
					done = true;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.logError(e);
					}
				}
			}
		}
	}
}