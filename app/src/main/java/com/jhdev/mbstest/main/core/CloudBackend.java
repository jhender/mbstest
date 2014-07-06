/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jhdev.mbstest.main.core;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.cloud.backend.android.mobilebackend.Mobilebackend;
import com.google.cloud.backend.android.mobilebackend.model.EntityDto;
import com.google.cloud.backend.android.mobilebackend.model.EntityListDto;
import com.google.cloud.backend.android.mobilebackend.model.QueryDto;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Base Cloud Backend API class that provides CRUD operation and query operation
 * on the backend. All the methods work synchronously, so they can not be called
 * directly from UI thread of {@link android.app.Activity} or {@link android.app.Fragment}. (Use
 * {@link CloudBackendAsync} for calling backend from UI thread). See
 * {@link CloudBackendTest} for detailed usage.
 */
public class CloudBackend {

    static {
        // to prevent EOFException after idle
        // http://code.google.com/p/google-http-java-client/issues/detail?id=116
        System.setProperty("http.keepAlive", "false");
    }

    private GoogleAccountCredential mCredential;

    /**
     * Sets {@link com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential} that will be used on all backend
     * calls. By setting null, all call will not be associated with user account
     * info.
     *
     * @param credential {@link com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential}
     */
    public void setCredential(GoogleAccountCredential credential) {
        this.mCredential = credential;
    }

    /**
     * Returns {@link com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential} that has been set to this
     * backend.
     *
     * @return {@link com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential}
     */
    public GoogleAccountCredential getCredential() {
        return this.mCredential;
    }

