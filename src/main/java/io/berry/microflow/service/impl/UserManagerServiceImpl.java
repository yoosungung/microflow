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

	private final JDBCAuth fauth;
	private final String fpasswd_salt;
	
	public UserManagerServiceImpl(JsonObject conf) {
		super(conf);
	}

	public void setAuth(JDBCAuth auth) {
		fauth = auth;
	}

	private String getPasswdSalt() {
		if(fpasswd_salt == null) {
			fpasswd_salt = fauth.generatedSalt();
		}
		return fpasswd_salt;
	}

	private String getPasswdHsah(String raw_passwd, String passwd_salt) {
		return fauth.computedHash(raw_passwd, passwd_salt);
	}

	@Override
	public void getUserList(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {
		
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getUserList", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select username, password from user where username like ? ";
			params.add(username);

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getUserList", res.cause());
					resultHandler.handle(Future.failedFuture(ar.cause()));
					return;
				}
				if(res.result().getNumRows() == 0) {					
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray())));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(res.result().getRows()))));
				}
			});
		});
	}

	@Override
	public void createUser(String username, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("createUser", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			final Sting passwd_salt = getPasswordSalt();
			final Sting passwd_hash = getPasswordHash(body.getString("password"), passwd_salt);
			
			JsonArray params = new JsonArray();
			String sql = "insert into user (username, password, password_salt) values (?, ?, ?) ";
			params.add(body.getString("username"))
				.add(passwd_hash)
				.add(passwd_salt);
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("createUser", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(body)));				
				}
			});
		});
	}

	@Override
	public void getUser(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getUser", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select username, password from user where username = ? ";
			params.add(username);

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getUser", res.cause());
					resultHandler.handle(Future.failedFuture(ar.cause()));
					return;
				}
				if(res.result().getNumRows() == 0) {					
					resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("404")));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(res.result().getRows()))));
				}
			});
		});

	}

	@Override
	public void updateUser(String username, JsonObject body, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("updateUser", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			final String passwd_salt = getPasswordSalt();
			final Sting passwd_hash = getPasswordHash(body.getString("password"), passwd_salt);
			
			JsonArray params = new JsonArray();
			String sql = "update user set password = ?, password_salt = ?  where username = ? ";
			params.add(passwd_hash)
				.add(passwd_salt)
				.add(username);
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("updateNode", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					if(rs.result().getUpdated() == 0) {					
						resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("404")));
					} else {
						resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(body)));
					}				
				}
			});
		});	

	}

	@Override
	public void deleteUser(String username, OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("deletUser", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			String sql;
			JsonArray params = new JsonArray()add(username);
			
			sql = "delete from roles_perms where role in (select role from user_roles where username = ? ";
			conn.updateWithParams(sql, params, rs -> {
				if(rs.failed()) {
					LOGGER.error("deletUser - roles_perms", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				}
			});

			sql = "delete from user_roles where username = ? ";
			conn.updateWithParams(sql, params, rs -> {
				if(rs.failed()) {
					LOGGER.error("deletUser - user_roles", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				}
			});

			sql = "delete from user where username = ? ";
			conn.updateWithParams(sql, params, rs -> {
				if(rs.failed()) {
					LOGGER.error("deletUser", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					if(rs.result().getUpdated() == 0) {					
						resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("404")));
					} else {
						resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonObject().put("delete","OK"))));
					}				
				}
			});

			conn.close(done -> {
				if (done.failed()) {
					throw new RuntimeException(done.cause());
				}
			});

		});	

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
