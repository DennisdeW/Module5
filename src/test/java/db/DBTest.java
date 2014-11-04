package db;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import files.FileDescriptor;

public class DBTest {

	@Test
	public void testDB() {
		String name = "TestUser";
		byte[] pass = "password".getBytes();
		int uid = -1;

		try {
			uid = UserStatementMaker.createAccount(name, pass);
		} catch (SQLException e) {
			Assert.fail("Exception occured creating account:" + e);
		}
		Assert.assertNotEquals(uid, -1);

		byte[] salted = null;
		byte[] retrieved = null;

		try {
			salted = UserStatementMaker.saltPass(uid, pass);
		} catch (SQLException | UnknownUserException e) {
			Assert.fail("Exception occured salting password:" + e);
		}
		try {
			retrieved = UserStatementMaker.getPass(uid);
		} catch (SQLException | UnknownUserException e) {
			Assert.fail("Exception occured retrieving password:" + e);
		}
		Assert.assertArrayEquals(salted, retrieved);

		byte[] forName = null;
		try {
			forName = UserStatementMaker.getPass(name);
		} catch (SQLException | UnknownUserException e) {
			Assert.fail("Exception occured retrieving password by name:" + e);
		}
		Assert.assertArrayEquals(retrieved, forName);

		ResultSet data = null;
		try {
			data = UserStatementMaker.getUserData(uid);
		} catch (SQLException e) {
			Assert.fail("Exception occured retrieving user data:" + e);
		}
		Tuple[] tuples = null;
		try {
			tuples = Tuple.fromResultSet(data);
		} catch (SQLException e) {
			Assert.fail("Exception occured converting user data:" + e);
		}
		Assert.assertTrue("getUserData() returned multiple or no rows!",
				tuples.length == 1);
		Assert.assertEquals(uid, (int) tuples[0].getItem(0));
		Assert.assertEquals(name, (String) tuples[0].getItem(3));
		Assert.assertArrayEquals(salted, (byte[]) tuples[0].getItem(1));
		
		

		FileDescriptor testFileA = new FileDescriptor("TestFileA", uid, 9L);
		FileDescriptor testFileB = new FileDescriptor("TestFileB", uid, 9L);

		try {
			FileStatementMaker.addDescriptor(testFileA);
			FileStatementMaker.addDescriptor(testFileB);
		} catch (SQLException e) {
			Assert.fail("Exception occured storing files:" + e);
		}

		try {
			Assert.assertTrue("Test File was not stored correctly!",
					FileStatementMaker.hasDescriptor(testFileA));
		} catch (SQLException e1) {
			Assert.fail("Exception occured checking for file presence:" + e1);
		}

		try {
			Assert.assertEquals(18, FileStatementMaker.getTotalSpaceForUser(uid));
		} catch (SQLException e2) {
			Assert.fail("Exception occured getting file size:" + e2);
		}
		
		Set<FileDescriptor> actualFiles = new HashSet<>();
		actualFiles.addAll(Arrays.asList(testFileA, testFileB));
		Set<FileDescriptor> retrievedFiles = null;
		try {
			retrievedFiles = FileStatementMaker.getOwnedFiles(uid);
		} catch (SQLException e) {
			Assert.fail("Exception occured retrieving files by owner:" + e);
		}
		Assert.assertEquals(actualFiles, retrievedFiles);

		FileDescriptor forIdentifier = null;
		try {
			forIdentifier = FileStatementMaker.getDescriptor(testFileA
					.getIdentifier().getBytes());
		} catch (FileNotFoundException | SQLException e) {
			Assert.fail("Exception occured retrieving files by identifier:" + e);
		}
		Assert.assertEquals(forIdentifier, testFileA);
		
		try {
			Assert.assertTrue("File A was not deleted!", FileStatementMaker.deleteDescriptor(testFileA));
			Assert.assertTrue("File B was not deleted!", FileStatementMaker.deleteDescriptor(testFileB));
		} catch (SQLException e) {
			Assert.fail("Exception occured deleting files:" + e);
		}
		
		try {
			UserStatementMaker.deleteAccount(uid);
		} catch (SQLException e1) {
			Assert.fail("Exception occured deleting user:" + e1);
		}
	}

}
