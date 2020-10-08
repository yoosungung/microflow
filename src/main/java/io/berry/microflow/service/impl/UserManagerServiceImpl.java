package io.berry.microflow.service.impl;

import io.berry.microflow.service.UserManagerService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class UserManagerServiceImpl extends JdbcManagerServiceImpl implements UserManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerServiceImpl.class);
	
	public UserManagerServiceImpl(JsonObject conf) {
		super(conf);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getUserList(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createUser(String username, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getUser(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUser(String username, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUser(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initServiceTables() {
		LOGGER.info("initServiceTables");
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("initServiceTables", ar.cause());
				return;
			}
			final SQLConnection conn = ar.result();
			String sql = "create table if not exists user ( "
					+ "username varchar(255) not null, "
					+ "passwd varchar(255) not null, "
					+ "password_salt carchar(255) not null "
					+ ") ";
			conn.execute(sql, res -> {
				if(res.failed()) {
					conn.close();
					LOGGER.error("initServiceTables", res.cause());
				} else
					res.up
					
					if(res.updateRow() > 0) {
					sql = "alter table user add constraint pk_username primary key (username) ";
					conn.execute(sql, alter_res -> {
						conn.close();
						if(alter_res.failed()) {
							LOGGER.error("initServiceTables", res.cause());
						}
					});
				}
			});
			sql = "create table if not exists user_roles ( "
					+ "username varchar(255) not null, "
					+ "role varchar(255 not null "
					+ ") ";
			conn.execute(sql, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});
			sql = "create table if not exists user_roles ( "
					+ "role varchar(255) not null, "
					+ "rerm varchar(255 not null "
					+ ") ";
			conn.execute(sql, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});
		});
	}

}
