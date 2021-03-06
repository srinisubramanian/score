/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
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

package io.cloudslang.engine.queue.services;

import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.cloudslang.score.events.EventConstants.BRANCH_ID;
import static io.cloudslang.score.events.EventConstants.EXECUTION_ID;
import static io.cloudslang.score.events.EventConstants.SPLIT_ID;

/**
 * User:
 * Date: 03/08/2014
 */
public class ScoreEventFactoryImpl implements ScoreEventFactory {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    public ScoreEvent createFinishedEvent(Execution execution) {
        String eventType = EventConstants.SCORE_FINISHED_EVENT;
        Serializable eventData = createFinishedEventData(execution);
        SystemContext systemContext = execution.getSystemContext();
        return new ScoreEvent(eventType, systemContext.getLanguageName(), eventData, systemContext.getMetaData());
    }

    public ScoreEvent createFailureEvent(Execution execution) {
        String eventType = EventConstants.SCORE_FAILURE_EVENT;
        Serializable eventData = createFailureEventData(execution);
        SystemContext systemContext = execution.getSystemContext();
        return new ScoreEvent(eventType, systemContext.getLanguageName(), eventData, systemContext.getMetaData());
    }

    @Override
    public ScoreEvent createFinishedBranchEvent(Execution execution) {
        String eventType = EventConstants.SCORE_FINISHED_BRANCH_EVENT;
        Serializable eventData = createFinishedEventData(execution);
        SystemContext systemContext = execution.getSystemContext();
        return new ScoreEvent(eventType, systemContext.getLanguageName(), eventData, systemContext.getMetaData());
    }

    public ScoreEvent createFailedBranchEvent(Execution execution) {
        String eventType = EventConstants.SCORE_BRANCH_FAILURE_EVENT;
        Serializable eventData = createBranchFailureEventData(execution);
        SystemContext systemContext = execution.getSystemContext();
        return new ScoreEvent(eventType, systemContext.getLanguageName(), eventData, systemContext.getMetaData());
    }

    public ScoreEvent createNoWorkerEvent(Execution execution, Long pauseId) {
        String eventType = EventConstants.SCORE_NO_WORKER_FAILURE_EVENT;
        Serializable eventData = createNoWorkerFailureEventData(execution, pauseId);
        SystemContext systemContext = execution.getSystemContext();
        return new ScoreEvent(eventType, systemContext.getLanguageName(), eventData, systemContext.getMetaData());
    }

    private Serializable createFinishedEventData(Execution execution) {
        Map<String, Serializable> eventData = new HashMap<>();
        ExecutionRuntimeServices runtimeServices = execution.getSystemContext();

        //if nothing went wrong flow is completed
        if (runtimeServices.getFlowTerminationType() == null) {
            runtimeServices.setFlowTerminationType(ExecutionStatus.COMPLETED);
        }

        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, runtimeServices);
        eventData.put(EventConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
        eventData.put(EventConstants.EXECUTION_CONTEXT, (Serializable) execution.getContexts());
        eventData.put(EventConstants.SCORE_RUN_ENV, execution.getContexts().get("runEnv"));
        eventData.put(EventConstants.IS_BRANCH, !StringUtils.isEmpty(runtimeServices.getBranchId()));

        eventData.put(EXECUTION_ID, runtimeServices.getExecutionId());
        eventData.put(SPLIT_ID, runtimeServices.getSplitId());
        eventData.put(BRANCH_ID, runtimeServices.getBranchId());

        return (Serializable) eventData;
    }

    private Serializable createBranchFailureEventData(Execution execution) {
        Map<String, Serializable> eventData = new HashMap<>();
        SystemContext systemContext = execution.getSystemContext();
        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, systemContext);
        eventData.put(EventConstants.SCORE_RUN_ENV, execution.getContexts().get("runEnv"));
        eventData.put(EventConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());

        eventData.put(EXECUTION_ID, systemContext.getExecutionId());
        eventData.put(SPLIT_ID, systemContext.getSplitId());
        eventData.put(BRANCH_ID, systemContext.getBranchId());

        return (Serializable) eventData;
    }

    private Serializable createFailureEventData(Execution execution) {
        Map<String, Serializable> eventData = new HashMap<>();
        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, execution.getSystemContext());
        eventData.put(EventConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
        eventData.put(EventConstants.SCORE_RUN_ENV, execution.getContexts().get("runEnv"));
        eventData.put(BRANCH_ID, execution.getSystemContext().getBranchId());
        eventData.put(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID, execution.getRunningExecutionPlanId());
        return (Serializable) eventData;
    }

    private Serializable createNoWorkerFailureEventData(Execution execution, Long pauseId) {
        String flowUuid = extractFlowUuid(execution.getRunningExecutionPlanId());

        Map<String, Serializable> eventData = new HashMap<>();
        eventData.put(ExecutionParametersConsts.SYSTEM_CONTEXT, execution.getSystemContext());
        eventData.put(EventConstants.EXECUTION_ID_CONTEXT, execution.getExecutionId());
        eventData.put(BRANCH_ID, execution.getSystemContext().getBranchId());
        eventData.put(EventConstants.FLOW_UUID, flowUuid);
        eventData.put(EventConstants.PAUSE_ID, pauseId);
        return (Serializable) eventData;
    }

    private String extractFlowUuid(Long runningExecutionPlanId) {
        return runningExecutionPlanService.getFlowUuidByRunningExecutionPlanId(runningExecutionPlanId);
    }

}
