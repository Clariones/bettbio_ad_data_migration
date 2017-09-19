package com.skynet.bettbioadv2.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skynet.bettbioadv2.dboperator.BaseDbOperator;

public class TableIDUtils {

	private BaseDbOperator dbOperator;
	private String tableName;
	private String idFormat;
	private int curId;

	public TableIDUtils(BaseDbOperator dbOperator, String tableName, String idFormat) {
		this.dbOperator = dbOperator;
		this.tableName = "bettbio_ad_v2." + tableName;
		this.idFormat = idFormat;
	}

	public void reloadMaxId() {
		String sql = getQueryMaxIdSql();
		String maxID = dbOperator.queryForOneResult(sql, null, String.class);
		if (maxID == null || maxID.isEmpty()){
			curId = 0;
			return;
		}
		
		Matcher m = Pattern.compile("(\\d+)").matcher(maxID);
		if (!m.find()){
			curId = 0;
			return;
		}
		
		curId = Integer.parseInt(m.group(1), 10);
	}

	private String getQueryMaxIdSql() {
		return "select max(id) from " + tableName;
	}

	public String getNextId() {
		return String.format(idFormat, ++curId);
	}

}
