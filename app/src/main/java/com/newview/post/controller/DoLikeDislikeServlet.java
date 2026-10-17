package com.newview.post.controller;
import javax.servlet.http.*;import java.io.*;import java.util.*;import com.google.gson.Gson;
import org.hibernate.*;import com.newview.util.HibernateUtil;
public class DoLikeDislikeServlet extends HttpServlet {
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException {
 r.setContentType("application/json;charset=UTF-8");String action=q.getParameter("action");if(!Set.of("like","dislike").contains(action==null?"":action)){r.sendError(400);return;}
 int postID;try{postID=Integer.parseInt(q.getParameter("postID"));}catch(Exception e){r.sendError(400);return;}
 int userID=Integer.parseInt((String)q.getSession().getAttribute("userID"));
 try(Session s=HibernateUtil.getSessionFactory().openSession()) {Transaction tx=s.beginTransaction();try {
 Object row=s.createNativeQuery("SELECT postID FROM post WHERE postID=:p FOR UPDATE").setParameter("p",postID).uniqueResult();if(row==null){tx.rollback();r.sendError(404);return;}
 s.createNativeQuery("DELETE FROM likes WHERE postID=:p AND userID=:u").setParameter("p",postID).setParameter("u",userID).executeUpdate();
 s.createNativeQuery("INSERT INTO likes(postID,userID,likeOrNot) VALUES(:p,:u,:v)").setParameter("p",postID).setParameter("u",userID).setParameter("v",action.equals("like")?1:0).executeUpdate();
 s.createNativeQuery("UPDATE post SET likeCount=(SELECT COUNT(*) FROM likes WHERE postID=:p AND likeOrNot=1),disLikeCount=(SELECT COUNT(*) FROM likes WHERE postID=:p AND likeOrNot=0) WHERE postID=:p").setParameter("p",postID).executeUpdate();
 Object[] counts=(Object[])s.createNativeQuery("SELECT likeCount,disLikeCount FROM post WHERE postID=:p").setParameter("p",postID).uniqueResult();tx.commit();r.getWriter().write(new Gson().toJson(Map.of("likeCount",counts[0],"dislikeCount",counts[1])));
 }catch(RuntimeException ex){if(tx.isActive())tx.rollback();throw ex;}}
 }
}