package edu.pdx.cs.multiview.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseAction {

	private static final String JDBC_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

	public DatabaseAction() {
		super();
	}

	public String getDatabase() {
		final String string = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + getDatabaseLocation() +
			";DriverID=22;READONLY=true}";
		return string;
	}

	public abstract String getDatabaseLocation();

	public Connection getDatabaseConnection() throws SQLException {
		
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(getDatabase(),"","");
	}

}