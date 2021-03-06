package com.skynet.bettbioadv2.dboperator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.skynet.bettbioadv2.initdata.BaseInitDataOperator;

public class SimpleSqlExecutor extends BaseDbOperator{
	protected List<String> step1_sql_list;
	protected List<String> step2_sql_list;
	protected String sqlImportSecUsers;
	protected String sqlImportResearchInstitute;
	protected String sqlImportResearchGroup;
	protected String sqlImportLabratory;
	protected String sqlImportAdMachines;
	protected String sqlImportCustomerCompany;
	protected String sqlImportApks;
	protected String sqlImportLabratoryStep2;
	protected List<BaseInitDataOperator> initDataOperators;
	
	
	
	public List<BaseInitDataOperator> getInitDataOperators() {
		return initDataOperators;
	}
	public void setInitDataOperators(List<BaseInitDataOperator> initDataOperators) {
		this.initDataOperators = initDataOperators;
	}
	public List<String> getStep2_sql_list() {
		return step2_sql_list;
	}
	public void setStep2_sql_list(List<String> step2_sql_list) {
		this.step2_sql_list = step2_sql_list;
	}
	public String getSqlImportLabratoryStep2() {
		return sqlImportLabratoryStep2;
	}
	public void setSqlImportLabratoryStep2(String sqlImportLabratoryStep2) {
		this.sqlImportLabratoryStep2 = sqlImportLabratoryStep2;
	}
	public String getSqlImportApks() {
		return sqlImportApks;
	}
	public void setSqlImportApks(String sqlImportApks) {
		this.sqlImportApks = sqlImportApks;
	}
	public String getSqlImportCustomerCompany() {
		return sqlImportCustomerCompany;
	}
	public void setSqlImportCustomerCompany(String sqlImportCustomerCompany) {
		this.sqlImportCustomerCompany = sqlImportCustomerCompany;
	}
	public String getSqlImportAdMachines() {
		return sqlImportAdMachines;
	}
	public void setSqlImportAdMachines(String sqlImportAdMachines) {
		this.sqlImportAdMachines = sqlImportAdMachines;
	}
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
		int i=0;
		System.out.print("Initialize DB...");
		for(String sql : step1_sql_list){
			i += executeUpdateSql(null, sql);
		}
		System.out.println(" " + i + " rows affected.");
		// fill initial data now
		Connection dbConn;
		try {
			dbConn = getJdbcTemplateObject().getDataSource().getConnection();
			int done = 0;
			for(BaseInitDataOperator operator: initDataOperators){
				done += operator.execute(dbConn);
			}
			dbConn.close();
			System.out.println(" " + done + " data was inserted.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		for(String sql : step2_sql_list){
			i += executeUpdateSql(null, sql);
		}
		System.out.println(" " + i + " rows affected.");
	}
	public void importSecUsers() {
		executeUpdateSql("Migrate SecUser", getSqlImportSecUsers());
	}
	public void importResearchOrgnizations() {
		executeUpdateSql("Migrate Research Institute", sqlImportResearchInstitute);
		executeUpdateSql("Migrate Research Group", sqlImportResearchGroup);
		executeUpdateSql("Migrate Laboratory", sqlImportLabratory);
		executeUpdateSql("Migrate AD machines", sqlImportAdMachines);
		executeUpdateSql("Migrate AD machines step2", sqlImportLabratoryStep2);
	}
	
	public void importCustomerCompany() {
		executeUpdateSql("Migrate Customer Company", sqlImportCustomerCompany);
	}
	public void importApks() {
		executeUpdateSql("Migrate AD machine APKs", sqlImportApks);
	}
	
}
