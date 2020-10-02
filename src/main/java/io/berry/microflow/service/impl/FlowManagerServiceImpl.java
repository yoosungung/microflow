package io.berry.microflow.service.impl;

import io.berry.microflow.service.FlowManagerService;
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

public class FlowManagerServiceImpl extends JdbcManagerServiceImpl implements FlowManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManagerServiceImpl.class);

	public FlowManagerServiceImpl(JsonObject conf) {
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
			String sql_flow = "create table if not exists mf_flow ( "
					+ "id int primary key, "
					+ "node_id int, "
					+ "is_current int, "
					+ "create_date datetime )";
			conn.execute(sql_flow, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});
			String sql_flow_index = "create index node_idx on mf_flow (node_id)";
			conn.execute(sql_flow_index, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});
			String sql_xml = "create table if not exists mf_flow_xml ( "
					+ "id int primary key, "
					+ "content_xml varchar(4000) ) ";
			conn.execute(sql_xml, res -> {
				conn.close();
				if(res.failed()) {
					LOGGER.error("initServiceTables", res.cause());
				}
			});

		});
	}

	@Override
	public void getFlowList(Integer nodeId,
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getFlowList", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select id, node_id, is_current, create_date "
					+ "from mf_flow "
					+ "where node_id = ? ";
			params.add(nodeId);

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getFlowList", res.cause());
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
	public void createFlow(Integer nodeId, JsonObject body, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("createFlow", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "insert into mf_flow (id, node_id, is_current, create_date) values (?, ?, ?, ?) ";
			params.add(body.getInteger("id"))
				.add(body.getInteger("node_id"))
				.add(body.getInteger("is_current"))
				.add(body.getString("create_date"));
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("createFlow", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(body)));				
				}
			});
		});		
	}

	@Override
	public void getFlowXml(Integer flowId, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("getFlowXml", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			JsonArray params = new JsonArray();
			String sql = "select id, content_xml "
					+ "from mf_flow_xml "
					+ "where id = ? ";
			params.add(flowId);

			conn.queryWithParams(sql, params, res -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(res.failed()) {
					LOGGER.error("getFlowXml", res.cause());
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
	public void createFlowXml(Integer flowId, JsonObject body, 
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("createFlowXml", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "insert into mf_flow_xml (id, content_xml) values (?, ?) ";
			params.add(body.getInteger("id"))
				.add(body.getString("content_xml"));
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("createFlowXml", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(body)));				
				}
			});
		});
	}

	@Override
	public void setCurrentFlowXml(Integer flowId,
			OperationRequest context,
			Handler<AsyncResult<OperationResponse>> resultHandler) 
	{
		this.getConnection(ar -> {
			if(ar.failed()) {
				LOGGER.error("setCurrentFlowXml", ar.cause());
				resultHandler.handle(Future.failedFuture(ar.cause()));
				return;
			}
			final SQLConnection conn = ar.result();
			
			JsonArray params = new JsonArray();
			String sql = "update mf_flow set is_current = (case when id = ? then 1 else 0 end) where node_id = (select node_id from mf_flow where id = ?) ";
			params.add(flowId)
				.add(flowId);
			conn.updateWithParams(sql, params, rs -> {
				conn.close(done -> {
					   if (done.failed()) {
						   throw new RuntimeException(done.cause());
					   }
					});
				if(rs.failed()) {
					LOGGER.error("setCurrentFlowXml", rs.cause());
					resultHandler.handle(Future.failedFuture(rs.cause()));
				} else {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonObject().put("is_current","OK"))));				
				}
			});
		});
	}

}
