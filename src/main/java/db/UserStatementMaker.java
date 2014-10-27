package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserStatementMaker {

	public static int getId(String name) throws SQLException {
		PreparedStatement statement = DatabaseManager
				.prepare("SELECT id FROM User WHERE Name = ?;");
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();
		DatabaseManager.registerResult(result, statement);
		Tuple[] tuples = Tuple.fromResultSet(result);
		if (tuples.length > 0)
			return (int) tuples[0].getItem(0);
		return -1;
	}

	public static ResultSet getUserData(int id) throws SQLException {
		
		return null;
	}
	
}
