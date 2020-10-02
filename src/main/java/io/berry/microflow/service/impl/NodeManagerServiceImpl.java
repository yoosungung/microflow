package io.berry.microflow.service.impl;

import io.berry.microflow.service.NodeManagerService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class NodeManagerServiceImpl extends JdbcManagerServiceImpl implements NodeManagerService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManagerServiceImpl.class);
	
	public NodeManagerServiceImpl(JsonObject conf) {
		super(conf);
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
			String sql = "create table if not exists mf_node ( "
					+ "id int primary key, "
					+ "name varchar(255), "
					+ "node_type varchar(128), "
					+ "folder int, "
					+ "description varchar(1024), "
					+ "create_date datetime )";
			conn.execute(sql, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});
		});
	}
	
	@Override
	public void getNodeList(String folder, String node_type, String owner, String name, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getNodeList", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select id, name, node_type, folder, description, create_date "
					+ "from mf_node "
					+ "where 1=1 ";
			if(folder != null) {
				sql = sql + "and folder = ? ";
				params.add(folder);
			}
			if(node_type != null) {
				sql = sql + "and node_type = ? ";
				params.add(node_type);
			}
			if(owner != null) {
				sql = sql + "and owner = ? ";
				params.add(owner);
			}
			if(name != null) {
				sql = sql + "and name like ? ";
				params.add(name);
			}

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getNodeList", res.cause());
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
	public void createNode(JsonObject body, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("createNode", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "insert into mf_node (id, name, node_type, folder, description, create_date) values (?, ?, ?, ?, ?, ?) ";
			params.add(body.getInteger("id"))
				.add(body.getString("name"))
				.add(body.getString("node_type"))
				.add(body.getInteger("folder"))
				.add(body.getString("description"))
				.add(body.getString("create_date"));
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("createNode", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(body)));				
				}
			});
		});
	}

	@Override
	public void getNode(Integer nodeId, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getNode", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select id, name, node_type, folder, description, create_date "
					+ "from mf_node "
					+ "where id = ? ";
			params.add(nodeId);

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getNode", res.cause());
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
	public void updateNode(Integer nodeId, JsonObject body, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("updateNode", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "update mf_node set id = ?, name = ?, node_type = ?, folder = ?, description = ?, create_date = ? where id = ? ";
			params.add(body.getString("id"))
				.add(body.getString("name"))
				.add(body.getString("node_type"))
				.add(body.getInteger("folder"))
				.add(body.getString("description"))
				.add(body.getString("create_date"))
				.add(nodeId);
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
	public void deletNode(Integer nodeId, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("deletNode", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "delete from mf_node where id = ? ";
			params.add(nodeId);
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("deletNode", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					if(rs.result().getUpdated() == 0) {					
						resultHandler.handle(Future.succeededFuture(new OperationResponse().setStatusMessage("404")));
					} else {
						resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonObject().put("delete","OK"))));
					}				
				}
			});
		});	
	}

}
