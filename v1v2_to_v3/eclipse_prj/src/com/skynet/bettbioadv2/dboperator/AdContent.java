package com.skynet.bettbioadv2.dboperator;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class AdContent {
	public static class V2RowMapper implements RowMapper<AdContent> {

		@Override
		public AdContent mapRow(ResultSet rs, int row) throws SQLException {
			AdContent result = new AdContent();
			result.setOldId(rs.getString("id"));
			result.setTitle(rs.getString("title"));
			result.setPlayDuration(rs.getInt("play_duration"));
			result.setImageUri(rs.getString("image"));
			result.setPageLink(rs.getString("page_link"));
			result.setBelongsTo(rs.getString("belongs_to"));
			result.setCompanyId(rs.getString("company"));
			return result;
		}

	}
	protected String oldId;
	protected String newId;
	protected String adType;
	protected String companyId;
	protected String imageUri;
	protected String title;
	protected String description;
	protected int playDuration;
	protected String belongsTo;
	protected String pageLink;
	public String getOldId() {
		return oldId;
	}
	public void setOldId(String oldId) {
		this.oldId = oldId;
	}
	public String getNewId() {
		return newId;
	}
	public void setNewId(String newId) {
		this.newId = newId;
	}
	public String getAdType() {
		return adType;
	}
	public void setAdType(String adType) {
		this.adType = adType;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getImageUri() {
		return imageUri;
	}
	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getPlayDuration() {
		return playDuration;
	}
	public void setPlayDuration(int playDuration) {
		this.playDuration = playDuration;
	}
	public String getBelongsTo() {
		return belongsTo;
	}
	public void setBelongsTo(String belongsTo) {
		this.belongsTo = belongsTo;
	}
	public String getPageLink() {
		return pageLink;
	}
	public void setPageLink(String pageLink) {
		this.pageLink = pageLink;
	}
	
	
}
