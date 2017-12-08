package com.skynet.bettbioadv2.initdata;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AllStringInitOperator extends BaseInitDataOperator {
	Pattern ptnSpliter = Pattern.compile("(?<!(\\\\)),");
	protected void prepareParameters(CallableStatement st, String dataStr) throws SQLException {
		String[] params = ptnSpliter.split(dataStr);
		for(int i=0;i<params.length;i++){
			st.setString(i+1, trimValue(params[i]));
		}
	}
}
