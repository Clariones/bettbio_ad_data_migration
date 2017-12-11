package com.skynet.bettbioadv2.initdata;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class BaseInitDataOperator {
	protected String sql;
	protected List<String> dataList;
	
	
	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}


	public List<String> getDataList() {
		return dataList;
	}


	public void setDataList(List<String> dataList) {
		this.dataList = dataList;
	}


	public int execute(Connection dbConn) {
		CallableStatement st = null;
		int done = 0;
		try {
			 st = dbConn.prepareCall(getSql());
			 for(String dataStr : dataList){
				 prepareParameters(st, dataStr);
				 done += st.executeUpdate();
			 }
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if (st != null){
				try {
					st.close();
				} catch (SQLException e) {
				}
			}
		}
		return done;
	}

	protected String trimValue(Object object) {
		if (object == null){
			return "";
		}
		return String.valueOf(object).trim().replaceAll("^[\\s\\p{Z}]+", "").replaceAll("[\\s\\p{Z}]+$", "");
	}
	protected abstract void prepareParameters(CallableStatement st, String dataStr) throws SQLException;

}
