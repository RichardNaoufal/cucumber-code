package seleniumcode;

import io.cucumber.java.After;

import static org.junit.Assert.assertEquals;

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ExampleSteps {

	private WebDriver driver;
	
	static final String SQL_CLEAR_ACCOUNTS = "DELETE FROM accounts WHERE 1 = 1 ;";
	static final String SQL_CLEAR_USERS = "DELETE FROM users WHERE 1 = 1 ;";
	static final String SQL_CLEAR_TRANSACTIONS = "DELETE FROM transactions WHERE 1 = 1 ;";
	static final String SQL_CLEAR_PAYEES = "DELETE FROM payees WHERE 1 = 1 ;";
	static final String SQL_REINITIALISE_SEQUENCE_ACCONT = "ALTER SEQUENCE accounts_account_nb_seq RESTART WITH 121212;";
	
	static final String SQL_LIST_USERS = "SELECT * FROM users;";
	static final String SQL_LIST_PAYEES = "SELECT * FROM payees;";
	static final String SQL_LIST_ACCOUNTS = "SELECT * FROM accounts;";
	
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
	/*
		cd existing_folder
		git init
		git remote add origin http://ec2-34-203-178-61.compute-1.amazonaws.com/root/selenium-code.git
		git add .
		git commit -m "Initial commit"
		git push -u origin master
	*/
	
	static final String SQL_CLEAR_USER_TRANSACTIONS = "DELETE FROM transactions WHERE from_user_id = ? OR to_user_id = ?  ;";
	static final String SQL_CLEAR_USER_PAYEES = "DELETE FROM payees WHERE user_id = ? OR payee_id = ? ;";
	static final String SQL_CLEAR_USER_BALANCE = "UPDATE accounts SET balance = ? WHERE user_id= ? ;";
	
	@Given("the user {string} has been re-initialised") 
    public void initialiseUser(String username) {	

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
					PreparedStatement stmt= conn.prepareStatement(SQL_CLEAR_USER_TRANSACTIONS);
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
	
	@Given("the database has been re-initialised") 
    public void initialiseDatabase() {

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
			stmt= conn.prepareStatement(SQL_CLEAR_PAYEES);
			stmt.executeUpdate();
			stmt= conn.prepareStatement(SQL_REINITIALISE_SEQUENCE_ACCONT);
			stmt.executeUpdate();
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		ResultSet usersRS = null;
		
		try {
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			
			PreparedStatement stmt= conn.prepareStatement(SQL_LIST_USERS);
			stmt.executeUpdate();
			
			usersRS = stmt.executeQuery();
			
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
		JSONObject body;
		try{
			boolean henryExists = false;
			if(usersRS != null) {
				while(usersRS.next()) {
					if(usersRS.getString("username").equals("henry")) {
						henryExists = true;
						break;
					}
				}
			}
			if(henryExists) {
				body = new JSONObject();
				
				body.put("username", "henry");
				body.put("email", "henry@hotmail.com");
				body.put("password", "henry");

			    CloseableHttpClient httpclient = HttpClients.createDefault();
			    StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

			    HttpPost postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
			    postMethod.setEntity(requestEntity);

				try {
					HttpResponse httpResponse = httpclient.execute(postMethod);
					String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
					
					System.out.println("\n //////// CREATION OF the USER henry  //////// \n");
					System.out.println("\n"+ response +"\n");

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	    /*
		 * CREATE the user jenny
		 * 
		 * // {"username":"jenny","email":"jenny@hotmail.com","password":"jenny"}
		 *    
		 */
		try{
			boolean jennyExists = false;
			usersRS.beforeFirst();
			if(usersRS != null) {
				while(usersRS.next()) {
					if(usersRS.getString("username").equals("jenny")) {
						jennyExists = true;
						break;
					}
				}
			}
			if(jennyExists) {
				body = new JSONObject();
				
				body.put("username", "jenny");
				body.put("email", "jenny@hotmail.com");
				body.put("password", "jenny");

				CloseableHttpClient httpclient = HttpClients.createDefault();
				StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

				HttpPost postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
			    postMethod.setEntity(requestEntity);
			    
				try {
					HttpResponse httpResponse = httpclient.execute(postMethod);
					String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
					
					System.out.println("\n //////// CREATION OF the USER jenny  //////// \n");
					System.out.println("\n"+ response +"\n");

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
		usersRS = null;
		ResultSet accountsRS = null;
		ResultSet payeesRS = null;
		
		try {
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			
			PreparedStatement stmt= conn.prepareStatement(SQL_LIST_USERS);
			stmt.executeUpdate();
			usersRS = stmt.executeQuery();
			
			stmt= conn.prepareStatement(SQL_LIST_ACCOUNTS);
			stmt.executeUpdate();
			accountsRS = stmt.executeQuery();
			
			stmt= conn.prepareStatement(SQL_LIST_PAYEES);
			stmt.executeUpdate();
			payeesRS = stmt.executeQuery();
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*
		 * ADD payee henry to user jenny
		 *    
		 */
		try{
			boolean jennyExists = false;
			usersRS.beforeFirst();
			if(usersRS != null) {
				while(usersRS.next()) {
					if(usersRS.getString("username").equals("jenny")) {
						jennyExists = true;
						break;
					}
				}
			}
			if(jennyExists) {
				body = new JSONObject();
				
				body.put("username", "jenny");
				body.put("email", "jenny@hotmail.com");
				body.put("password", "jenny");

				CloseableHttpClient httpclient = HttpClients.createDefault();
				StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

				HttpPost postMethod = new HttpPost("http://34.195.216.204/bankappapi/register");
			    postMethod.setEntity(requestEntity);
			    
				try {
					HttpResponse httpResponse = httpclient.execute(postMethod);
					String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
					
					System.out.println("\n //////// CREATION OF the USER jenny  //////// \n");
					System.out.println("\n"+ response +"\n");

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
    }
	
	@Given("I have opened the browser") 
    public void openBrowser() {
	System.setProperty("webdriver.chrome.driver", "/opt/google/chrome/chrome");
    	ChromeOptions options = new ChromeOptions();
    	options.addArguments("--headless");
		this.driver = new ChromeDriver(options);
		this.driver.manage().window().maximize();
    }

    @Given("I opened the online banking app") 
    public void goToBankingApp() { 
       driver.navigate().to("http://34.195.216.204/onlinebanking/");
       try {
    	   Thread.sleep(7000);
       } catch (InterruptedException e) {}
    } 
 	
    @Then("I should see username and password fields") 
    public void usernamePasswordFieldsExist() { 
    	driver.findElement(By.id("username"));
    	driver.findElement(By.id("password"));
     } 
    
    @When("I login with username {string} and password {string}") 
    public void login(String username, String password ) { 
    	// fill username if enabled
    	if(!driver.findElement(By.id("username")).isEnabled()) { 
    		assertEquals(true, false);
    		return;
    	}else {
    		WebElement usernameF = driver.findElement(By.id("username"));
    		usernameF.sendKeys(username);
    	}
    	// fill password if enabled
    	if(!driver.findElement(By.id("password")).isEnabled()) { 
    		assertEquals(true, false);
    		return;
    	}else {
    		WebElement passwordF = driver.findElement(By.id("password"));
    		passwordF.sendKeys(password);
    	}
    	
    	// login
    	driver.findElement(By.id("signin")).click(); 
        
    	try {
     	   Thread.sleep(2000);
        } catch (InterruptedException e) {}
        
        driver.findElement(By.id("account-balance"));
        
    } 
    
    @Then("I should see a credit of {string} in the accounts page")
    public void accountCreditValue(String credit) { 
    	// check the account balance
    	if(driver.findElement(By.id("account-balance")).isDisplayed()) { 
    		assertEquals(credit, driver.findElement(By.id("account-balance")).getText());
    	}
    	
    	try {
      	   Thread.sleep(2000);
         } catch (InterruptedException e) {}
    }    
    
    @When("I add a payee with username {string} and account number {string}") 
    public void addPayee(String username, String accountnb) { 
    	// navigate to the payee page 
    	driver.findElement(By.id("payees-page")).click();
    	
    	try {
      	   Thread.sleep(2000);
        } catch (InterruptedException e) {}
    	
    	// fill username
    	if(!driver.findElement(By.id("payee-username")).isEnabled()) { 
    		assertEquals(true, false);
    		return;
    	}else {
    		WebElement usernameF = driver.findElement(By.id("payee-username"));
    		usernameF.sendKeys(username);
    	}
    	
    	
    	
    	// fill acount number
    	if(!driver.findElement(By.id("payee-acountnb")).isEnabled()) { 
    		assertEquals(true, false);
    		return;
    	}else {
    		WebElement usernameF = driver.findElement(By.id("payee-acountnb"));
    		usernameF.sendKeys(accountnb);
    	}
    	
    	// add payee
    	driver.findElement(By.id("payee-add")).click(); 
        
    	try {
     	   Thread.sleep(2000);
        } catch (InterruptedException e) {}
    	
    }

    @When("I delete a payee with username {string} and account number {string}") 
    public void deletePayee(String username, String accountnb) { 
    	// navigate to the payee page 
    	driver.findElement(By.id("payees-page")).click();
    	
    	try {
      	   Thread.sleep(2000);
        } catch (InterruptedException e) {}
    	
    	// delete payee
    	if(!driver.findElement(By.id(username+"_"+accountnb)).isEnabled()) { 
    		assertEquals(true, false);
    		return;
    	}else {
    		driver.findElement(By.id(username+"_"+accountnb)).click();
    	}
        
    	try {
     	   Thread.sleep(2000);
        } catch (InterruptedException e) {}
    	
    }
    
    @Then("I should see a number of {string} payees with username {string} and account number {string} in the manage payee page")
    public void singlePayeeInManagePayeePage(String number, String username, String accountnb) { 
    	// navigate to the payee page 
    	driver.findElement(By.id("payees-page")).click();
    	
    	try {
      	   Thread.sleep(4000);
        } catch (InterruptedException e) {}
    	
    	// check the payee has been added
    	System.out.println(driver.findElements(By.id(username)).size());
    	System.out.println(Integer.parseInt(number));
    	
    	if(driver.findElements(By.id(username)).size() != Integer.parseInt(number)) { 
    		assertEquals(true, false);
    	}
    	if(driver.findElements(By.id(accountnb)).size() != Integer.parseInt(number)) { 
    		assertEquals(true, false);
    	}
    	
    	try {
       	   Thread.sleep(5000);
         } catch (InterruptedException e) {}
    	
    } 

    @Then("I should see an error message {string} in the manage payee page")
    public void errorMessageInPayeePage(String message) { 
    	
    	// check the account balance
    	if(driver.findElement(By.id("payee-error")).isDisplayed()) { 
    		assertEquals(message, driver.findElement(By.id("payee-error")).getText());
    	}
    	
    	try {
      	   Thread.sleep(2000);
         } catch (InterruptedException e) {}
    }
    
    
    
	@After()
	public void closeBrowser() {
		this.driver.quit();
	}
   
}
