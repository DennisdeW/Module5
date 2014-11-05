package db;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sql.rowset.serial.SerialException;

import files.FileDescriptor;

public class FileStatementMaker {

	public static FileDescriptor getDescriptor(byte[] identifier)
			throws SQLException, FileNotFoundException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT * FROM \"File\" WHERE \"identifier\" = ?;");
		statement.setBytes(1, identifier);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return FileDescriptor.forTuple(tuples[0]);
		throw new FileNotFoundException("Identifier not in database!");
	}

	public static Set<FileDescriptor> getOwnedFiles(int owner)
			throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT * FROM \"File\" WHERE \"owner\" = ?;");
		statement.setInt(1, owner);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		Set<FileDescriptor> descriptors = new HashSet<>();
		Arrays.stream(tuples).forEach(
				t -> descriptors.add(FileDescriptor.forTuple(t)));
		return descriptors;
	}

	public static boolean addDescriptor(FileDescriptor fd)
			throws SerialException, SQLException {
		if (hasDescriptor(fd))
			return false;
		PreparedStatement statement = DatabaseManager
				.prepare("INSERT INTO \"File\" (\"identifier\", \"owner\", \"size\") VALUES(?, ?, ?);");
		statement.setBytes(1, fd.getIdentifier().getBytes());
		statement.setInt(2, fd.getOwner());
		statement.setLong(3, fd.getSize());
		boolean result = statement.executeUpdate() > 0;
		statement.close();
		return result;
	}

	public static boolean deleteDescriptor(FileDescriptor fd)
			throws SerialException, SQLException {
		if (!hasDescriptor(fd))
			return false;
		PreparedStatement statement = DatabaseManager
				.prepare("DELETE FROM \"File\" WHERE \"identifier\" = ?;");
		statement.setBytes(1, fd.getIdentifier().getBytes());
		boolean result = statement.executeUpdate() > 0;
		statement.close();
		return result;
	}

	public static boolean hasDescriptor(FileDescriptor fd)
			throws SerialException, SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT \"owner\" FROM \"File\" WHERE \"identifier\" = ?;");
		statement.setBytes(1, fd.getIdentifier().getBytes());
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		return tuples.length > 0;
	}

	public static int getTotalSpaceForUser(int uid) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT SUM(size) FROM \"File\" WHERE owner = ?");
		statement.setInt(1, uid);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length == 0 || tuples[0].getItems().length == 0
				|| tuples[0].getItem(0) == null)
			return 0;
		return ((BigDecimal) tuples[0].getItem(0)).intValue();
	}

}
