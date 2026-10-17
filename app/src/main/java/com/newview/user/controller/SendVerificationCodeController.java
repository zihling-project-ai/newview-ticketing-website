package com.newview.user.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.user.model.UserVO;
import com.newview.user.service.UserServiceImpl;

import redis.clients.jedis.Jedis;

@WebServlet("/SendVerificationCode")
public class SendVerificationCodeController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		HttpSession session = req.getSession();
		HashMap<String, String> data = new HashMap<>();
		String json = null;
		Gson gson = new Gson();
		PrintWriter out = res.getWriter();

		// 取得 session 中存取的 userID
		Integer userID = Integer.valueOf((String) session.getAttribute("userID"));
		System.out.println("sendverificationCode's userID: " + userID);

		// 取得 userEmail
		UserVO userVO = new UserServiceImpl().getUserByPK(userID);
		String userEmail = userVO.getUserEmail();
		// 取得 userAccount -> 給 redis 使用
		String userAccount = userVO.getUserAccount();

		// 取得新驗證碼
		String verificationCode = getVerificationCode();
		// 重新寄出驗證信
		new Thread(() -> sendMail(userEmail, verificationCode)).start();

		// 將新驗證碼覆蓋先前存在 redis 中的驗證碼資訊
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(15);
		jedis.set("UserAccount:" + userAccount, verificationCode);
		jedis.expire("UserAccount:" + userAccount, 600);
		jedis.close();
		
		// 將成功訊息打回前端
		data.put("status", "success");
		json = gson.toJson(data);
		out.write(json);
		return;
		
	}

	public void sendMail(String to, String verificationCode) {

		try {
			// 設定使用SSL連線至 Gmail smtp Server
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");

			final String myGmail = System.getenv().getOrDefault("NEWVIEW_MAIL_ACCOUNT", "");
			final String myGmail_password = System.getenv().getOrDefault("NEWVIEW_MAIL_PASSWORD", "");
			Session session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(myGmail, myGmail_password);
				}
			});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myGmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// 設定信中的主旨
			message.setSubject("NewView 註冊驗證碼");
			// 設定信中的內容
			message.setText("您好，歡迎您註冊成為 NewView 會員，請輸入以下驗證碼以完成會員註冊，謝謝。\n\n" + "驗證碼：\n[" + verificationCode + "]");

			Transport.send(message);
			System.out.println("傳送成功!");
		} catch (MessagingException e) {
			System.out.println("傳送失敗!");
			e.printStackTrace();
		}
	}

	public String getVerificationCode() {
		String verficationCode = "";
		StringBuilder sb = new StringBuilder();

		// 產生驗證碼
		for (int i = 0; i < 8; i++) {
			switch ((int) (Math.random() * 3)) {
			case 0:
				verficationCode = sb.append(((int) (Math.random() * 10))).toString();
				break;
			case 1:
				verficationCode = sb.append((char) (int) (Math.random() * 26 + 65)).toString();
				break;
			case 2:
				verficationCode += sb.append(((char) (int) (Math.random() * 26 + 97))).toString();
				break;
			}
		}
		// 將驗證碼回傳，回傳後紀錄進 Session Attribute 以供後續驗證
		return verficationCode.substring(0, 8);
	}

}
