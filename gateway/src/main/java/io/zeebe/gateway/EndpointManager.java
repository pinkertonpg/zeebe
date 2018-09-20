/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.gateway;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.zeebe.gateway.api.commands.Topology;
import io.zeebe.gateway.api.events.DeploymentEvent;
import io.zeebe.gateway.api.events.JobEvent;
import io.zeebe.gateway.api.events.MessageEvent;
import io.zeebe.gateway.api.events.WorkflowInstanceEvent;
import io.zeebe.gateway.protocol.GatewayGrpc;
import io.zeebe.gateway.protocol.GatewayOuterClass.CreateJobRequest;
import io.zeebe.gateway.protocol.GatewayOuterClass.CreateJobResponse;
import io.zeebe.gateway.protocol.GatewayOuterClass.CreateWorkflowInstanceRequest;
import io.zeebe.gateway.protocol.GatewayOuterClass.CreateWorkflowInstanceResponse;
import io.zeebe.gateway.protocol.GatewayOuterClass.DeployWorkflowRequest;
import io.zeebe.gateway.protocol.GatewayOuterClass.DeployWorkflowResponse;
import io.zeebe.gateway.protocol.GatewayOuterClass.HealthRequest;
import io.zeebe.gateway.protocol.GatewayOuterClass.HealthResponse;
import io.zeebe.gateway.protocol.GatewayOuterClass.PublishMessageRequest;
import io.zeebe.util.sched.ActorScheduler;
import io.zeebe.util.sched.future.ActorFuture;

public class EndpointManager extends GatewayGrpc.GatewayImplBase {

  private final ResponseMapper responseMapper;
  private final ClusterClient clusterClient;
  private final RequestActor requestActor;

  public EndpointManager(
      final ResponseMapper mapper,
      final ClusterClient clusterClient,
      final ActorScheduler actorScheduler) {
    this.responseMapper = mapper;
    this.clusterClient = clusterClient;
    this.requestActor = new RequestActor();
    actorScheduler.submitActor(requestActor);
  }

  @Override
  public void health(
      final HealthRequest request, final StreamObserver<HealthResponse> responseObserver) {
    final ActorFuture<Topology> responseFuture = clusterClient.sendHealthRequest();
    requestActor.handleResponse(responseFuture, responseMapper::toHealthResponse, responseObserver);
  }

  @Override
  public void deployWorkflow(
      final DeployWorkflowRequest request,
      final io.grpc.stub.StreamObserver<DeployWorkflowResponse> responseObserver) {
    final ActorFuture<DeploymentEvent> responseFuture =
        clusterClient.sendDeployWorkflowRequest(request);
    requestActor.handleResponse(
        responseFuture, responseMapper::toDeployWorkflowResponse, responseObserver);
  }

  @Override
  public void publishMessage(
      PublishMessageRequest request, StreamObserver<Empty> responseObserver) {
    final ActorFuture<MessageEvent> responseFuture = clusterClient.sendPublishMessage(request);
    requestActor.handleResponse(responseFuture, responseMapper::emptyResponse, responseObserver);
  }

  @Override
  public void createJob(
      CreateJobRequest request, StreamObserver<CreateJobResponse> responseObserver) {
    final ActorFuture<JobEvent> responseFuture = clusterClient.sendCreateJob(request);
    requestActor.handleResponse(
        responseFuture, responseMapper::toCreateJobResponse, responseObserver);
  }

  @Override
  public void createWorkflowInstance(
      CreateWorkflowInstanceRequest request,
      StreamObserver<CreateWorkflowInstanceResponse> responseObserver) {
    final ActorFuture<WorkflowInstanceEvent> responseFuture =
        clusterClient.sendCreateWorkflowInstance(request);
    requestActor.handleResponse(
        responseFuture, responseMapper::toCreateWorkflowInstanceResponse, responseObserver);
  }
}