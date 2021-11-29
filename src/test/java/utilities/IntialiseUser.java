package utilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class IntialiseUser {

	static final String SQL_CLEAR_ACCOUNTS = "DELETE FROM accounts WHERE 1 = 1 ;";
	static final String SQL_CLEAR_USERS = "DELETE FROM users WHERE 1 = 1 ;";
	static final String SQL_CLEAR_TRANSACTIONS = "DELETE FROM transactions WHERE 1 = 1 ;";
	static final String SQL_CLEAR_PAYEES = "DELETE FROM payees WHERE 1 = 1 ;";
	static final String SQL_REINITIALISE_SEQUENCE_ACCONT = "ALTER SEQUENCE accounts_account_nb_seq RESTART WITH 121212;";
	
	static final String DB_URL = "jdbc:postgresql://34.195.216.204:5432/bankapp";
	static final String USER = "postgres";
	static final String PASS = "admin";
	static final String DRIVER = "org.postgresql.Driver";
	
	static final String SQL_LIST_USERS = "SELECT * FROM users;";
	
	static final String username = "richard";

	static final String SQL_CLEAR_USER_TRANSACTIONS = "DELETE FROM transactions WHERE from_user_id = ? OR to_user_id = ?  ;";
	static final String SQL_CLEAR_USER_PAYEES = "DELETE FROM payees WHERE user_id = ? OR payee_id = ? ;";
	static final String SQL_CLEAR_USER_BALANCE = "UPDATE accounts SET balance = ? WHERE user_id= ? ;";
	
	static {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		 /*
		 * List all users
		 */
		ResultSet usersRS = null;
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			
			PreparedStatement stmt= conn.prepareStatement(SQL_LIST_USERS);			
			usersRS = stmt.executeQuery();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		JSONObject body;

		try{
			boolean userExists = false;
			int user_id = 0;
			
			if(usersRS != null) {
				while(usersRS.next()) {
					if(usersRS.getString("username").equals(username)) {
						userExists = true;
						user_id = usersRS.getInt("id");
						
						System.out.println("exists");
						System.out.println(user_id);
						
						break;
					}
				}
			}
			if(!userExists) {
				/*
				 * CREATE THE USER
				 */
				body = new JSONObject();
				
				body.put("username", username);
				body.put("email", username+"@hotmail.com");
				body.put("password", username);

			    CloseableHttpClient httpclient = HttpClients.createDefault();
			    StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

			    HttpPost postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
			    postMethod.setEntity(requestEntity);

				try {
					HttpResponse httpResponse = httpclient.execute(postMethod);
					String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
					
					System.out.println("\n //////// CREATION OF the USER "+ username +" //////// \n");
					System.out.println("\n"+ response +"\n");

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else {
				/*
				 * USER EXISTS DELETE USER'S TRANSACTIONS, PAYEES and RESET BALANCE
				 */
				
				try {
					PreparedStatement stmt= conn.prepareStatement(SQL_CLEAR_ACCOUNTS);

					stmt= conn.prepareStatement(SQL_CLEAR_USER_TRANSACTIONS);
					stmt.setInt(1, user_id);
					stmt.setInt(2, user_id);
					stmt.executeUpdate();
					
					stmt= conn.prepareStatement(SQL_CLEAR_USER_PAYEES);
					stmt.setInt(1, user_id);
					stmt.setInt(2, user_id);
					stmt.executeUpdate();
					
					stmt= conn.prepareStatement(SQL_CLEAR_USER_BALANCE);
					stmt.setInt(1, 10000);
					stmt.setInt(2, user_id);
					stmt.executeUpdate();
					
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	}

}
