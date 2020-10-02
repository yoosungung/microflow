package io.berry.microflow.service;

import io.berry.microflow.service.impl.NodeManagerServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface NodeManagerService {

	static NodeManagerService create(JsonObject conf) {
		return new NodeManagerServiceImpl(conf);
	}
				
	void getNodeList(String folder, String node_type, String owner, String name,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
			
	void createNode(JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void getNode(Integer nodeId,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void updateNode(Integer nodeId,
			JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void deletNode(Integer nodeId,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
}
