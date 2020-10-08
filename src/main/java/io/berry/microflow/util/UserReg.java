package io.berry.microflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.jdbc.JDBCAuth;

public class UserReg {

	private static String fsalt = null;
	private static AuthProvider auth = null;
	
	public static void main(String[] args) {
		if(args.length != 3) {
			printUsage();
			return;
		}
		
		switch(args[0]) {
		case "add":
			addUser(args[1], args[2]);
			break;
		case "remove":
			removeUser(args[1], args[2]);
			break;
		case "passwd":
			changePasswd(args[1], args[2]);
			break;
		}
		System.out.print("End - " + args[0] + " " + args[1] + " " + args[2]);
	}

	private static void addUser(String username, String password) {
		Connection conn = getConnection();
		try {
			String paswd_salt = getPasswordSalt();
			String pswd_enc = getPasswordEncord(password, paswd_salt);
			
			PreparedStatement pst = conn.prepareStatement("insert into user (username, password, password_salt) values (?, ?, ?) ");
			pst.setString(1, username);
			pst.setString(2, pswd_enc);
			pst.setString(3, paswd_salt);
			pst.execute();			
			pst.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static void removeUser(String username, String password) {
		Connection conn = getConnection();
		try {
			String paswd_salt = getPasswordSalt();
			String pswd_enc = getPasswordEncord(password, paswd_salt);
			
			PreparedStatement pst = conn.prepareStatement("delete from user where username = ? and password = ?");
			pst.setString(1, username);
			pst.setString(2, pswd_enc);
			if(pst.execute()) {
				System.out.println("removeUser : (" + username + ", " + password + ") is not delete !");
				return;
			}
			pst.close();

			pst = conn.prepareStatement("delete from roles_perms where role in (select role from user_roles where username = ? ");
			pst.setString(1, username);
			pst.execute();			
			pst.close();

			pst = conn.prepareStatement("delete from user_roles where username = ? ");
			pst.setString(1, username);
			pst.execute();			
			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static void changePasswd(String username, String password) {
		Connection conn = getConnection();
		try {
			String paswd_salt = getPasswordSalt();
			String pswd_enc = getPasswordEncord(password, paswd_salt);
			
			PreparedStatement pst = conn.prepareStatement("update user password = ?, password_salt = ? where username = ? ");
			pst.setString(1, pswd_enc);
			pst.setString(2, paswd_salt);
			pst.setString(3, username);
			pst.execute();			
			pst.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static String getPasswordEncord(String password, String passwd_salt) {
		// TODO Auto-generated method stub
		return "null";
	}

	private static String getPasswordSalt() {
		if(fsalt == null) {
			fsalt = 
		}
		return fsalt;
	}

	private static Connection getConnection() {
		try {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = DriverManager.getConnection(
				"jdbc:hsqldb:file:./db/flowdb;shutdown=true;create=true", 
				"SA", 
				"");
		return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void printUsage() {
		System.out.println("Useage : UserReg {command} 'username' 'password'");
		System.out.println("         command = ['add' | 'remove' | 'passwd']");
	}

}
