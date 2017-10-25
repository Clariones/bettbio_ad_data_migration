package com.skynet.bettbioadv2.dboperator;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

public abstract class BaseDbOperator {
	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplateObject;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplateObject = new JdbcTemplate(this.dataSource);

		jdbcTemplateObject.setFetchSize(1000);
		jdbcTemplateObject.setQueryTimeout(10000);
	}

	public JdbcTemplate getJdbcTemplateObject() {
		return jdbcTemplateObject;
	}

	public void setJdbcTemplateObject(JdbcTemplate jdbcTemplateObject) {
		this.jdbcTemplateObject = jdbcTemplateObject;
	}

	protected void executePlainSql(String sql) {
		getJdbcTemplateObject().execute(sql);
	}

	protected int executeUpdateSql(String description, String sql) {
		// System.out.print(sql);
		Object[] args = null;
		if (description != null) {
			System.out.print(description + "...");
		}
		try {
			int n = getJdbcTemplateObject().update(sql, args);
			if (description != null) {
				System.out.println(" " + n + " rows done");
			}
			return n;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	protected void executeUpdateSql(String sql, List<? extends Object> params) {
		Object[] paramArray = params.toArray();
		getJdbcTemplateObject().update(sql, paramArray);

	}

	public <T> T queryForOneResult(String sql, List<Object> params, Class<T> requiredType) {
		if (params != null) {
			return getJdbcTemplateObject().queryForObject(sql, params.toArray(), requiredType);
		}
		return getJdbcTemplateObject().queryForObject(sql, null, requiredType);
	}

	public <T> List<T> queryObjectList(String sql, List<Object> params, Class<T> requiredType) {

		Object[] args = null;
		if (params != null) {
			args = params.toArray();
		}
		return getJdbcTemplateObject().queryForList(sql, args, requiredType);
	}

}
