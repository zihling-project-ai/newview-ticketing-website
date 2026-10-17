package com.newview.actcategory.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.newview.act.model.ActVO;

@Entity
@Table(name = "actCategory")
// 配合 TestHQLWithParameter.java
@NamedQuery(name = "getAllActCategory", query = "from ActCategory where actCategoryID > :actCategoryID order by actCategoryID desc")
public class ActCategory {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "actCategoryID", updatable = false)
	private Integer actCategoryID;
	@Expose
	@Column(name = "actCategoryName")
	private String actCategoryName;
	@Expose
	@OneToMany(mappedBy = "actCategory", cascade=CascadeType.ALL  )
	
	private Set<ActVO> acts;
	
	
	
	@Override
	public String toString() {
		return "ActCategory [actCategoryID=" + actCategoryID + ", actCategoryName=" + actCategoryName +  "]";
	}


	public ActCategory(Integer actCategoryID, String actCategoryName, Set<ActVO> acts) {
		super();
		this.actCategoryID = actCategoryID;
		this.actCategoryName = actCategoryName;
		this.acts = acts;
	}



	public Integer getActCategoryID() {
		return actCategoryID;
	}



	public void setActCategoryID(Integer actCategoryID) {
		this.actCategoryID = actCategoryID;
	}



	public String getActCategoryName() {
		return actCategoryName;
	}



	public void setActCategoryName(String actCategoryName) {
		this.actCategoryName = actCategoryName;
	}



	public Set<ActVO> getActs() {
		return acts;
	}



	public void setActs(Set<ActVO> acts) {
		this.acts = acts;
	}



	public ActCategory() {
       
    }
	
}
