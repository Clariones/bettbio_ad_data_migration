package com.skynet.bettbioadv2.dboperator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.skynet.bettbioadv2.utils.TableIDUtils;

public class ContentOperator extends BaseDbOperator {
	private static final String INTRA_IMAGE = "intra_image";
	protected Set<String> notCustomerCompanyId;
	protected String rootBettbioCompanyId;
	protected String sqlCreateIntraAd;
	protected String sqlCreateCmcAd;
	protected String adminUserId;
	protected List<Map<String, String>> adInPlayList;
	protected int startSeqNo = 0;

	public String getAdminUserId() {
		return adminUserId;
	}

	public void setAdminUserId(String adminUserId) {
		this.adminUserId = adminUserId;
	}

	protected Map<String, String> playListAndPlan;
	
	public String getSqlCreateCmcAd() {
		return sqlCreateCmcAd;
	}

	public void setSqlCreateCmcAd(String sqlCreateCmcAd) {
		this.sqlCreateCmcAd = sqlCreateCmcAd;
	}

	public String getSqlCreateIntraAd() {
		return sqlCreateIntraAd;
	}

	public void setSqlCreateIntraAd(String sqlCreateIntraAd) {
		this.sqlCreateIntraAd = sqlCreateIntraAd;
	}

	public String getRootBettbioCompanyId() {
		return rootBettbioCompanyId;
	}

	public void setRootBettbioCompanyId(String rootBettbioCompanyId) {
		this.rootBettbioCompanyId = rootBettbioCompanyId;
	}

	public Set<String> getNotCustomerCompanyId() {
		return notCustomerCompanyId;
	}

	public void setNotCustomerCompanyId(Set<String> notCustomerCompanyId) {
		this.notCustomerCompanyId = notCustomerCompanyId;
	}

	private List<AdContent> allOldAdContents;
	private TableIDUtils intraIdHelper;
	private TableIDUtils cmcIdHelper;
	private List<Map<String, Object>> allPlayList;
	private TableIDUtils playPlanIdHelper;
	private TableIDUtils aippIdHelper;
	private TableIDUtils cmcLogIdHelper;
	private TableIDUtils cmcAdmachineIdHelper;
	private TableIDUtils playPlanInListIdHelper;
	private TableIDUtils rplIdHelper;

	public void migrateContents() {
		this.adInPlayList = new ArrayList<>();
		migrateContentRepository();

		loadAllAdContents();
		createNewAdContents();

		migratePlayList();
		
		migratePlayRecord();
	}

	
	@Override
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplateObject = new JdbcTemplate(this.dataSource);

