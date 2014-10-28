package db;

import global.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;

/**
 * 
 * @author Dennis (Partly copied from Data & Informatie project - di07)
 *
 */
public class DatabaseManager {

	private static final Connection DB_CONN;

	/**
	 * Always update activeResults when returing a ResultSet. Volatile because
	 * WeakReferences may become invalid.
	 */
	private static volatile Map<ResultSet, WeakReference<PreparedStatement>> activeResults;

	private static Set<PreparedStatement> activeStatements;

	/**
	 * Initialize connection and variables. Add shutdown hook for cleanup.
	 */
	static {
		if (Platform.isLinux()) {
			System.load("libsqlitejdbc.so");
		}
		decryptDB();
		Connection t = null;
		try {
			Class.forName("org.sqlite.JDBC");
			t = DriverManager.getConnection("jdbc:sqlite:db.sqlite");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		DB_CONN = t;
		activeResults = new HashMap<>();
		activeStatements = new HashSet<>();
		Runtime.getRuntime().addShutdownHook(new CleanupThread());
	}

	/**
	 * Ensures a ResultSet is closed properly
	 * 
	 * @param result
	 *            The ResultSet to close
	 * @throws SQLException
	 *             If the ResultSet or its PreparedStatement cannot be closed
	 */
	public static void clean(ResultSet result) throws SQLException {
		if (!activeResults.containsKey(result))
			return;
		WeakReference<PreparedStatement> ref = activeResults.get(result);
		boolean removeStatement = true;
		for (Entry<ResultSet, WeakReference<PreparedStatement>> ent : activeResults
				.entrySet()) {
			if (!ent.getKey().equals(result) && ent.getValue().equals(ref))
				removeStatement = false;
		}

		result.close();
		if (removeStatement) {
			ref.get().close();
			activeStatements.remove(ref.get());
		}
		activeResults.remove(result);
		System.gc();
	}

	private static void decryptDB() {
		String path = new File("").getAbsolutePath() + File.separatorChar;
		try {
			Process proc = Runtime.getRuntime().exec(
					"openssl aes-256-cbc -d -pass file:key.bin -in " + path
							+ "db-e.sqlite -out " + path + "db.sqlite");
			File db = new File("db.sqlite");
			System.out.println(db.getAbsolutePath());
			db.deleteOnExit();
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void encryptDB() {
		String path = new File("").getAbsolutePath() + File.separatorChar;
		try {
			Process proc = Runtime.getRuntime().exec("openssl aes-256-cbc -e -pass file:key.bin -out " + path
								+ "db-e.sqlite -in " + path + "db.sqlite");
			proc.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the provided PreparedStatement.<br>
	 * Usage of this method is very risky, since any active ResultSets will
	 * become invalid.
	 * 
	 * @param statement
	 * @throws SQLException
	 *             If the <code>PreparedStatement</code> is already closed.
	 * @deprecated Should not be used if it can be avoided
	 */
	@Deprecated
	public static void forceClean(PreparedStatement statement)
			throws SQLException {
		statement.close();
		activeResults.remove(statement);
	}

	public static void main(String[] args) throws SQLException, IOException {
		Logger.init();
		byte[] buf = new byte[65536];
		PasswordGetter.INSTANCE.getKey(buf);
		File file = new File("key.bin");
		FileInputStream fos = new FileInputStream(file);
		byte[] key = new byte[65536];
		fos.read(key);
		fos.close();
		Logger.log("Keys match: " + Arrays.equals(buf, key));
		
		Statement s = DB_CONN.createStatement();
		ResultSet r = s.executeQuery("SELECT * FROM User;");
		r.next();
		String name = r.getString("Name");
		System.out.println(name);
		r.close();
		s.close();

		try {
			System.out.println(UserStatementMaker.getId(name));
		} catch (UnknownUserException e1) {
			e1.printStackTrace();
		}
		System.out.println(activeResults.size() + ", "
				+ activeStatements.size());
		System.out.println();
		System.out.println(Tuple.fromResultSet(UserStatementMaker
				.getUserData(0))[0]);
		Logger.log("Done Testing");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		Logger.log("Testing error...");
		Logger.logError(new RuntimeException());
		Logger.log("Terminating...");
	}

	protected static void registerResult(ResultSet r, PreparedStatement s) {
		activeResults.put(r, new WeakReference<PreparedStatement>(s));
		activeStatements.add(s);
	}

	protected static PreparedStatement prepare(String sql) throws SQLException {
		return DB_CONN.prepareStatement(sql);
	}

	/**
	 * Closes Remaining Connections
	 */
	private static final class CleanupThread extends Thread {
		public void run() {
			Logger.log("Running Cleanup...");
			encryptDB();
			try {
				for (ResultSet rs : activeResults.keySet())
					clean(rs);
				for (PreparedStatement ps : activeStatements)
					forceClean(ps);
				DB_CONN.close();
			} catch (Throwable t) {
			}
			Logger.log("Cleanup Done.");
		}
	}

	private static interface PasswordGetter extends Library {
		PasswordGetter INSTANCE = (PasswordGetter) Native.loadLibrary(Platform
				.isWindows() ? "PiCloudKeyStore.dll" : "PiCloudKeyStore.so",
				PasswordGetter.class, new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;
					{
						put(Library.OPTION_FUNCTION_MAPPER, new Mapper());
					}
				});

		/**
		 * Gets a random key.
		 * @param buf
		 */
		public void generateKey(byte[] buf);

		/**
		 * Gets the correct 64kiB key, to test against a supplied key file.
		 * @param buf
		 */
		public void getKey(byte[] buf);
	}

	private static final class Mapper implements FunctionMapper {

		@Override
		public String getFunctionName(NativeLibrary library, Method method) {
			if (method.getName().equals("generateKey"))
				return "?generateKey@PiCloud@1@SAXPEAD@Z";
			else if (method.getName().equals("getKey"))
				return "?getKey@PiCloud@1@SAXPEAD@Z";
			return method.getName();
		}

	}
}
