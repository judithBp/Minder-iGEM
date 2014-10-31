package eu.corre.minder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import android.os.AsyncTask;

public class ServerRequestTask extends AsyncTask<String, String, String> {

	private EnablesServerRequest requestEntity;
	private String context;
	private ArrayList<Object[]> result = new ArrayList<Object[]>();

	private Connection cn = null;
	private PreparedStatement pst = null;
	private Statement stmt = null;
	ResultSet rs = null;

	private final String url = "jdbc:mysql://www.corre.eu:3306/ideaweave?allowMultiQueries=true";
	private final String user = "ideaweave";
	private final String password = "H3Q4fvwDRBz94FLq";

	public ServerRequestTask(EnablesServerRequest a) {
		super();
		requestEntity = a;
	}

	@Override
	protected String doInBackground(String... query) {
		context = query[query.length - 1];
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			cn = DriverManager.getConnection(url, user, password);
			if (context.contains("update table") || context.equals("set idea")) {
				stmt = cn.createStatement();
				for (int i = 0; i < query.length - 1; i++) {
					stmt.addBatch(query[i]);
				}
				stmt.executeBatch();
			} else {
				pst = cn.prepareStatement(query[0]);
				for (int i = 1; i < query.length - 1; i++) {
					try {
						pst.setInt(i, Integer.parseInt(query[i]));
					} catch (NumberFormatException e) {
						pst.setString(i, query[i]);
					}
				}
				rs = pst.executeQuery();

				ResultSetMetaData rsmd = rs.getMetaData();
				int n, m = rsmd.getColumnCount();
				Object[] resultRow = new Object[m];
				System.out.println("It works !");
				while (rs.next()) {
					for (int j = 1; j <= m; j++) {
						System.out.println(rs.getObject(j).toString());
						if (context.equals("set idea")) {
							resultRow[j - 1] = rs.getInt(j);
						} else {
							resultRow[j - 1] = rs.getObject(j);
						}
					}
					result.add(resultRow.clone());
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (cn != null) {
					cn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
		return context;
	}

	@Override
	protected void onPostExecute(String context) {
		super.onPostExecute(context);
		requestEntity.handleRequestResult(result, context);
	}

}
