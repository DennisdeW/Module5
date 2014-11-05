package db;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bouncycastle.crypto.digests.SHA3Digest;

public class UserStatementMaker {

	/**
	 * Gets the ID of a user with the given name.
	 *
	 * @param name
	 *            The name to find the id for
	 * @return The id corresponding to the name
	 * @throws SQLException
	 *             If the generated statements cannot be executed
	 * @throws UnknownUserException
	 *             If there is no user with this name
	 */
	public static int getId(String name) throws SQLException,
	UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT id FROM \"Users\" WHERE \"name\" = ?;");
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (int) tuples[0].getItem(0);
		throw new UnknownUserException("For Name: " + name);
	}

	/**
	 * Gets all data information about the user corresponding to id.<br>
	 * <b>Do not forget to call DatabaseManager.clean() on the result!</b>
	 * Tuple.fromResultSet() will also do this.
	 *
	 * @param id
	 *            The user's id
	 * @return A ResultSet containing the result of the query <i>SELECT * FROM
	 *         User WHERE id = [id]</i>
	 * @throws SQLException
	 *             If the generated statements cannot be executed
	 */
	public static ResultSet getUserData(int id) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT * FROM \"Users\" WHERE \"id\" = ?;");
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		return result;
	}

	/**
	 * Gets the (salted and hashed) password corresponing to the given id.
	 *
	 * @param id
	 *            A user's id.
	 * @return The salted and hashed password belonging to the user.
	 * @throws SQLException
	 *             If the generated statements cannot be executed
	 * @throws UnknownUserException
	 *             If there is no user with this id
	 */
	public static byte[] getPass(int id) throws SQLException,
	UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT \"pass\" FROM \"Users\" WHERE \"id\" = ?;");
		statement.setInt(1, id);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (byte[]) tuples[0].getItem(0);
		throw new UnknownUserException("For ID: " + id);
	}

	/**
	 * Salts and hashes the given password with the salt corresponding to id.
	 *
	 * @param id
	 *            The user's id
	 * @param pass
	 *            The password to salt and hash
	 * @return The salted and hashed password
	 * @throws SQLException
	 *             If the generated statements cannot be executed
	 * @throws UnknownUserException
	 *             If there is no user with this id
	 */
	public static byte[] saltPass(int id, byte[] pass) throws SQLException,
	UnknownUserException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT \"salt\" FROM \"Users\" WHERE \"id\" = ?;");
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

	/**
	 * Convenience method to get a password:
	 * <code>return getPass(getId(name));</code>
	 *
	 * @param name
	 *            The user's name.
	 * @return The user's (salted and hashed) password
	 * @throws SQLException
	 *             If the generated statements cannot be executed
	 * @throws UnknownUserException
	 *             If there is no user with this name OR getId(name) returned an
	 *             improper result
	 */
	public static byte[] getPass(String name) throws SQLException,
	UnknownUserException {
		return getPass(getId(name));
	}

	/**
	 * Deletes the account with this id
	 *
	 * @param id
	 *            The id of the user to delete
	 * @return
	 * @throws SQLException
	 */
	public static boolean deleteAccount(int id) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("DELETE FROM \"Users\" WHERE \"id\" = ?;");
		statement.setInt(1, id);
		boolean result = statement.executeUpdate() > 0;
		statement.close();
		return result;
	}

	public static int createAccount(String name, byte[] pass)
			throws SQLException {
		PreparedStatement checkStatement = DatabaseManager
				.prepare("SELECT * FROM \"Users\" WHERE \"name\" = ?;");
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
				.prepare("INSERT INTO \"Users\" (\"id\", \"name\", \"pass\", \"salt\")"
						+ " VALUES (?, ?, ?, ?);");

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
			throw new RuntimeException(
					"Create Account statement failed to create new account!");

		statement.close();
		try {
			return getId(name);
		} catch (UnknownUserException e) {
			throw new RuntimeException(
					"Create Account statement failed to create account!");
		}
	}

	public static List<String> getUserNameList() throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT \"name\" FROM \"Users\";");
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		List<String> names = new ArrayList<>();
		for (Tuple t : tuples)
			names.add((String) t.getItem(0));
		return names;
	}

	private static final int getNextId() throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT MAX(\"id\") FROM \"Users\";");
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (int) tuples[0].getItem(0) + 1;
		throw new Error("User database is empty!");
	}

	public static boolean accountExists(String name) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT * FROM \"Users\" WHERE name = ?");
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		return tuples.length > 0;
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