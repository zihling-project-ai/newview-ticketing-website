package com.newview.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Util {
//	驅動driver
	public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
//	mysql url
	public static final String URL = System.getenv().getOrDefault("NEWVIEW_DB_URL", "jdbc:mysql://127.0.0.1:3309/newview_recovery?serverTimezone=Asia/Taipei");
//	db account/password
	public static final String USER = System.getenv().getOrDefault("NEWVIEW_DB_USER", "newview");
	public static final String PASSWORD = System.getenv().getOrDefault("NEWVIEW_DB_PASSWORD", "");

	public static void closeResource(Connection con, PreparedStatement pstmt, ResultSet rs) {
   
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}

				if (con != null) {
					try {
						con.close();
					} catch (SQLException se) {
						se.printStackTrace();
					}

				}
			}
		}
	}
}