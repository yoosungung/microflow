package io.berry.microflow.service;

import io.berry.microflow.service.impl.FlowManagerServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface FlowManagerService {

	static FlowManagerService create(JsonObject conf) {
		return new FlowManagerServiceImpl(conf);
	}
				
	void getFlowList(Integer nodeId,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
			
	void createFlow(Integer nodeId, 
			JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void getFlowXml(Integer flowId,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void createFlowXml(Integer flowId, 
			JsonObject body,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
	void setCurrentFlowXml(Integer flowId,
			OperationRequest context, 
		    Handler<AsyncResult<OperationResponse>> resultHandler);
	
}
