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

import com.google.gson.Gson;
import com.newview.user.jedis.JedisPoolUtil;
import com.newview.user.service.UserService;
import com.newview.user.service.UserServiceImpl;

import redis.clients.jedis.Jedis;

@WebServlet("/RetrievePassword")
public class RetrievePasswordController extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		Gson gson = new Gson();
		HashMap<String, String> data = new HashMap<>();
		String json = null;

		// 取得前端傳遞 email 參數
		String email = req.getParameter("email");

		// 透過 email 查詢此使用者是否存在
		// 不存在回傳 false，程式中斷，回傳 status -> failed 告知前端
		UserService userSvc = new UserServiceImpl();
		if (!userSvc.checkUserAccountByEmail(email)) {
			data.put("status", "failed");
			json = gson.toJson(data);
			out.write(json);
			return;
		}

		// 寄驗證信給前端傳來之 email + 驗證信 + 驗證碼 + 新密碼 (亂數8碼)
		String verificationCode = getVerificationCode();
		String newPassword = getVerificationCode();
		new Thread(() -> sendMail(email, verificationCode, newPassword)).start();
		
		// 將驗證碼 (1) 、新密碼 (2) 存入 redis，Key 為 userEmail
		Jedis jedis = JedisPoolUtil.getJedisPool().getResource();
		jedis.select(15);
		jedis.set("UserEmail_Verify:" + email, verificationCode);
		jedis.set("UserEmail_NewPassword:" + email, newPassword);
		jedis.close();
		
		// 回傳 status -> success
		data.put("status", "success");
		json = gson.toJson(data);
		out.write(json);

	}

	public void sendMail(String to, String verificationCode, String newPassword) {

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
			message.setSubject("NewView 忘記密碼驗證信");
			// 設定信中的內容
			message.setText(
					"NewView 會員您好，已將密碼重設如下：" + newPassword + "\n\n請輸入以下驗證碼以重新修改密碼，謝謝。\n\n" + "驗證碼：" + verificationCode);

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
