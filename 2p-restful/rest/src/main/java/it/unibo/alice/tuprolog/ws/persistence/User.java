package it.unibo.alice.tuprolog.ws.persistence;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;

import it.unibo.alice.tuprolog.ws.security.Role;


/**
 * This entity specifies how the application persists the users' information.
 * The class contains the needed getters and setters to access the fields and
 * methods to check the validity of usernames and passwords matching them
 * against set regular expressions. The class can also generate a default
 * admin User with the specific method.</br>
 * 
 * @author Andrea Muccioli
 *
 */
@Entity
@Table(name = "USERS")
public class User implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Regular expression used to test the validity of usernames.</br></br>
	 * 
	 * Matches strings between 5 and 15 characters that can only contain lowercase
	 * characters, digits, and the symbols '_' and '-'.
	 */
	private static final String USERNAME_REGEX = "^[a-z0-9_-]{5,15}$";
	
	
	/**
	 * Regular expression uset to test the validity of passwords.</br></br>
	 * 
	 * (?=.*\\d)	--> must contain at least one digit from 0-9</br>
	 * (?=.*[a-z])	--> must contain at least one lowercase character</br>
	 * (?=.*[A-Z])	--> must contain at least one uppercase character</br>
	 * (?=.*[@#$%,])	--> must contain at least one symbol in the list "@#$%,"</br>
	 * .{6,20}	--> matches anything that satisfies the previous conditions and whose length is between 6 and 20 characters
	 * 
	 */
	private static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%,]).{6,20})";
	
	private static final String defaultAdminUsername = "admin";
	private static final String defaultAdminPsw = "Adm1n@";
	
	
	@Id
	@Column(name="username")
	private String username;
	
	@Column(name="password", nullable=false)
	private String password;
	
	@Column(name="role")
	@Enumerated(EnumType.STRING)
	private Role role;

	public User() {
	}
	
	public User(String username, String password, Role role) {
		if (!User.validatePassword(password))
			throw new IllegalArgumentException("Password is not valid");
		if (!User.validateUsername(username))
			throw new IllegalArgumentException("Username is not valid");
		this.username = username;
		this.password = password;
		this.role = role;
	}
	
	public User(String username, String password) {
		this(username, password, Role.GUEST);
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		if (!User.validateUsername(username))
			throw new IllegalArgumentException("Username is not valid");
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		if (!User.validatePassword(password))
			throw new IllegalArgumentException("Password is not valid");
		this.password = password;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	
	/**
	 * Checks the validity of the username matching it
	 * against the set regular expression.
	 * 
	 * @param username : the username to validate
	 * @return true if valid, false if not.
	 */
	public static boolean validateUsername(String username) {
		Pattern pattern = Pattern.compile(USERNAME_REGEX);
		Matcher matcher = pattern.matcher(username);
		return matcher.matches();
	}
	
	/**
	 * Checks the validity of the password matching it
	 * against the set regular expression.
	 * 
	 * @param password : the password to validate
	 * @return true if valid, false if not.
	 */
	public static boolean validatePassword(String password) {
		Pattern pattern = Pattern.compile(PASSWORD_REGEX);
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}
	
	/**
	 * Creates a default admin User, using the set defaultAdminUsername
	 * and defaultAdminPsw.
	 * 
	 * @return the created default admin User.
	 */
	public static User getDefaultAdmin() {
		return new User(defaultAdminUsername, defaultAdminPsw, Role.ADMIN);
	}
	
	@Override
	public String toString() {
		return "Username: "+username+" ; Password: "+password+" ; Role: "+role.name()+" ;";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof User))
			return false;
		User other = (User)o;
		if (other.getUsername().equals(this.username) &&
				other.getPassword().equals(this.password) &&
				other.getRole().equals(this.role))
			return true;
		return false;
	}
	
   
}