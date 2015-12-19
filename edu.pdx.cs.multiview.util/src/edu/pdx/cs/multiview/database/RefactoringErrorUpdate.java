package edu.pdx.cs.multiview.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RefactoringErrorUpdate extends DatabaseAction{

	@Override
	public String getDatabaseLocation() {
		return "C:/Documents and Settings/Emerson/My Documents/eclipse_workspaces/" +
				"workspace3.3/Thesis/Errors Generalization/tool_errors.mdb";
	}

	public static void main(String args[]) {
		
	    Connection con = null;
		Statement stmt;
		ResultSet rs;
		
		List<String> updates = new ArrayList<String>(1300);

		try {

			con = new RefactoringErrorUpdate()
					.getDatabaseConnection();

			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery("SELECT * FROM messages");

			while(rs.next()){
				String description = rs.getString("description").replace("\'", "''");;
				String[] keyValue = description.split("=");
				
				if(keyValue.length!=2){
					continue;
				}
				
				//rs.updateString("aKey",keyValue[0]);
				//rs.updateString("aValue",keyValue[1]);
				//rs.updateString("refactoring_name", description.split("_")[0]);
				
				//rs.updateRow();
				String updateString = "UPDATE messages SET aKey='" +
							keyValue[0] +
						"', aValue='" +
							keyValue[1] +
						"', refactoring_name='" +
						description.split("_")[0] +
						"' WHERE line=" +
							rs.getInt("line");
				updates.add(updateString);
				//stmt.executeUpdate(updateString);
			}

			rs.close();
			stmt.close();
			
			stmt = con.createStatement();
			for(String update : updates){
				stmt.execute(update);
				//stmt.addBatch(update);
			}
			//stmt.executeBatch();
			
			stmt.close();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException ignore) {}
		}
	}
}
