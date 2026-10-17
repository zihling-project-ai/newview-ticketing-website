//package com.newview.compic.model;
//
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//
//import com.newview.util.Util;
//
//public class ComPicJDBCDAO implements ComPicDAO_Interface {
//
//	private static final String INSERT_STMT = "INSERT INTO  comPic (orderListID, comPic) VALUES (?,?)";
//	private static final String GET_ALL_STMT = "SELECT comPicID,orderListID,comPic FROM  comPic order by comPicID";
//	private static final String GET_ONE_STMT = "SELECT comPicID,orderListID,comPic  FROM comPic where comPicID = ?";
//	private static final String DELETE = "DELETE FROM comPic where comPicID = ?";
//
//	static {
//		try {
//			Class.forName(Util.DRIVER);
//		} catch (ClassNotFoundException ce) {
//			ce.printStackTrace();
//		}
//	}
//
//	@Override
//	public void insert(ComPicVO comPicID) {
//		// TODO Auto-generated method stub
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//
//			con = DriverManager.getConnection(Util.URL, Util.USER, Util.PASSWORD);
//			pstmt = con.prepareStatement(INSERT_STMT);
//			pstmt.setInt(1, comPicID.getOrderListID());
//			pstmt.setBytes(2, comPicID.getComPic());
//			pstmt.executeUpdate();
//
//			// Handle any driver errors
//		} catch (SQLException se) {
//			throw new RuntimeException("A database error occured. " + se.getMessage());
//			// Clean up JDBC resources
//		} finally {
//			Util.closeResource(con, pstmt, null);
//		}
//
//	}
//
//	@Override
//	public void delete(Integer comPicID) {
//		// TODO Auto-generated method stub
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//
//			con = DriverManager.getConnection(Util.URL, Util.USER, Util.PASSWORD);
//			pstmt = con.prepareStatement(DELETE);
//
//			pstmt.setInt(1, comPicID);
//
//			pstmt.executeUpdate();
//
//			// Handle any driver errors
//		} catch (SQLException se) {
//			throw new RuntimeException("A database error occured. " + se.getMessage());
//			// Clean up JDBC resources
//		} finally {
//			Util.closeResource(con, pstmt, null);
//		}
//
//	}
//
//	@Override
//	public ComPicVO findByPrimaryKey(Integer comPicID) {
//		ComPicVO comPicVO = null;
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//
//			con = DriverManager.getConnection(Util.URL, Util.USER, Util.PASSWORD);
//			pstmt = con.prepareStatement(GET_ONE_STMT);
//
//			pstmt.setInt(1, comPicID);
//
//			rs = pstmt.executeQuery();
//
//			while (rs.next()) {
//
//				comPicVO = new ComPicVO();
//				comPicVO.setComPicID(rs.getInt("comPicID"));
//				comPicVO.setOrderListID(rs.getInt("orderListID"));
//				comPicVO.setComPic(rs.getBytes("comPic"));
//
//			}
//
//			// Handle any driver errors
//		} catch (SQLException se) {
//			throw new RuntimeException("A database error occured. " + se.getMessage());
//			// Clean up JDBC resources
//		} finally {
//			Util.closeResource(con, pstmt, rs);
//		}
//
//		return comPicVO;
//
//	}
//
//	@Override
//	public List<ComPicVO> getAll() {
//		List<ComPicVO> list = new ArrayList<ComPicVO>();
//
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//
//			con = DriverManager.getConnection(Util.URL, Util.USER, Util.PASSWORD);
//			pstmt = con.prepareStatement(GET_ALL_STMT);
//			rs = pstmt.executeQuery();
//
//			while (rs.next()) {
//				ComPicVO comPicVO = new ComPicVO();
//				comPicVO.setComPicID(rs.getInt("comPicID"));
//				comPicVO.setOrderListID(rs.getInt("orderListID"));
//				comPicVO.setComPic(rs.getBytes("comPic"));
//
//				list.add(comPicVO); // Store the row in the list
//			}
//
//			// Handle any driver errors
//		} catch (SQLException se) {
//			throw new RuntimeException("A database error occured. " + se.getMessage());
//			// Clean up JDBC resources
//		} finally {
//			Util.closeResource(con, pstmt, rs);
//		}
//
//		return list;
//
//	}
//
//	public byte[] gifToByteArray(String gif) throws IOException {
//		File gifFile = new File(gif);
//
//		BufferedImage image = ImageIO.read(gifFile);
//
//		if (image != null) {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ImageIO.write(image, "gif", baos);
//			return baos.toByteArray();
//		} else {
//			throw new IOException("GIF?????");
//		}
//	}
//
//	public static byte[] getPictureByteArray(String path) throws IOException {
//		FileInputStream fis = new FileInputStream(path);
//
//		byte[] buffer = fis.readAllBytes();
//		fis.close();
//		return buffer;
//
//	}
//
//	public static void main(String[] args) {
//		ComPicJDBCDAO comPicJDBCDAO = new ComPicJDBCDAO();
//		ComPicVO comPicVO = new ComPicVO();
//		
//		// 新增 OK
////		comPicVO.setOrderListID(3);
////		try {
////
////			comPicVO.setComPic(comPicJDBCDAO.gifToByteArray("image/ok2.jpg"));
//////			comPicVO.setComPic(comPicJDBCDAO.getPictureByteArray(
//////					"C:/THA103_WebApp/eclipse_WTP_workspace1/THA103G5_All_ver9/src/main/java/com/newview/model/compic/image/ok2.jpg"));
////
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////
////		comPicJDBCDAO.insert(comPicVO);
//
////		//刪除 OK
////		comPicJDBCDAO.delete(7);
//
////		//查單一 OK
////		comPicVO = comPicJDBCDAO.findByPrimaryKey(6);
////		System.out.print(comPicVO.getComPicID() + ",");
////		System.out.print(comPicVO.getOrderListID() + ",");
////		System.out.print(comPicVO.getComPic() + ",");
//
////		//全部 OK
//		List<ComPicVO> list = comPicJDBCDAO.getAll();
//		for (ComPicVO cList : list) {
//			System.out.println(cList);
//		}
//
//	}
//}
