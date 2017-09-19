package com.skynet.bettbioadv2.dboperator;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDbOperator {
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
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
	
	
}
