package com.newview.post.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.newview.post.model.PostDAO;
import com.newview.post.model.PostDAOImpl;
import com.newview.post.model.PostVO;
import com.newview.postmessage.model.PostMessageVO;
import com.newview.postpic.model.PostPicVO;

@WebServlet("/GetPostServlet")
@MultipartConfig
public class GetPostServlet extends HttpServlet {
	private static final long serialVersionUID = -1195497717454804472L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setContentType("application/json; charset=UTF-8");
		String action = req.getParameter("action");

		// ********************************************ListALL******************************************//

		if ("getAll".equals(action)) {
			PostDAO dao = new PostDAOImpl();
			List<PostVO> posts = dao.getAll();
            posts.sort(java.util.Comparator.comparing(PostVO::getPostID).reversed());

			List<Map<String, Object>> postList = new ArrayList<>();
			for (PostVO post : posts) {
				Map<String, Object> postData = new HashMap<>();

				postData.put("postID", post.getPostID());
                postData.put("userID", post.getUserVO().getUserID());
				postData.put("postHeader", post.getPostHeader());
				postData.put("postContent", post.getPostContent());
				postData.put("likeCount", post.getLikeCount());
				postData.put("dislikeCount", post.getDisLikeCount());
				postData.put("postDateTime", post.getPostDateTime().toString());
				postData.put("lastEditedTime", post.getLastEditedTime().toString());
				postData.put("postStatus", post.getPostStatus());
				postData.put("userName", post.getUserVO().getUserName());
				postData.put("postCategoryID", post.getPostCategoryVO().getPostCategoryID());
				postData.put("postCategoryName", post.getPostCategoryVO().getPostCategoryName());

				// 獲取postMessageID
				List<Integer> postMessageIDs = new ArrayList<>();
				for (PostMessageVO postMessage : post.getPostMessageVOs()) {
					postMessageIDs.add(postMessage.getPostMessageID());
				}

				postData.put("postMessageIDs", postMessageIDs);

				List<String> base64Images = new ArrayList<>();
				for (PostPicVO pic : post.getPostPicVOs()) {
					if (pic.getPostPic() != null && pic.getPostPic().length != 0) {
						byte[] picBytes = pic.getPostPic();
						String base64Image = com.newview.recovery.ImageSupport.dataUrl(picBytes);
						base64Images.add(base64Image);
					}
				}
				postData.put("postPic", base64Images);

				postList.add(postData);
			}

			Gson gson = new Gson();
			String jsonData = gson.toJson(postList);
			res.getWriter().write(jsonData);
		}

		
		
		
		// *****************************************ListByLike***********************************************//

		if ("getbylike".equals(action)) {
			PostDAO dao = new PostDAOImpl();
			List<PostVO> posts = dao.getAll();
            posts.sort(java.util.Comparator.comparing(PostVO::getPostID).reversed());

			// (老師教學)根據讚數討厭數排序
//			Comparator<PostVO> comp = new Comparator<PostVO>() {
//				@Override
//				public int compare(PostVO post1, PostVO post2) {
//					int totalLikes1 = post1.getLikeCount() + post1.getDisLikeCount();
//					int totalLikes2 = post2.getLikeCount() + post2.getDisLikeCount();
//					return totalLikes2 - totalLikes1;
//				}
//			};
//            Collections.sort(posts, comp);

			Collections.sort(posts, (post1, post2) -> {
				int totalLikes1 = post1.getLikeCount() + post1.getDisLikeCount();
				int totalLikes2 = post2.getLikeCount() + post2.getDisLikeCount();
				return totalLikes2 - totalLikes1;
			});

			// Empty feeds are valid.

			List<Map<String, Object>> postList = new ArrayList<>();

			for (int i = 0; i < Math.min(2, posts.size()); i++) {
				PostVO post = posts.get(i);

				Map<String, Object> postData = new HashMap<>();

				postData.put("postID", post.getPostID());
                postData.put("userID", post.getUserVO().getUserID());
				postData.put("postHeader", post.getPostHeader());
				postData.put("postContent", post.getPostContent());
				postData.put("likeCount", post.getLikeCount());
				postData.put("dislikeCount", post.getDisLikeCount());
				postData.put("postDateTime", post.getPostDateTime().toString());
				postData.put("lastEditedTime", post.getLastEditedTime().toString());
				postData.put("postStatus", post.getPostStatus());
				postData.put("userName", post.getUserVO().getUserName());
				postData.put("postCategoryID", post.getPostCategoryVO().getPostCategoryID());
				postData.put("postCategoryName", post.getPostCategoryVO().getPostCategoryName());

				// 獲取postMessageID[]
				List<Integer> postMessageIDs = new ArrayList<>();
				for (PostMessageVO postMessage : post.getPostMessageVOs()) {
					postMessageIDs.add(postMessage.getPostMessageID());
				}
				postData.put("postMessageIDs", postMessageIDs);

				// 獲取postPic[]
				List<String> base64Images = new ArrayList<>();
				for (PostPicVO pic : post.getPostPicVOs()) {
					if (pic.getPostPic() != null && pic.getPostPic().length != 0) {
						byte[] picBytes = pic.getPostPic();
						String base64Image = com.newview.recovery.ImageSupport.dataUrl(picBytes);
						base64Images.add(base64Image);
					}
				}
				postData.put("postPic", base64Images);

				postList.add(postData);

			}

			Gson gson = new Gson();
			String jsonData = gson.toJson(postList);
			res.getWriter().write(jsonData);

		}

	}

}
