package com.skynet.bettbioadv2.dboperator;

import java.util.List;

public class SimpleSqlExecutor extends BaseDbOperator{
	protected List<String> step1_sql_list;
	protected String sqlImportSecUsers;
	protected String sqlImportResearchInstitute;
	protected String sqlImportResearchGroup;
	protected String sqlImportLabratory;
	

	public String getSqlImportLabratory() {
		return sqlImportLabratory;
	}
	public void setSqlImportLabratory(String sqlImportLabratory) {
		this.sqlImportLabratory = sqlImportLabratory;
	}
	public String getSqlImportResearchInstitute() {
		return sqlImportResearchInstitute;
	}
	public void setSqlImportResearchInstitute(String sqlImportResearchInstitute) {
		this.sqlImportResearchInstitute = sqlImportResearchInstitute;
	}
	public String getSqlImportResearchGroup() {
		return sqlImportResearchGroup;
	}
	public void setSqlImportResearchGroup(String sqlImportResearchGroup) {
		this.sqlImportResearchGroup = sqlImportResearchGroup;
	}
	public String getSqlImportSecUsers() {
		return sqlImportSecUsers;
	}
	public void setSqlImportSecUsers(String sqlImportSecUsers) {
		this.sqlImportSecUsers = sqlImportSecUsers;
	}
	public List<String> getStep1_sql_list() {
		return step1_sql_list;
	}
	public void setStep1_sql_list(List<String> step1_sql_list) {
		this.step1_sql_list = step1_sql_list;
	}


	public void createPureV3Data() {
		for(String sql : step1_sql_list){
			executePlainSql(sql);
		}
		
	}
	public void importSecUsers() {
		executePlainSql(getSqlImportSecUsers());
	}
	public void importResearchOrgnizations() {
		executePlainSql(sqlImportResearchInstitute);
		executePlainSql(sqlImportResearchGroup);
	}

}
