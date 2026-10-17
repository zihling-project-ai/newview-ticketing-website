package com.newview.actpic.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.newview.act.model.ActVO;
import com.newview.actcategory.model.ActCategory;

@Entity
@Table(name = "actpic")
// 配合 TestHQLWithParameter.java
@NamedQuery(name = "getAllActPics", query = "from ActPic where ActPicID > :ActPicID order by ActPicID desc")
public class ActPic {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "actPicID", updatable = false)
	private Integer actPicID;

	

	@Column(name = "actPic", columnDefinition = "LONGBLOB")
	@JsonIgnore
	private byte[] actPic;


	@ManyToOne
	@JoinColumn(name = "actID",referencedColumnName = "actID")
	@JsonIgnore
	private ActVO act;


	@Override
	public String toString() {
		return "ActPic [actPicID=" + actPicID + ", actPic=" + Arrays.toString(actPic) + "]";
	}

	public ActPic(Integer actPicID, byte[] actPic, ActVO act) {
		super();
		this.actPicID = actPicID;
		this.actPic = actPic;
		this.act = act;
	}

	public ActPic() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getActPicID() {
		return actPicID;
	}

	public ActVO getActID() {
		return act;
	}

	public byte[] getActPic() {
		return actPic;
	}

	public void setActPicID(Integer actPicID) {
		this.actPicID = actPicID;
	}

	public void setActID(ActVO actID) {
		this.act = actID;
	}

	public void setActPic(byte[] actPic) {
		this.actPic = actPic;
	}


	


}
