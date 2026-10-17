package com.newview.user.dto;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import com.newview.actpic.model.ActPic;
import com.newview.mylike.model.MyLikeVO;

public class MyLikeActDTO {

	List<String> actName = new ArrayList<>();

	List<String> actIntroduce = new ArrayList<>();

	List<String> actPic = new ArrayList<>();

	public MyLikeActDTO(MyLikeVO myLikeVO) {

		this.actName.add(myLikeVO.getAct().getActName());
		this.actIntroduce.add(myLikeVO.getAct().getActIntroduce());
		Set<ActPic> actPics = myLikeVO.getAct().getActpics();

		for (ActPic actPic : actPics) {
			byte[] actPicByte = actPic.getActPic();
			if (actPicByte != null) {
				this.actPic.add(Base64.getEncoder().encodeToString(actPicByte));
			}
		}
	}

	public MyLikeActDTO() {
	}

	@Override
	public String toString() {
		return "MyLikeActDTO [actName=" + actName + ", actIntroduce=" + actIntroduce + ", actPic=" + actPic + "]";
	}

}
