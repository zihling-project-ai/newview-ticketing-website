package com.newview.recovery;
import javax.servlet.*;import javax.servlet.http.*;import java.io.*;import java.util.*;
import com.newview.util.HibernateUtil;import org.hibernate.Session;
public class SocialFilter implements Filter {
 private static final Set<String> READ=Set.of("/GetPostServlet","/GetPostDetailServlet","/GetPostCategoryServlet","/GetMemberSession","/VerifyPostMember","/RecoverySession");
 private static final Set<String> WRITE=Set.of("/DoPostServlet","/DeletePostServlet","/DoLikeDislikeServlet","/DoMessageServlet");
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException {
 HttpServletRequest q=(HttpServletRequest)request;HttpServletResponse r=(HttpServletResponse)response;q.setCharacterEncoding("UTF-8");
 MemberSessions.validate(q);
 if(Set.of("/ActivityList","/ActivityDetail","/ActivityImage").contains(q.getServletPath())){r.setHeader("X-Content-Type-Options","nosniff");r.setHeader("Cache-Control","no-store");chain.doFilter(q,r);return;}
 String p=q.getServletPath(),action;
 try{if("/DoPostServlet".equals(p)&&q.getContentType()!=null&&q.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("multipart/"))q.getParts();action=q.getParameter("action");}catch(IllegalStateException ex){r.sendError(413);return;}catch(ServletException ex){r.sendError(400);return;}
 r.setHeader("X-Content-Type-Options","nosniff");r.setHeader("X-Frame-Options","DENY");r.setHeader("Cache-Control","no-store");
 boolean asset=p.matches("/(?:css|js|images|img|fonts|vendor|assets)/.*")&&!p.matches("(?i).*\\.(?:jsp|java|class|properties|xml)$");
 if(p.equals("/")||p.equals("/recovery.html")||Set.of("/sign-in.html","/sign-up.html","/member.html","/my-profile.html","/change-password.html","/forgot-password.html","/myOrder.html","/my-favourite.html","/my-coupons.html","/not-found.html","/unavailable.html","/activities.html","/activity-detail.html","/product.html","/product-detail.html","/checkout_cart.html","/checkout_thankupage.html","/seats.html","/tickets.html","/ticket.html","/redeem-ticket.html").contains(p)||p.startsWith("/forum_")&&p.endsWith(".html")||asset){chain.doFilter(q,r);return;}
 if(!(READ.contains(p)||WRITE.contains(p)||p.equals("/DoMessageServlet")||Set.of("/SignIn","/SignUp","/GetUserInfo","/UpdateUserInfo","/UpdateUserPassword","/RetrievePassword","/VerifyPassword","/MemberPage","/MemberOrders","/MemberFavorites","/MemberActivities","/MemberActivityImage","/likeChange","/Cart","/LocalCheckout","/Seats","/CancelLocalOrder","/Tickets","/TicketQR","/RedeemLocalTicket","/PreviewTicket","/DecodeTicketQR","/TicketReview","/ActivityReviews","/MemberCoupons").contains(p))){r.sendError(404);return;}
 try {for(String key:List.of("postID","postMessageID","postCategoryID")){String value=q.getParameter(key);if(value!=null&&Integer.parseInt(value)<=0){r.sendError(400);return;}}}catch(NumberFormatException e){r.sendError(400);return;}
 if(Set.of("/SignUp","/GetUserInfo","/UpdateUserInfo","/UpdateUserPassword","/RetrievePassword","/VerifyPassword","/MemberPage","/MemberOrders","/MemberFavorites","/MemberActivities","/MemberActivityImage","/likeChange","/Cart","/LocalCheckout","/Seats","/CancelLocalOrder","/Tickets","/TicketQR","/RedeemLocalTicket","/PreviewTicket","/DecodeTicketQR","/TicketReview","/ActivityReviews","/MemberCoupons").contains(p)){String origin=q.getHeader("Origin");if(origin!=null&&!origin.equals("http://"+q.getHeader("Host"))){r.sendError(403);return;}chain.doFilter(q,r);return;}
 boolean read=READ.contains(p)&&q.getMethod().equals("GET")||p.equals("/DoMessageServlet")&&"GetAll".equals(action)&&q.getMethod().equals("GET");
 if(read){chain.doFilter(q,r);return;}
 if(READ.contains(p)&&!p.equals("/RecoverySession")&&q.getMethod().equals("POST")){chain.doFilter(q,r);return;}
 if(!q.getMethod().equals("POST")){r.sendError(405);return;}
 if(p.equals("/SignIn")) {String origin=q.getHeader("Origin");if(origin!=null&&!origin.equals("http://"+q.getHeader("Host"))){r.sendError(403);return;}chain.doFilter(q,r);return;}
 HttpSession session=q.getSession(false);if(session==null||session.getAttribute("userID")==null){r.sendError(401);return;}
 String csrf=q.getHeader("X-CSRF-Token");if(csrf==null)csrf=q.getParameter("csrf");if(csrf==null||!csrf.equals(session.getAttribute("csrf"))){r.sendError(403);return;}
 try {
 if(p.equals("/DeletePostServlet")||p.equals("/DoPostServlet")&&"update".equals(action)) {if(!owner("post","postID",q.getParameter("postID"),session)){r.sendError(403);return;}}
 if(p.equals("/DoMessageServlet")&&Set.of("Update","Delete").contains(action==null?"":action)){if(!owner("postmessage","postMessageID",q.getParameter("postMessageID"),session)){r.sendError(403);return;}}
 if(p.equals("/DoPostServlet")){for(String key:List.of("postHeader","postContent")){String value=q.getParameter(key);if(value==null||value.isBlank()||key.equals("postHeader")&&value.length()>100){r.sendError(400);return;}}}
 if(p.equals("/DoMessageServlet")&&Set.of("AddMsg","Update").contains(action==null?"":action)){String value=q.getParameter("AddMsg".equals(action)?"MesContent":"mesContent");if(value==null||value.isBlank()){r.sendError(400);return;}}
 for(String key:List.of("postHeader","postContent","MesContent","mesContent")){String v=q.getParameter(key);if(v!=null&&(v.contains("<")||v.contains(">")||v.length()>10000)){r.sendError(400,"Use plain text, maximum 10000 characters");return;}}
 chain.doFilter(q,r);
 }catch(NumberFormatException e){r.sendError(400);}
 }
 private boolean owner(String table,String key,String value,HttpSession hs){int id=Integer.parseInt(value);try(Session s=HibernateUtil.getSessionFactory().openSession()){Object found=s.createNativeQuery("SELECT userID FROM "+table+" WHERE "+key+"=:id").setParameter("id",id).uniqueResult();return found!=null&&found.toString().equals(hs.getAttribute("userID"));}}
}
