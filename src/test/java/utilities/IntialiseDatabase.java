package utilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

public class IntialiseDatabase {

	static final String SQL_CLEAR_ACCOUNTS = "DELETE FROM accounts WHERE 1 = 1 ;";
	static final String SQL_CLEAR_USERS = "DELETE FROM users WHERE 1 = 1 ;";
	static final String SQL_CLEAR_TRANSACTIONS = "DELETE FROM transactions WHERE 1 = 1 ;";
	static final String SQL_CLEAR_PAYEES = "DELETE FROM payees WHERE 1 = 1 ;";
	static final String SQL_REINITIALISE_SEQUENCE_ACCONT = "ALTER SEQUENCE accounts_account_nb_seq RESTART WITH 121212;";
	
	static final String DB_URL = "jdbc:postgresql://34.195.216.204:5432/bankapp";
	static final String USER = "postgres";
	static final String PASS = "admin";
	static final String DRIVER = "org.postgresql.Driver";
	
	static {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		    /*
			 * Clear all tables & Reinitialise sequence  
			 */
			try {
				Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				
				PreparedStatement stmt= conn.prepareStatement(SQL_CLEAR_ACCOUNTS);
				stmt.executeUpdate();
				stmt= conn.prepareStatement(SQL_CLEAR_USERS);
				stmt.executeUpdate();
				stmt= conn.prepareStatement(SQL_CLEAR_TRANSACTIONS);
				stmt.executeUpdate();
				stmt= conn.prepareStatement(SQL_CLEAR_TRANSACTIONS);
				stmt.executeUpdate();
				stmt= conn.prepareStatement(SQL_REINITIALISE_SEQUENCE_ACCONT);
				stmt.executeUpdate();
				
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		    /*
			 * CREATE the user henry
			 * 
			 * // {"username":"henry","email":"henry@hotmail.com","password":"henry"}
			 *    
			 */
			JSONObject body = new JSONObject();
			
			body.put("username", "henry");
			body.put("email", "henry@hotmail.com");
			body.put("password", "henry");

		    CloseableHttpClient httpclient = HttpClients.createDefault();
		    StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

		    HttpPost postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
		    postMethod.setEntity(requestEntity);

		    HttpResponse httpResponse;
		    
			try {
				httpResponse = httpclient.execute(postMethod);
				String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				
				System.out.println("\n //////// CREATION OF the USER henry  //////// \n");
				System.out.println("\n"+ response +"\n");

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		    /*
			 * CREATE the user jenny
			 * 
			 * // {"username":"jenny","email":"jenny@hotmail.com","password":"jenny"}
			 *    
			 */
			body = new JSONObject();
			
			body.put("username", "jenny");
			body.put("email", "jenny@hotmail.com");
			body.put("password", "jenny");

		    httpclient = HttpClients.createDefault();
		    requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

		    postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
		    postMethod.setEntity(requestEntity);
		    
			try {
				httpResponse = httpclient.execute(postMethod);
				String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				
				System.out.println("\n //////// CREATION OF the USER jenny  //////// \n");
				System.out.println("\n"+ response +"\n");

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
	    

	}

}
