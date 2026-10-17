package com.newview.post.controller;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import com.google.gson.*;
import org.hibernate.*;
import org.hibernate.type.StandardBasicTypes;
import com.newview.util.HibernateUtil;
import com.newview.recovery.ImageSupport;

/** Restored write endpoint: author checks and image updates share one transaction. */
public class DoPostServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String action = req.getParameter("action");
            if (!Set.of("add", "update").contains(action == null ? "" : action)) throw new IllegalArgumentException("無效的操作");
            String title = Objects.toString(req.getParameter("postHeader"), "").trim();
            String content = Objects.toString(req.getParameter("postContent"), "").trim();
            if (title.isEmpty() || title.length() > 100 || content.isEmpty() || content.length() > 10000 || title.matches("(?s).*[<>].*") || content.matches("(?s).*[<>].*"))
                throw new IllegalArgumentException("請輸入標題與純文字內容");
            int category = Integer.parseInt(req.getParameter("postCategoryID"));
            int user = Integer.parseInt((String) req.getSession().getAttribute("userID"));
            List<byte[]> uploads = new ArrayList<>();
            if (req.getContentType() != null && req.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/")) {
                for (Part part : req.getParts()) if ("images".equals(part.getName()) && part.getSubmittedFileName() != null && part.getSize() > 0) {
                    if (part.getSize() > ImageSupport.MAX_BYTES) throw new IllegalArgumentException("每張圖片限 2 MB");
                    uploads.add(ImageSupport.normalize(part.getInputStream().readNBytes(ImageSupport.MAX_BYTES + 1)));
                }
            }
            // The old diagnostic page sends an empty img_base64 parameter; do not create empty blobs.
            String encoded = req.getParameter("img_base64");
            if (encoded != null && !encoded.isBlank()) for (String value : encoded.split(",")) uploads.add(ImageSupport.normalize(Base64.getDecoder().decode(value)));
            Set<Integer> keep = new HashSet<>();
            String keepValue = req.getParameter("keepImageIds");
            if (keepValue != null) {
                JsonArray array = JsonParser.parseString(keepValue).getAsJsonArray();
                for (JsonElement id : array) keep.add(id.getAsInt());
            }
            boolean replace = "true".equals(req.getParameter("replaceImages")) || encoded != null;
            if (uploads.size() + keep.size() > 3) throw new IllegalArgumentException("每篇最多 3 張圖片");
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    if (session.createNativeQuery("SELECT postCategoryID FROM postcategory WHERE postCategoryID=:c").setParameter("c", category).uniqueResult() == null)
                        throw new IllegalArgumentException("文章分類不存在");
                    int id;
                    if ("update".equals(action)) {
                        id = Integer.parseInt(req.getParameter("postID"));
                        Object owner = session.createNativeQuery("SELECT userID FROM post WHERE postID=:p FOR UPDATE").setParameter("p", id).uniqueResult();
                        if (owner == null) { tx.rollback(); res.sendError(404); return; }
                        if (!owner.toString().equals(String.valueOf(user))) { tx.rollback(); res.sendError(403); return; }
                        List<?> existing = session.createNativeQuery("SELECT postPicID FROM postpic WHERE postID=:p").setParameter("p", id).list();
                        Set<Integer> existingIDs = new HashSet<>();
                        for (Object number : existing) existingIDs.add(((Number)number).intValue());
                        if (!existingIDs.containsAll(keep)) throw new IllegalArgumentException("圖片不屬於這篇文章");
                        if (!replace && existing.size() + uploads.size() > 3) throw new IllegalArgumentException("每篇最多 3 張圖片");
                        session.createNativeQuery("UPDATE post SET postHeader=:h,postContent=:t,postCategoryID=:c,lastEditedTime=CURRENT_TIMESTAMP(6) WHERE postID=:p")
                            .setParameter("h",title).setParameter("t",content).setParameter("c",category).setParameter("p",id).executeUpdate();
                        if (replace) for (int picture : existingIDs) if (!keep.contains(picture))
                            session.createNativeQuery("DELETE FROM postpic WHERE postPicID=:i AND postID=:p").setParameter("i",picture).setParameter("p",id).executeUpdate();
                    } else {
                        if (!keep.isEmpty()) throw new IllegalArgumentException("新增文章不能引用其他圖片");
                        session.createNativeQuery("INSERT INTO post(postHeader,postContent,postCategoryID,userID,postStatus,postDateTime,lastEditedTime,likeCount,disLikeCount) VALUES(:h,:t,:c,:u,1,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6),0,0)")
                            .setParameter("h",title).setParameter("t",content).setParameter("c",category).setParameter("u",user).executeUpdate();
                        id = ((Number)session.createNativeQuery("SELECT LAST_INSERT_ID()").uniqueResult()).intValue();
                    }
                    for (byte[] image : uploads) session.createNativeQuery("INSERT INTO postpic(postID,postpic) VALUES(:p,:b)").setParameter("p",id).setParameter("b",image,StandardBasicTypes.BINARY).executeUpdate();
                    tx.commit();
                    res.getWriter().write(new Gson().toJson(Map.of("status","success","postID",id,"location","forum_postdetail.html?postID="+id)));
                } catch (RuntimeException | IOException e) { if(tx.isActive()) tx.rollback(); throw e; }
            }
        } catch (IllegalArgumentException | IllegalStateException | JsonParseException e) {
            res.setStatus(400); res.getWriter().write(new Gson().toJson(Map.of("error","圖片或文章資料無效：請檢查分類、大小與格式")));
        }
    }
}
