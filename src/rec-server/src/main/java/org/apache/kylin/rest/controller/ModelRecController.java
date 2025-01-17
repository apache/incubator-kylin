/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.rest.controller;

import static org.apache.kylin.common.constant.HttpConstant.HTTP_VND_APACHE_KYLIN_JSON;
import static org.apache.kylin.common.exception.ServerErrorCode.FAILED_CREATE_MODEL;

import org.apache.kylin.common.exception.KylinException;
import org.apache.kylin.metadata.model.exception.LookupTableException;
import org.apache.kylin.rec.AbstractContext;
import org.apache.kylin.rest.aspect.WaitForSyncBeforeRPC;
import org.apache.kylin.rest.request.FavoriteRequest;
import org.apache.kylin.rest.request.ModelSuggestionRequest;
import org.apache.kylin.rest.request.OptRecRequest;
import org.apache.kylin.rest.request.SqlAccelerateRequest;
import org.apache.kylin.rest.response.EnvelopeResponse;
import org.apache.kylin.rest.response.OptRecResponse;
import org.apache.kylin.rest.response.SuggestionResponse;
import org.apache.kylin.rest.service.ModelService;
import org.apache.kylin.rest.service.ModelSmartService;
import org.apache.kylin.rest.service.OptRecApproveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@EnableDiscoveryClient
@RequestMapping(value = "/api/models", produces = { HTTP_VND_APACHE_KYLIN_JSON })
public class ModelRecController extends NBasicController {

    private static final String MODEL_ID = "modelId";
    private static final String REC_COUNT_ACTION = "action";

    @Autowired
    @Qualifier("modelService")
    private ModelService modelService;

    @Autowired
    private OptRecApproveService optRecApproveService;

    @Autowired
    private ModelSmartService modelSmartService;

    @ApiOperation(value = "suggestModel", tags = { "AI" }, notes = "")
    @PostMapping(value = "/suggest_model")
    @ResponseBody
    @WaitForSyncBeforeRPC
    public EnvelopeResponse<SuggestionResponse> suggestModel(@RequestBody SqlAccelerateRequest request) {
        checkProjectName(request.getProject());
        checkProjectNotSemiAuto(request.getProject());
        AbstractContext proposeContext = modelSmartService.suggestModel(request.getProject(), request.getSqls(),
                request.getReuseExistedModel(), true);
        return new EnvelopeResponse<>(KylinException.CODE_SUCCESS,
                modelSmartService.buildModelSuggestionResponse(proposeContext), "");
    }

    @Deprecated
    @ApiOperation(value = "checkIfCanAnsweredByExistedModel", tags = { "AI" }, notes = "")
    @PostMapping(value = "/can_answered_by_existed_model")
    @ResponseBody
    @WaitForSyncBeforeRPC
    public EnvelopeResponse<Boolean> couldAnsweredByExistedModel(@RequestBody FavoriteRequest request) {
        checkProjectName(request.getProject());
        checkProjectNotSemiAuto(request.getProject());
        return new EnvelopeResponse<>(KylinException.CODE_SUCCESS,
                modelSmartService.couldAnsweredByExistedModel(request.getProject(), request.getSqls()), "");
    }

    @ApiOperation(value = "suggestModel", tags = { "AI" }, notes = "")
    @PostMapping(value = "/model_recommendation")
    @ResponseBody
    public EnvelopeResponse<String> approveSuggestModel(@RequestBody ModelSuggestionRequest request) {
        checkProjectName(request.getProject());
        checkProjectNotSemiAuto(request.getProject());
        try {
            request.getNewModels().forEach(req -> {
                req.setWithModelOnline(request.isWithModelOnline());
                req.setWithEmptySegment(request.isWithEmptySegment());
            });
            modelService.batchCreateModel(request.getProject(), request.getNewModels(), request.getReusedModels());
            return new EnvelopeResponse<>(KylinException.CODE_SUCCESS, "", "");
        } catch (LookupTableException e) {
            throw new KylinException(FAILED_CREATE_MODEL, e.getMessage(), e);
        }
    }

    @ApiOperation(value = "approveOptimizeRecommendations", tags = { "AI" }, notes = "Add URL: {model}")
    @PostMapping(value = "/recommendations")
    @ResponseBody
    public EnvelopeResponse<OptRecResponse> approveOptimizeRecommendations(@RequestBody OptRecRequest request) {
        checkProjectName(request.getProject());
        checkProjectNotSemiAuto(request.getProject());
        String modelId = request.getModelId();
        checkRequiredArg(MODEL_ID, modelId);
        OptRecResponse optRecResponse = optRecApproveService.approve(request.getProject(), request);
        return new EnvelopeResponse<>(KylinException.CODE_SUCCESS, optRecResponse, "");
    }
}