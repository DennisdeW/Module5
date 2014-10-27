package db;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	public static void main(String[] args) throws SQLException {
		Statement s = DB_CONN.createStatement();
		ResultSet r = s.executeQuery("SELECT * FROM User;");
		r.next();
		String name = r.getString("Name");
		System.out.println(name);
		r.close();
		s.close();
		
		System.out.println(UserStatementMaker.getId(name));
		System.out.println(activeResults.size() + ", " + activeStatements.size());
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
			try {
				for (ResultSet rs : activeResults.keySet())
					clean(rs);
				for (PreparedStatement ps : activeStatements)
					forceClean(ps);
				DB_CONN.close();
			} catch (Throwable t) {
			}
		}
	}
}
