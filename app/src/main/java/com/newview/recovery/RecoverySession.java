package com.newview.recovery;
import javax.servlet.http.*; import java.io.*; import java.util.*; import com.google.gson.Gson;
public class RecoverySession extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException {r.setContentType("application/json;charset=UTF-8");HttpSession s=q.getSession();if(s.getAttribute("csrf")==null)s.setAttribute("csrf",UUID.randomUUID().toString());Map<String,Object> m=new HashMap<>();m.put("csrf",s.getAttribute("csrf"));m.put("userID",s.getAttribute("userID"));m.put("account",s.getAttribute("account"));r.getWriter().write(new Gson().toJson(m));}
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException{q.getSession().invalidate();doGet(q,r);}
}