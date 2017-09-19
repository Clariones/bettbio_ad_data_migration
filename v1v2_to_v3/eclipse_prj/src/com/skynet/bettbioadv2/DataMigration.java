package com.skynet.bettbioadv2;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.skynet.bettbioadv2.dboperator.BaseDbOperator;
import com.skynet.bettbioadv2.dboperator.SimpleSqlExecutor;

public class DataMigration {
	protected SimpleSqlExecutor dbOperator;
	
	public BaseDbOperator getDbOperator() {
		return dbOperator;
	}

	public void setDbOperator(SimpleSqlExecutor dbOperator) {
		this.dbOperator = dbOperator;
	}

	public static void main(String[] args) {
		Map<String, String> envs = System.getenv();
    	for(Entry<String, String> entry : envs.entrySet()){
    		System.out.println(entry.getKey()+"="+ entry.getValue());
    	}
        System.out.println( "Starting POC http server....." );
        String configFileName = "config/spring_main.xml";
        if (args != null && args.length > 0){
        	configFileName = args[0].trim();
        }
        ApplicationContext context = new FileSystemXmlApplicationContext(configFileName);
        
        DataMigration dm = (DataMigration) context.getBean("data_migration_executor");
        dm.startWork();
	}

	private void startWork() {
		dbOperator.createPureV3Data();
		dbOperator.importSecUsers();
		dbOperator.importResearchOrgnizations();
	}

}
