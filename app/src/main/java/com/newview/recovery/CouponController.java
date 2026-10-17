package com.newview.recovery;
import javax.servlet.http.*;import java.io.IOException;import org.hibernate.Session;import com.newview.util.HibernateUtil;import com.google.gson.Gson;
/** Read-only assigned coupon wallet. Redemption is exclusively inside local checkout. */
public class CouponController extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException{HttpSession h=q.getSession(false);if(h==null||h.getAttribute("userID")==null){r.sendError(401);return;}try(Session s=HibernateUtil.getSessionFactory().openSession()){r.setContentType("application/json;charset=UTF-8");new Gson().toJson(CouponStore.list(s,Integer.parseInt(h.getAttribute("userID").toString())),r.getWriter());}}
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException{r.sendError(405);}
}
