package db;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.bouncycastle.crypto.digests.SHA3Digest;

public class UserStatementMaker {

	/**
	 * Gets the ID of a user with the given name.
	 * @param name
	 * @return
	 * @throws SQLException
	 * @throws UnknownUserException
	 */
	public static int getId(String name) throws SQLException,
			UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT id FROM User WHERE Name = ?;");
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (int) tuples[0].getItem(0);
		throw new UnknownUserException("For Name: " + name);
	}

	public static ResultSet getUserData(int id) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT * FROM User WHERE id = ?;");
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		return result;
	}

	public static byte[] getPass(int id) throws SQLException,
			UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT Pass FROM User WHERE id = ?;");
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (byte[]) tuples[0].getItem(0);
		throw new UnknownUserException("For ID: " + id);
	}

	public static byte[] saltPass(int id, byte[] pass) throws SQLException,
			UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT Salt FROM User WHERE id = ?;");
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0) {
			byte[] salt = (byte[]) tuples[0].getItem(0);
			byte[] salted = new byte[pass.length + salt.length];
			System.arraycopy(pass, 0, salted, 0, pass.length);
			System.arraycopy(salt, 0, salted, pass.length, salt.length);
			return hash(salted);
		}
		throw new UnknownUserException("For Id: " + id);
	}

	public static byte[] getPass(String name) throws SQLException,
			UnknownUserException {
		return getPass(getId(name));
	}

	public static boolean deleteAccount(int id) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("DELETE FROM User WHERE id = ?;");
		statement.setInt(1, id);
		boolean result = statement.executeUpdate() > 0;
		statement.close();
		return result;
	}

	public static int createAccount(String name, byte[] pass)
			throws SQLException {
		PreparedStatement checkStatement = DatabaseManager
				.prepare("SELECT * FROM User WHERE Name = ?;");
		checkStatement.setString(1, name);
		ResultSet checkResult = checkStatement.executeQuery();
		DatabaseManager.registerResult(checkResult, checkStatement);
		Tuple[] checkTuples = Tuple.fromResultSet(checkResult);
		if (checkTuples.length != 0)
			throw new IllegalArgumentException(
					"A user with this name already exists!");
		DatabaseManager.clean(checkResult);

		byte[] salt = generateSalt();

		PreparedStatement statement = DatabaseManager
				.prepare("INSERT INTO User VALUES (?, ?, ?, ?);");
		
		byte[] salted = new byte[512];
		byte[] raw = new byte[pass.length + salt.length];
		System.arraycopy(pass, 0, raw, 0, pass.length);
		System.arraycopy(salt, 0, raw, pass.length, salt.length);
		SHA3Digest sha3 = new SHA3Digest(512);
		sha3.update(raw, 0, raw.length);
		sha3.doFinal(salted, 0);
		
		statement.setInt(1, getNextId());
		statement.setString(2, name);
		statement.setBytes(3, salted);
		statement.setBytes(4, salt);
		
		if (statement.executeUpdate() <= 0)
			throw new RuntimeException("Create Account statement failed to create new account!");
		
		statement.close();			
		try {
			return getId(name);
		} catch (UnknownUserException e) {
			throw new RuntimeException("Create Account statement failed to create account!");
		}
	}

	private static final int getNextId() throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT MAX(Id) FROM User;");
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (int) tuples[0].getItem(0) + 1;
		throw new Error("User database is empty!");
	}

	private static final byte[] hash(byte[] in) {
		SHA3Digest sha3 = new SHA3Digest(512);
		byte[] out = new byte[512];
		sha3.update(in, 0, in.length);
		sha3.doFinal(out, 0);
		return out;
	}

	private static final byte[] generateSalt() {
		byte[] salt = new byte[256];
		SecureRandom sr = new SecureRandom();
		sr.setSeed(new Random().nextLong());
		sr.nextBytes(salt);
		return salt;
	}
}