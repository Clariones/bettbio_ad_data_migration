package com.skynet.bettbioad.playrecordcleanup;

import java.sql.ResultSet;
import java.sql.SQLException;

public class V2Cleaner extends Cleaner {
	protected String writeRecord(ResultSet rs) throws SQLException {
		String rcdId;
		rcdId = rs.getString(1);
		String rfgId = rs.getString(2);
		String adPageType = rs.getString(3);
		String adPageId = rs.getString(4);
		String rcdTime = rs.getString(5);
		
		outFile.printf("%s,%s,%s,%s,%s\n", rcdId, rfgId, adPageType, adPageId, rcdTime);
		return rcdId;
	}

	protected String getDeleteRecordSql() {
		return "delete from ad_play_record_data where id<=?";
	}

	protected String getLoadRecordSql() {
		String sql = "select id,refrigerator,ad_page_type,ad_page_id,count_time from ad_play_record_data where count_time BETWEEN ? and ? order by count_time asc";
		return sql;
	}
}
