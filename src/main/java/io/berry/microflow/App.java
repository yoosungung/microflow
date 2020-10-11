package io.berry.microflow;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.sql.SQLClient;

public class App {
	
	private static Vertx fvertx;
	private static JDBCAuth fauth;
	
	public static Map<String, SQLClient> dbcpools = new HashMap<>();

	public static JDBCAuth getJdbcAuth(String dbname) {
		if(fauth == null) {
			JDBCClient jdbcClient = (JDBCClient)dbcpools.get(dbname);
			fauth = JDBCAuth.create(fvertx, jdbcClient);
		}
		return fauth;
	}


	public static void main(String[] args) {
		VertxOptions vxOptions = new VertxOptions().setBlockedThreadCheckInterval(200000000);
		fvertx = Vertx.vertx(vxOptions);
	    fvertx.deployVerticle(MainVerticle.class.getName());
	}

}
