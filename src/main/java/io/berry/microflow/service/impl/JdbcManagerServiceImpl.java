package io.berry.microflow.service.impl;

import io.berry.microflow.App;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

public abstract class JdbcManagerServiceImpl {

	private JsonObject config;
	private SQLClient dbclient = null;

	public JdbcManagerServiceImpl(JsonObject conf) {
		this.config = conf;
	}

	protected SQLClient getSQLClient(String jdbc_name) {
		if(dbclient == null) {
			dbclient = App.dbcpools.get(jdbc_name);
			initServiceTables();
		}
		return dbclient;
	}
	
	protected SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
		SQLClient dbclient = getSQLClient(config.getString("jdbc"));
		if(dbclient != null) {
			dbclient.getConnection(handler);
		}
		return dbclient;
	}

	protected abstract void initServiceTables();

}