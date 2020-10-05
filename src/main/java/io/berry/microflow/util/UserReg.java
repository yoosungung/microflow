package io.berry.microflow.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserReg {

	public static void main(String[] args) {
		if(args.length != 4) {
			printUsage();
			return;
		}
		
		switch(args[1]) {
		case "add":
			addUser(args[2], args[3]);
			break;
		case "remove":
			removeUser(args[2], args[3]);
			break;
		case "passwd":
			changePasswd(args[2], args[3]);
			break;
		}

	}

	private static void changePasswd(String username, String password) {
		Connection conn = getConnection();
		try {
			PreparedStatement pst = conn.prepareStatement("insert into aa (aa) values (?) ");
			pst.setString(0, "aa");
			pst.execute();			
		} catch (SQLException e) {
			System.out.print(e);
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static void removeUser(String username, String password) {
		Connection conn = getConnection();
		try {
			PreparedStatement pst = conn.prepareStatement("insert into aa (aa) values (?) ");
			pst.setString(0, "aa");
			pst.execute();			
		} catch (SQLException e) {
			System.out.print(e);
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static void addUser(String username, String password) {
		Connection conn = getConnection();
		try {
			PreparedStatement pst = conn.prepareStatement("insert into aa (aa) values (?) ");
			pst.setString(0, "aa");
			pst.execute();			
		} catch (SQLException e) {
			System.out.print(e);
		} finally {
			try { conn.close(); } catch (Exception e) {}
		}		
	}

	private static Connection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	private static void printUsage() {
		System.out.println("Useage : UserReg {command} 'username' 'password'");
		System.out.println("         command = ['add' | 'remove' | 'passwd']");
	}

}
