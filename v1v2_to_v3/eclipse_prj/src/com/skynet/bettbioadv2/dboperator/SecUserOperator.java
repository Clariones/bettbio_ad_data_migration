package com.skynet.bettbioadv2.dboperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.skynet.bettbioadv2.utils.TableIDUtils;

public class SecUserOperator extends BaseDbOperator {
	protected String adminUserId;
	protected String adminUserAppSql;
	protected String adminUserAccessSql;
	protected List<String> beforeSetupAdminSqls;
	protected List<String> afterSetupAdminSqls;
	protected List<String> adminUserAppDataList;
	protected List<String> adminUserObjectAccessList;
	protected String customerUserAppSql;
	protected String customerUserDataViewSql;
	
	
	public String getCustomerUserAppSql() {
		return customerUserAppSql;
	}

	public void setCustomerUserAppSql(String customerUserAppSql) {
		this.customerUserAppSql = customerUserAppSql;
	}

	public String getCustomerUserDataViewSql() {
		return customerUserDataViewSql;
	}

	public void setCustomerUserDataViewSql(String customerUserDataViewSql) {
		this.customerUserDataViewSql = customerUserDataViewSql;
	}

	public String getAdminUserAppSql() {
		return adminUserAppSql;
	}

	public void setAdminUserAppSql(String adminUserAppSql) {
		this.adminUserAppSql = adminUserAppSql;
	}

	public String getAdminUserAccessSql() {
		return adminUserAccessSql;
	}

	public void setAdminUserAccessSql(String adminUserAccessSql) {
		this.adminUserAccessSql = adminUserAccessSql;
	}

	public List<String> getBeforeSetupAdminSqls() {
		return beforeSetupAdminSqls;
	}

	public void setBeforeSetupAdminSqls(List<String> beforeSetupAdminSqls) {
		this.beforeSetupAdminSqls = beforeSetupAdminSqls;
	}

	public List<String> getAfterSetupAdminSqls() {
		return afterSetupAdminSqls;
	}

	public void setAfterSetupAdminSqls(List<String> afterSetupAdminSqls) {
		this.afterSetupAdminSqls = afterSetupAdminSqls;
	}

	public List<String> getAdminUserAppDataList() {
		return adminUserAppDataList;
	}

	public void setAdminUserAppDataList(List<String> adminUserAppDataList) {
		this.adminUserAppDataList = adminUserAppDataList;
	}

	public List<String> getAdminUserObjectAccessList() {
		return adminUserObjectAccessList;
	}

	public void setAdminUserObjectAccessList(List<String> adminUserObjectAccessList) {
		this.adminUserObjectAccessList = adminUserObjectAccessList;
	}

	public String getAdminUserId() {
		return adminUserId;
	}

	public void setAdminUserId(String adminUserId) {
		this.adminUserId = adminUserId;
	}

	public void setupUsersView() {
		executeBeforeSetupAdminSqls();
		createAdminUserApps();
		createAdminUserAccess();
		executeAfterSetupAdminSqls();
		
		createCustomerUsersView();
	}

	private void createCustomerUsersView() {
		String sql = "select id from bettbio_ad_v2.sec_user_data where id != '" + getAdminUserId() +"'";
		List<String> customerUserIds = this.getJdbcTemplateObject().queryForList(sql, String.class);
		TableIDUtils ccdvIdHelper = new TableIDUtils(this, "customer_company_data_view_data", "CCDV%06d");
		TableIDUtils uaIdHelper = new TableIDUtils(this, "user_app_data", "UA%06d");
		
		ccdvIdHelper.reloadMaxId();
		uaIdHelper.reloadMaxId();
		
		for(String userId : customerUserIds){
			
			sql = "select distinct CCDV.arg1 from bettbio_ad.customer_company_data_view_data CCDV left join bettbio_ad.user_app_data UA on CCDV.id=UA.object_id where UA.sec_user=?";
			List<String> customerRelatedCompanyes = this.getJdbcTemplateObject().queryForList(sql, new Object[]{userId}, String.class);
			if (customerRelatedCompanyes == null || customerRelatedCompanyes.isEmpty()){
				continue;
			}
			for(String cmpyId : customerRelatedCompanyes){
				List<String> params = new ArrayList<String>();
				String ccdvId = ccdvIdHelper.getNextId();
				params.add(ccdvId);
				params.add(cmpyId);
				this.executeUpdateSql(customerUserDataViewSql, params);
				
				params.clear();
				params.add(uaIdHelper.getNextId());
				params.add(userId);
				params.add(ccdvId);
				this.executeUpdateSql(customerUserAppSql, params);
				
				System.out.println("Create user app for " + userId + " of company " + cmpyId);
			}
		}
	}

	private void createAdminUserAccess() {
		for(int i=0;i<this.getAdminUserObjectAccessList().size();i++){
			ArrayList<String> params = new ArrayList<String>(16);
			params.addAll(Arrays.asList(getAdminUserObjectAccessList().get(i).split(",")));
			for(int n = params.size(); n < 13; n++){
				params.add("null");
			}
			System.out.println("Create admin user permission: " + params);
			executeUpdateSql(this.getAdminUserAccessSql(), params);
		}
		
	}

	private void createAdminUserApps() {
		for(int i=0;i<getAdminUserAppDataList().size();i++){
			ArrayList<String> params = new ArrayList<String>(8);
			params.addAll(Arrays.asList(getAdminUserAppDataList().get(i).split(",")));
			params.add(2, adminUserId);
			params.add(String.format("%04d", i+1));
			System.out.println("Create admin user app: " + params);
			executeUpdateSql(this.getAdminUserAppSql(), params);
		}
		
	}

	private void executeAfterSetupAdminSqls() {
		int n = 0;
		for(String sql : getAfterSetupAdminSqls()){
			n += executeUpdateSql(null, sql);
		}
		System.out.println("Execute sqls needed after create admin user view. " + n + " rows affected.");
	}

	private void executeBeforeSetupAdminSqls() {
		int n = 0;
		for(String sql : getBeforeSetupAdminSqls()){
			n += executeUpdateSql(null, sql);
		}
		System.out.println("Execute sqls needed before create admin user view. " + n + " rows affected.");
	}
	
	
}
