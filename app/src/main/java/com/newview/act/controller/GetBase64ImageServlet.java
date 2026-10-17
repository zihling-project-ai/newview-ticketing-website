package com.newview.act.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.newview.actpic.model.ActPic;

@WebServlet("/GetImageFromDB")
public class GetBase64ImageServlet extends HttpServlet {
	
    private SessionFactory sessionFactory;

    @Override
    public void init() throws ServletException {
        super.init();
        sessionFactory = new Configuration().configure("/hibernate.cfg.xml").buildSessionFactory();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    	    throws ServletException, IOException {
    	    String actIDParam = request.getParameter("actID");

    	    if (actIDParam != null && !actIDParam.isEmpty()) {
    	        Integer actID = Integer.parseInt(actIDParam);

    	        Session session = sessionFactory.openSession();

    	        try {
    	            session.beginTransaction();
    	            String hql = "FROM ActPic ap WHERE ap.act.actID = :actID";
    	            List<ActPic> actPics = session.createQuery(hql, ActPic.class)
    	                .setParameter("actID", actID)
    	                .list();

    	            
    	            ActPic selectedActPic = null;
    	            for (ActPic actPic : actPics) {
    	                if (actPic.getActPic() != null) {
    	                    selectedActPic = actPic;
    	                    break;
    	                }
    	            }

    	            if (selectedActPic != null && selectedActPic.getActPic() != null) {
    	                byte[] picBytes = selectedActPic.getActPic();
    	                String base64Image = Base64.getEncoder().encodeToString(picBytes);
    	                
    	                response.setContentType("image/jpeg"); 
    	                response.getOutputStream().write(Base64.getDecoder().decode(base64Image));
//    	                System.out.println("Image found for actID: " + actID);
    	            } else {
//    	                System.out.println("No image found for actID: " + actID);
    	                response.setContentType("text/plain");
    	                response.getWriter().write("Image not found");
    	            }

    	            session.getTransaction().commit();
    	        } catch (Exception e) {
    	            if (session.getTransaction() != null) {
    	                session.getTransaction().rollback();
    	            }
    	            e.printStackTrace();
    	        } finally {
    	            session.close();
    	        }
    	    } else {
    	        response.setContentType("text/plain");
    	        response.getWriter().write("Invalid request");
    	    }
    	}

}
