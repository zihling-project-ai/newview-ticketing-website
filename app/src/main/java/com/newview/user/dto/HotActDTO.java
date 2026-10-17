package com.newview.user.dto;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import com.newview.act.model.ActVO;
import com.newview.actpic.model.ActPic;

public class HotActDTO {

	List<Integer> actID = new ArrayList<>();

	List<String> actName = new ArrayList<>();

	List<String> actPic = new ArrayList<>();

	public HotActDTO(ActVO actVO) {

		this.actID.add(actVO.getActID());
		this.actName.add(actVO.getActName());

		Set<ActPic> actpics = actVO.getActpics();
		for(ActPic actpic : actpics) {
			byte[] actPicByte = actpic.getActPic();
			if(actPicByte != null) {
				this.actPic.add(Base64.getEncoder().encodeToString(actPicByte));
			}
		}
	}

	public HotActDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "HomeDTO [actID=" + actID + ", actName=" + actName + ", actPic=" + actPic + "]";
	}
}