		jdbcTemplateObject.setFetchSize(1000);
		jdbcTemplateObject.setQueryTimeout(10000);
	}

	protected final static String sqlMigratePlayRecordStep1 = "insert into bettbio_ad_v2.ad_play_record_data(id,refrigerator,ad_page_id,count_time) select id,refrigerator,ad_page,count_time from bettbio_ad.ad_play_record_data";
	protected final static String sqlMigratePlayRecordStep2 = "update bettbio_ad_v2.ad_play_record_data set ad_page_type=?, ad_page_id=? where ad_page_id=?";
	protected final static String sqlMigratePlayRecordStep3 ="update  bettbio_ad_v2.ad_play_record_data r set customer_company_id = (select c.company from bettbio_ad_v2.customer_marketing_campaign_data c where c.id=r.ad_page_id) where r.ad_page_type='cmc_image'";
	protected final static String sqlMigratePlayRecordStep4 ="update  bettbio_ad_v2.ad_play_record_data r left join bettbio_ad_v2.ad_machine_full_info_view v on r.refrigerator=v.r_id set research_institute_id = v.ri_id, research_group_id=v.rg_id";
	private void migratePlayRecord() {
		if (1==1){
			System.out.println("Skip play record migration for testing");
			return;
		}
		executeUpdateSql("Migrate play record step1: import all data", sqlMigratePlayRecordStep1);
		System.out.println("Migrate play record step2: modify AD content type and id");
		for(AdContent adContent : allOldAdContents){
			List<Object> params = new ArrayList<Object>();
			params.add(adContent.getAdType());
			params.add(adContent.getNewId());
			params.add(adContent.getOldId());
			executeUpdateSql(sqlMigratePlayRecordStep2, params);
			System.out.println("Update play record, update " + adContent.getOldId() + " to " + adContent.getNewId());
		}
		executeUpdateSql("Migrate play record step3: fill company ID", sqlMigratePlayRecordStep3);
		executeUpdateSql("Migrate play record step4: fill organization info", sqlMigratePlayRecordStep4);
	}

	private void migratePlayList() {
		String sql = "insert into bettbio_ad_v2.play_list_data(id,title,belongs_to,version) "
				+ "select id,title,belongs_to,version from bettbio_ad.play_list_data "
				+ "where exists (select id from bettbio_ad_v2.content_repository_data where id=bettbio_ad.play_list_data.belongs_to)";
		this.executeUpdateSql("Migrate Play list", sql);
		

		sql = "select id,title,belongs_to from bettbio_ad_v2.play_list_data";
		allPlayList = queryForList(sql, null, new RowMapper<Map<String, Object>>() {

			@Override
			public Map<String, Object> mapRow(ResultSet rs, int arg1) throws SQLException {
				Map<String, Object> playList = new HashMap<String, Object>();
				playList.put("id", rs.getString("id"));
				playList.put("title", rs.getString("title"));
				playList.put("belongs_to", rs.getString("belongs_to"));
				return playList;
			}
		});

		playPlanIdHelper = new TableIDUtils(this, "play_plan_data", "PP%06d");
		playPlanInListIdHelper = new TableIDUtils(this, "play_plan_in_list_data", "PPIL%06d");
		playPlanIdHelper.reloadMaxId();
		playPlanInListIdHelper.reloadMaxId();
		aippIdHelper = new TableIDUtils(this, "ad_in_play_plan_data", "AIPP%06d");
		aippIdHelper.reloadMaxId();
		createPlayPlans();
		createAdInPlayPlans();
		
		if (allPlayList == null || allPlayList.isEmpty()){
			return;
		}
		
		cmcAdmachineIdHelper = new TableIDUtils(this, "customer_required_ad_machine_data", "CRAM%06d");
		cmcAdmachineIdHelper.reloadMaxId();
		rplIdHelper = new TableIDUtils(this, "refrigerator_play_list_data", "RPL%06d");
		rplIdHelper.reloadMaxId();
		for(Map<String, Object> playList : allPlayList){
			migrateOnePlayList(playList);
		}
	}

	/**算法说明：
	 * 1. 首先，找出广告列表的所有广告和关联的广告机. 没有就什么都不用做了。
	 * 2. 然后过滤掉内部广告
	 * 
	 * @param playList
	 */
	protected final static String sqlQueryAllAdMachineForPlayList = "select refrigerator from bettbio_ad.refrigerator_play_list_data where play_list=?";
	protected final static String sqlQueryAllAdPageForPlayList = "select ad_page from bettbio_ad.ad_in_page_list_data where play_list=?";
	protected final static String sqlCreateCmcRequiredAdMachine = "insert into bettbio_ad_v2.customer_required_ad_machine_data(id,belongs_to,refrigerator,sequence_number,start_time,end_time,last_update_time,status,version) values(?,?,?,?,'00:00:00','23:59:59',now(),'confirmed',1)";
	protected final static String sqlbindAdMachineToList = "insert into bettbio_ad_v2.refrigerator_play_list_data(id, refrigerator,play_list,version) values(?,?,?,1)";
	private void migrateOnePlayList(Map<String, Object> playList) {
		List<Object> params = new ArrayList<Object>();
		String playListId = (String) playList.get("id");
		params.add(playListId);
		List<String> adMachineList = queryObjectList(sqlQueryAllAdMachineForPlayList, params, String.class);
		List<String> adPageList = queryObjectList(sqlQueryAllAdPageForPlayList, params, String.class);
		
		if (adMachineList == null || adMachineList.isEmpty() || adPageList == null || adPageList.isEmpty()){
			return;
		}
		Iterator<String> it = adPageList.iterator();
		while(it.hasNext()){
			String adPageOldId = it.next();
			AdContent adPage = findAdContentByOldId(adPageOldId);
			if (adPage == null || adPage.getAdType().equals(INTRA_IMAGE)){
				it.remove();
				continue;
			}
			
			for(String adMachineId : adMachineList){
				params = new ArrayList<Object>();
				params.add(cmcAdmachineIdHelper.getNextId());
				params.add(adPage.getNewId());
				params.add(adMachineId);
				params.add(getSeqNum(adPage.getOldId(), playListId));
				//System.out.printf("Create CustomerRequiredAdmachine %s, bind %s to %s\n", params.get(0),params.get(2),params.get(1));
				executeUpdateSql(sqlCreateCmcRequiredAdMachine, params);
			}
			System.out.printf("Bind %d AD machines to %s\n", adMachineList.size(),adPage.getNewId());
		}
		
		for(String adMachineId : adMachineList){
			params = new ArrayList<Object>();
			params.add(rplIdHelper.getNextId());
			params.add(adMachineId);
			params.add(playListId);
			
			//System.out.printf("Create CustomerRequiredAdmachine %s, bind %s to %s\n", params.get(0),params.get(2),params.get(1));
			executeUpdateSql(sqlbindAdMachineToList, params);
		}
		System.out.printf("Bind %d AD machines to play list %s\n", adMachineList.size(),playListId);
	}

	private int getSeqNum(String oldId, String playListId) {
		for(Map<String, String> adInPlan : adInPlayList){
			// id,sequence_number,ad_page,play_list
			if (oldId.equals(adInPlan.get("old_id")) && playListId.equals(adInPlan.get("play_list"))){
				return Integer.parseInt(adInPlan.get("sequence_number"));
			}
		}
		return startSeqNo++;
	}

	protected static final String sqlLoadOldAdinPlayList = "select id,sequence_number,ad_page,play_list from bettbio_ad.ad_in_page_list_data where play_list=?";
	private void createAdInPlayPlans() {
		
		for(Map<String, Object> playList : allPlayList){
			// create play plan for it
			List<Object> params = new ArrayList<Object>();
			String playListId = (String) playList.get("id");
			params.add(playListId);
			
			List<Map<String, String>> boundAds = queryForList(sqlLoadOldAdinPlayList, params, new RowMapper<Map<String, String>>(){
				@Override
				public Map<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
					Map<String, String> data = new HashMap<String, String>();
					data.put("id", rs.getString("id"));
					data.put("sequence_number", rs.getString("sequence_number"));
					data.put("old_id", rs.getString("ad_page"));
					data.put("play_list", rs.getString("play_list"));
					return data;
				}});
			
			if (boundAds == null || boundAds.isEmpty()){
				System.out.println("Cannot found any AD for play list " + playListId);
				continue;
			}
			for(Map<String, String> oldAd : boundAds){
				startSeqNo = Math.max(startSeqNo, Integer.parseInt(oldAd.get("sequence_number")));
				adInPlayList.add(oldAd);
				String oldId = oldAd.get("old_id");
				AdContent newAd = findAdContentByOldId(oldId);
				if (newAd == null){
					System.out.println("WARNING: cannot found ad-page " + oldId+" in V3 DB");
					continue;
				}
				if (!newAd.getAdType().equals(INTRA_IMAGE)){
					continue;
				}
				addContentIntoPlayPlan(oldAd, newAd, playListAndPlan.get(playListId));
			}
			startSeqNo++;
		}
	}

	

	private void addContentIntoPlayPlan(Map<String, String> oldAd, AdContent newAd, String playPlanId) {
		String sql = "insert into bettbio_ad_v2.ad_in_play_plan_data(id,sequence_number,play_plan,intra_ad_page,customer_marketing_campaign,version) values(?,?,?,?,NULL,1)";
		List<Object> params = new ArrayList<Object>();
		params.add(aippIdHelper.getNextId());
		params.add(oldAd.get("sequence_number"));
		params.add(playPlanId);
		params.add(newAd.getNewId());
		executeUpdateSql(sql, params);
		System.out.println("Add intra AD " + newAd.getNewId() +" into play-plan " + playPlanId);
	}

	protected static final String sqlCreatePlayPlan = "insert into bettbio_ad_v2.play_plan_data(id,title,description,belongs_to,start_date,end_date,start_time,end_time,version) values(?,?,?,?,now(),'2999-12-31','00:00:00','23:59:59',1)";
	protected static final String sqlAddPlayPlanToList = "insert into bettbio_ad_v2.play_plan_in_list_data(id, play_plan, play_list, version) values(?,?,?,1)";
	private void createPlayPlans() {
		playListAndPlan = new HashMap<String, String>();
		for(Map<String, Object> playList : allPlayList){
			// create play plan for it
			List<Object> params = new ArrayList<Object>();
			String nextPlanId = playPlanIdHelper.getNextId();
			params.add(nextPlanId);
			params.add(playList.get("title")+"_全天播放_内部广告");
			params.add("在播放列表：" + playList.get("title") +" 中全天播放的内部广告");
			params.add(playList.get("belongs_to"));
			
			executeUpdateSql(sqlCreatePlayPlan, params);
			playListAndPlan.put((String) playList.get("id"), nextPlanId);
			
			params = new ArrayList<Object>();
			params.add(playPlanInListIdHelper.getNextId());
			params.add(nextPlanId);
			params.add(playList.get("id"));
			executeUpdateSql(sqlAddPlayPlanToList, params);
			
			System.out.println("play plan " + nextPlanId +" for play list " + playList.get("id") +" was created");
		}
	}

	private void createNewAdContents() {
		Iterator<AdContent> it = allOldAdContents.iterator();
		intraIdHelper = new TableIDUtils(this, "intra_ad_page_data", "IAP%06d");
		cmcIdHelper = new TableIDUtils(this, "customer_marketing_campaign_data", "CMC%06d");
		cmcLogIdHelper = new TableIDUtils(this, "customer_ad_approval_log_data", "CAAL%06d");
		intraIdHelper.reloadMaxId();
		cmcIdHelper.reloadMaxId();
		cmcLogIdHelper.reloadMaxId();
		while (it.hasNext()) {
			AdContent adContent = it.next();
			// if
			// (!adContent.getBelongsTo().equalsIgnoreCase(rootBettbioCompanyId)){
			// it.remove();
			// System.out.println("Ignore AD content" + adContent.getOldId()+"
			// belongs to " + adContent.getBelongsTo());
			// continue;
			// }

			if (getNotCustomerCompanyId().contains(adContent.getCompanyId().toUpperCase())) {
				createNewIntraImageAd(adContent);
			} else {
				createNewCmcAd(adContent);
			}
		}
	}

	protected final static String sqlCreateCmcLog = "insert into bettbio_ad_v2.customer_ad_approval_log_data(id,customer_marketing_campaign,action_operator,action_type,action_message,action_time,version) values(?,?,?,'create_draft',?,now(),1)";
	private void createNewCmcAd(AdContent adContent) {
		adContent.setAdType("cmc_image");
		adContent.setNewId(cmcIdHelper.getNextId());
		List<Object> params = new ArrayList<Object>();
		params.add(adContent.getNewId());
		params.add(adContent.getTitle());
		params.add(adContent.getPlayDuration());
		params.add(adContent.getImageUri());
		params.add(adContent.getBelongsTo());
		params.add(adContent.getCompanyId());
		this.executeUpdateSql(sqlCreateCmcAd, params);
		System.out.println("Migrate " + adContent.getOldId() + " as customer_marketing_compaign " + adContent.getNewId());
		
		params = new ArrayList<Object>();
		params.add(cmcLogIdHelper.getNextId());
		params.add(adContent.getNewId());
		params.add(getAdminUserId());
		params.add("从旧版本的广告"+adContent.getOldId()+"迁移到新版本");
		executeUpdateSql(sqlCreateCmcLog, params);
	}

	private void createNewIntraImageAd(AdContent adContent) {
		adContent.setAdType(INTRA_IMAGE);
		adContent.setNewId(intraIdHelper.getNextId());
		List<Object> params = new ArrayList<Object>();
		params.add(adContent.getNewId());
		params.add(adContent.getTitle());
		params.add(adContent.getPlayDuration());
		params.add(adContent.getImageUri());
		params.add(adContent.getBelongsTo());
		this.executeUpdateSql(sqlCreateIntraAd, params);
		System.out.println("Migrate " + adContent.getOldId() + " as intra_image_ad " + adContent.getNewId());
	}

	private void loadAllAdContents() {
		String sql = "SELECT * FROM bettbio_ad.marketing_campaign_data where exists(select id from bettbio_ad_v2.content_repository_data where id=bettbio_ad.marketing_campaign_data.belongs_to)";
		List<Object> params = null;
		RowMapper<AdContent> rpwMapper = new AdContent.V2RowMapper();
		allOldAdContents = queryForList(sql, params, rpwMapper);
		System.out.println("total has " + allOldAdContents.size() + " AD contents");
	}

	private <T> List<T> queryForList(String sql, List<Object> params, RowMapper<T> rowMapper) {
		Object[] args = null;
		if (params != null) {
			args = params.toArray();
		}
		return this.getJdbcTemplateObject().query(sql, args, rowMapper);

	}

	private void migrateContentRepository() {
		String sql = "insert into bettbio_ad_v2.content_repository_data(id,title,belongs_to,version) "
				+ "select id,title,belongs_to,version from bettbio_ad.content_repository_data "
				+ "where exists (select id from bettbio_ad_v2.bettbio_company_data where id=bettbio_ad.content_repository_data.belongs_to)";
		this.executeUpdateSql("Migrate content repository", sql);
	}

	private AdContent findAdContentByOldId(String oldId) {
		if (allOldAdContents == null){
			return null;
		}
		for(AdContent ad : allOldAdContents){
			if (ad.getOldId().equals(oldId)){
				return ad;
			}
		}
		return null;
	}
}
