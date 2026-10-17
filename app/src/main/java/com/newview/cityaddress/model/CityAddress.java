package com.newview.cityaddress.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.newview.act.model.ActVO;

@Entity
@Table(name = "city")
public class CityAddress {
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cityAddressID", updatable = false)
	private Integer cityAddressID;
	@Expose
	@Column(name = "cityName")
	private String cityName;
	@Expose
	@OneToMany(mappedBy = "city", cascade=CascadeType.ALL)	
	private Set<ActVO> acts;
	
	public Integer getActAdressID() {
		return cityAddressID;
	}
	public void setActAdressID(Integer actAdressID) {
		this.cityAddressID = actAdressID;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public Set<ActVO> getActs() {
		return acts;
	}
	public void setActs(Set<ActVO> acts) {
		this.acts = acts;
	}
	@Override
	public String toString() {
		return "cityadress [cityAdressID=" + cityAddressID + ", cityName=" + cityName + "]";
	}
	public CityAddress() {
		super();
		
	}
	public CityAddress(Integer actAddressID, String cityName, Set<ActVO> acts) {
		super();
		this.cityAddressID = actAddressID;
		this.cityName = cityName;
		this.acts = acts;
	}
	public Integer getCityAddressID() {
		return cityAddressID;
	}
	public void setCityAddressID(Integer cityAddressID) {
		this.cityAddressID = cityAddressID;
	}
	
	
	
	
}
