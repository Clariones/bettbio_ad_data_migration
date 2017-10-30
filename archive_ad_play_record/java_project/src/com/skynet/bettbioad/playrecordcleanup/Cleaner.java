package com.skynet.bettbioad.playrecordcleanup;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Cleaner {

	private File outputBaseFolder;
	private Connection dbConn;
	private PrintStream outFile;

	public void init(Properties props) throws Exception {
		this.outputBaseFolder = new File(props.getProperty("archive.folder"));
		this.dbConn = createDbConnection(props);

	}

	private Connection createDbConnection(Properties props) throws Exception {
		Class.forName(props.getProperty("data.jdbc.class"));
		String dbUrl = props.getProperty("data.jdbc.url");
		String userName = props.getProperty("data.jdbc.username");
		String password = props.getProperty("data.jdbc.password");
		return DriverManager.getConnection(dbUrl, userName, password);

	}

	public void doCleanup() throws Exception {
		Date startDate = findEarlestDate();
		Date endDate = calcClosingDate();
		cleanUpBetweenDays(startDate, endDate);
	}

	private static final SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private void cleanUpBetweenDays(Date startDate, Date endDate) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		while (true) {
			Date todayEnd = toDayEnd(calendar.getTime());
			if (todayEnd.after(endDate)) {
				break;
			}
			Date fromDate = toDayBegin(calendar.getTime());
			Date toDate = todayEnd;
			System.out.println("Now clean records between " + fmtDate.format(fromDate) + " and " + fmtDate.format(toDate));
			cleanUpRecords(fromDate, toDate);
			calendar.add(Calendar.DATE, 1);
		}
	}

	private void cleanUpRecords(Date fromDate, Date toDate) throws Exception {
		String sql = "select id,refrigerator,ad_page,count_time from ad_play_record_data where count_time BETWEEN ? and ? order by count_time asc";
		PreparedStatement st = dbConn.prepareStatement(sql);
		st.setTimestamp(1, new Timestamp(fromDate.getTime()));
		st.setTimestamp(2, new Timestamp(toDate.getTime()));
		ResultSet rs = st.executeQuery();
		
		PreparedStatement stDelete = dbConn.prepareStatement("delete from ad_play_record_data where id<=?");
		int added = 0;
		createArchiveFile(fromDate);
		String rcdId = null;
		while(rs.next()){
			rcdId = rs.getString(1);
			String rfgId = rs.getString(2);
			String adPageId = rs.getString(3);
			String rcdTime = rs.getString(4);
			
			outFile.printf("%s, %s,%s,%s\n", rcdId, rfgId, adPageId, rcdTime);
//			stDelete.setString(1,rcdId);
//			stDelete.execute();
//			stDelete.addBatch();
//			added++;
//			
//			if (added > 500){
//				stDelete.executeBatch();
//				added = 0;
//			}
		}
		outFile.close();
//		if (added > 0){
//			stDelete.executeBatch();
//			added = 0;
//		}
		stDelete.setString(1,rcdId);
		stDelete.execute();
		stDelete.close();
		rs.close();
		st.close();
	}

	private static final SimpleDateFormat fmtDay = new SimpleDateFormat("yyyyMMdd");
	private void createArchiveFile(Date fromDate) throws Exception {
		String name = String.format("AD_RCD_%s.txt", fmtDay.format(fromDate));
		File output = new File(outputBaseFolder, name);
		System.out.println("Save to " + output.getCanonicalPath());
		outFile = FileUtils.createFileForWrite(output);
	}

	private Date toDayBegin(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	private Date calcClosingDate() {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -2);
		Date closeDate = calendar.getTime();
		return toDayEnd(closeDate);
	}

	private Date toDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}

	private Date findEarlestDate() throws SQLException {
		return queryDate("select min(count_time) from ad_play_record_data");
	}

	private Date queryDate(String sql) throws SQLException {
		Statement st = dbConn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		Date result = null;
		if (rs.next()) {
			result = rs.getTimestamp(1);
		}
		rs.close();
		st.close();
		return result;
	}

	public void close() {
		try {
			dbConn.close();
		} catch (Exception e) {
		}
	}

}
