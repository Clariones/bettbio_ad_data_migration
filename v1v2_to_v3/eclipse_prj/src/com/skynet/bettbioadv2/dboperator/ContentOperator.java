package com.skynet.bettbioadv2.dboperator;

public class ContentOperator extends BaseDbOperator {

	public void migrateContents() {
		migrateContentRepository();
		
	}

	private void migrateContentRepository() {
		String sql = "insert into bettbio_ad_v2.content_repository_data(id,title,belongs_to,version) "
				+ "select id,title,belongs_to,version from bettbio_ad.content_repository_data "
				+ "where exists (select id from bettbio_ad_v2.bettbio_company_data where id=bettbio_ad.content_repository_data.belongs_to)";
		this.executeUpdateSql("Migrate content repository", sql);
		
	}

}