    // building CloudBackend endpoints and configuring authentication and
    // exponential back-off policy
    private Mobilebackend getMBSEndpoint() {

        // check if credential has account name
        final GoogleAccountCredential gac = mCredential == null
                || mCredential.getSelectedAccountName() == null ? null : mCredential;

        // create HttpRequestInitializer
        HttpRequestInitializer hri = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setBackOffPolicy(new ExponentialBackOffPolicy());
                if (gac != null) {
                    gac.initialize(request);
                }
            }
        };

        // build MBS builder
        // (specify gac or hri as the third parameter)
        return new Mobilebackend.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                hri)
                .setRootUrl(Consts.ENDPOINT_ROOT_URL).build();
    }

    /**
     * Inserts a CloudEntity into the backend synchronously.
     *
     * @param ce {@link com.jhdev.mbstest.main.core.CloudEntity} for inserting a CloudEntity.
     * @return {@link com.jhdev.mbstest.main.core.CloudEntity} that has updated fields (like updatedAt and
     *         new Id).
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public com.jhdev.mbstest.main.core.CloudEntity insert(com.jhdev.mbstest.main.core.CloudEntity ce) throws IOException {
        EntityDto resultEntityDto = getMBSEndpoint().endpointV1()
                .insert(ce.getKindName(), ce.getEntityDto()).execute();
        com.jhdev.mbstest.main.core.CloudEntity resultCo = com.jhdev.mbstest.main.core.CloudEntity.createCloudEntityFromEntityDto(resultEntityDto);
        Log.i(Consts.TAG, "insert: inserted: " + resultCo);
        return resultCo;
    }

    /**
     * Updates the specified {@link com.jhdev.mbstest.main.core.CloudEntity} on the backend synchronously.
     * If it does not have any Id, it creates a new Entity. If it has, find the
     * existing entity and update it.
     *
     * @param ce {@link com.jhdev.mbstest.main.core.CloudEntity} for updating a CloudEntity.
     * @return {@link com.jhdev.mbstest.main.core.CloudEntity} that has updated fields (like updatedAt and
     *         new Id).
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public CloudEntity update(CloudEntity ce) throws IOException {
        EntityDto resultEntityDto = getMBSEndpoint().endpointV1()
                .update(ce.getKindName(), ce.getEntityDto()).execute();
        CloudEntity resultCo = CloudEntity.createCloudEntityFromEntityDto(resultEntityDto);
        Log.i(Consts.TAG, "update: updated: " + resultCo);
        return resultCo;
    }

    /**
     * Inserts multiple {@link com.jhdev.mbstest.main.core.CloudEntity}s on the backend synchronously. Works
     * just the same as {@link #insert(CloudEntity)}.
     *
     * @param ceList {@link java.util.List} that holds {@link com.jhdev.mbstest.main.core.CloudEntity}s to save.
     * @return {@link java.util.List} that has updated {@link com.jhdev.mbstest.main.core.CloudEntity}s.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public List<CloudEntity> insertAll(List<com.jhdev.mbstest.main.core.CloudEntity> ceList) throws IOException {

        // prepare for EntityListDto
        List<EntityDto> cdList = new LinkedList<EntityDto>();
        for (com.jhdev.mbstest.main.core.CloudEntity co : ceList) {
            cdList.add(co.getEntityDto());
        }
        EntityListDto cdl = new EntityListDto();
        cdl.setEntries(cdList);

        // execute saveAll
        EntityListDto resultCdl;
        resultCdl = getMBSEndpoint().endpointV1().insertAll(cdl).execute();
        Log.i(Consts.TAG, "saveAll: saved: " + resultCdl.getEntries());
        List<com.jhdev.mbstest.main.core.CloudEntity> resultCoList = getListOfEntityDto(resultCdl);
        return resultCoList;
    }

    /**
     * Updates multiple {@link com.jhdev.mbstest.main.core.CloudEntity}s on the backend synchronously. Works
     * just the same as {@link #update(com.jhdev.mbstest.main.core.CloudEntity)}.
     *
     * @param coList {@link java.util.List} that holds {@link com.jhdev.mbstest.main.core.CloudEntity}s to save.
     * @return {@link java.util.List} that has updated {@link com.jhdev.mbstest.main.core.CloudEntity}s.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public List<com.jhdev.mbstest.main.core.CloudEntity> updateAll(List<com.jhdev.mbstest.main.core.CloudEntity> coList) throws IOException {

        // prepare for EntityListDto
        List<EntityDto> cdList = new LinkedList<EntityDto>();
        for (com.jhdev.mbstest.main.core.CloudEntity co : coList) {
            cdList.add(co.getEntityDto());
        }
        EntityListDto cdl = new EntityListDto();
        cdl.setEntries(cdList);

        // execute saveAll
        EntityListDto resultCdl;
        resultCdl = getMBSEndpoint().endpointV1().updateAll(cdl).execute();
        Log.i(Consts.TAG, "saveAll: saved: " + resultCdl.getEntries());
        List<com.jhdev.mbstest.main.core.CloudEntity> resultCoList = getListOfEntityDto(resultCdl);
        return resultCoList;
    }

    /**
     * Reads the specified {@link com.jhdev.mbstest.main.core.CloudEntity} synchronously.
     *
     * @param kindName Name of the table for the CloudEntity to get.
     * @param id Id of the CloudEntity to find.
     * @return {@link com.jhdev.mbstest.main.core.CloudEntity}.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public com.jhdev.mbstest.main.core.CloudEntity get(String kindName, String id) throws IOException {
        EntityDto cd = getMBSEndpoint().endpointV1().get(kindName, id).execute();
        com.jhdev.mbstest.main.core.CloudEntity co = com.jhdev.mbstest.main.core.CloudEntity.createCloudEntityFromEntityDto(cd);
        Log.i(Consts.TAG, "get: result: " + co);
        return co;
    }

    /**
     * Reads all the {@link com.jhdev.mbstest.main.core.CloudEntity}s synchronously specified by the
     * {@link java.util.List} of Ids.
     *
     * @param kindName Name of the table for the CloudEntities to get.
     * @param idList {@link java.util.List} of Ids of the CloudEntities to find.
     * @return {@link java.util.List} of the found {@link CloudEntity}s.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public List<com.jhdev.mbstest.main.core.CloudEntity> getAll(String kindName, List<String> idList) throws IOException {

        // prepare for EntityListDto
        EntityListDto cdl = createEntityListDto(kindName, idList);

        // execute getAll
        EntityListDto resultCdl;
        resultCdl = getMBSEndpoint().endpointV1().getAll(cdl).execute();
        Log.i(Consts.TAG, "getAll: result: " + resultCdl.getEntries());
        return getListOfEntityDto(resultCdl);
    }

    private EntityListDto createEntityListDto(String kindName, List<String> idList) {
        List<EntityDto> l = new LinkedList<EntityDto>();
        for (String id : idList) {
            EntityDto cd = new EntityDto();
            cd.setId(id);
            cd.setKindName(kindName);
            l.add(cd);
        }
        EntityListDto cdl = new EntityListDto();
        cdl.setEntries(l);
        return cdl;
    }

    private List<com.jhdev.mbstest.main.core.CloudEntity> getListOfEntityDto(EntityListDto cdl) {
        List<com.jhdev.mbstest.main.core.CloudEntity> l = new LinkedList<com.jhdev.mbstest.main.core.CloudEntity>();
        if (cdl.getEntries() != null) { // production returns null when its
            // empty
            for (EntityDto cd : cdl.getEntries()) {
                l.add(com.jhdev.mbstest.main.core.CloudEntity.createCloudEntityFromEntityDto(cd));
            }
        }
        return l;
    }

    /**
     * Deletes the specified {@link CloudEntity} synchronously.
     *
     * @param kindName Name of the table for the CloudEntity to delete.
     * @param id Id of the CloudEntity to delete.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public void delete(String kindName, String id) throws IOException {
        getMBSEndpoint().endpointV1().delete(kindName, id).execute();
        Log.i(Consts.TAG, "delete: deleted: " + kindName + "/" + id);
    }

    /**
     * Deletes the specified {@link CloudEntity} synchronously.
     *
     * @param co {@link CloudEntity} to delete
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public void delete(com.jhdev.mbstest.main.core.CloudEntity co) throws IOException {
        getMBSEndpoint().endpointV1().delete(co.getKindName(), co.getId()).execute();
        Log.i(Consts.TAG, "delete: deleted: " + co);
    }

    /**
     * Deletes all the specified {@link CloudEntity}s synchronously.
     *
     * @param kindName Name of the table for the CloudEntity to delete.
     * @param idList {@link java.util.List} that contains a list of Ids to delete.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public void deleteAllById(String kindName, List<String> idList) throws IOException {

        // prepare for EntityListDto
        EntityListDto cdl = createEntityListDto(kindName, idList);

        // delete
        getMBSEndpoint().endpointV1().deleteAll(cdl).execute();
        Log.i(Consts.TAG, "deleteAll: deleted: " + kindName + ": " + idList);
    }

    /**
     * Deletes all the specified {@link CloudEntity}s synchronously.
     *
     * @param kindName Name of the table for the CloudEntity to delete.
     * @param coList {@link java.util.List} that contains a list of Cloud to delete.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public void deleteAll(String kindName, List<com.jhdev.mbstest.main.core.CloudEntity> coList) throws IOException {
        List<String> idList = new LinkedList<String>();
        for (com.jhdev.mbstest.main.core.CloudEntity co : coList) {
            idList.add(co.getId());
        }
        deleteAllById(kindName, idList);
    }

    /**
     * Executes a query synchronously with specified {@link CloudQuery}.
     *
     * @param query {@link CloudQuery} to execute.
     * @return {@link java.util.List} of {@link com.jhdev.mbstest.main.core.CloudEntity} of the result.
     * @throws java.io.IOException When the call had failed for any reason.
     */
    public List<com.jhdev.mbstest.main.core.CloudEntity> list(CloudQuery query) throws IOException {

        // execute the query
        EntityListDto cbList;
        QueryDto cq = query.convertToQueryDto();
        Log.i(Consts.TAG, "list: executing query: " + cq);
        cbList = getMBSEndpoint().endpointV1().list(cq).execute();
        Log.i(Consts.TAG, "list: result: " + cbList.getEntries());

        // convert the result to List
        List<com.jhdev.mbstest.main.core.CloudEntity> coList = new LinkedList<com.jhdev.mbstest.main.core.CloudEntity>();
        if (cbList.getEntries() != null) {
            for (EntityDto cd : cbList.getEntries()) {
                coList.add(com.jhdev.mbstest.main.core.CloudEntity.createCloudEntityFromEntityDto(cd));
            }
        }
        return coList;
    }

}
