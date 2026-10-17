package com.newview.report.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.newview.post.model.PostVO;
import com.newview.user.model.UserVO;

@Entity
@Table(name = "report")
//配合 HQL
//@NamedQuery(name = "")
public class ReportVO {
	@Expose
	@Id
	@Column(name = "reportID")
	private Integer reportID;

	@Expose
	@Column(name = "reportContent")
	private String reportContent;

	@Expose
	@Column(name = "reportStatus", columnDefinition = "TINYINT")
	private Integer reportStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", referencedColumnName = "userID")
	private UserVO userVO;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postID", referencedColumnName = "postID")
	private PostVO postVO;

	public ReportVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReportVO(Integer reportID, String reportContent, Integer reportStatus, UserVO userVO, PostVO postVO) {
		super();
		this.reportID = reportID;
		this.reportContent = reportContent;
		this.reportStatus = reportStatus;
		this.userVO = userVO;
		this.postVO = postVO;
	}

	public Integer getReportID() {
		return reportID;
	}

	public void setReportID(Integer reportID) {
		this.reportID = reportID;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	public Integer getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Integer reportStatus) {
		this.reportStatus = reportStatus;
	}

	public UserVO getUserVO() {
		return userVO;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

	public PostVO getPostVO() {
		return postVO;
	}

	public void setPostVO(PostVO postVO) {
		this.postVO = postVO;
	}

	@Override
	public String toString() {
		return "ReportVO [reportID=" + reportID + ", reportContent=" + reportContent + ", reportStatus=" + reportStatus
				+ "]";
	}

	
	

}
