/*
 * Copyright 2014 by SCSK Corporation.
 * 
 * This file is part of PrimeCloud Controller(TM).
 * 
 * PrimeCloud Controller(TM) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PrimeCloud Controller(TM) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PrimeCloud Controller(TM). If not, see <http://www.gnu.org/licenses/>.
 */
package jp.primecloud.auto.api.component;


import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import jp.primecloud.auto.api.ApiSupport;
import jp.primecloud.auto.api.ApiValidate;

import org.apache.commons.lang.BooleanUtils;

import jp.primecloud.auto.api.response.component.EditComponentResponse;
import jp.primecloud.auto.common.status.ComponentInstanceStatus;
import jp.primecloud.auto.common.status.ComponentStatus;
import jp.primecloud.auto.entity.crud.Component;
import jp.primecloud.auto.entity.crud.ComponentInstance;
import jp.primecloud.auto.exception.AutoApplicationException;
import jp.primecloud.auto.exception.AutoException;
import jp.primecloud.auto.util.MessageUtils;


@Path("/EditComponent")
public class EditComponent extends ApiSupport{

    /**
     *
     * サービス編集
     *
     * @param uriInfo URI情報(InstanceNos取得の為)
     * @param farmNo ファーム番号
     * @param componentNo コンポーネント番号
     * @param diskSize ディスクサイズ
     * @param comment コメント
     * @param customParam1 カスタムパラメータ1(Puppetマニフェストカスタム設定値)
     * @param customParam2 カスタムパラメータ2(Puppetマニフェストカスタム設定値)
     * @param customParam3 カスタムパラメータ3(Puppetマニフェストカスタム設定値)
     * @return EditComponentResponse
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public EditComponentResponse editComponent(
	        @Context UriInfo uriInfo,
            @QueryParam(PARAM_NAME_FARM_NO) String farmNo,
	        @QueryParam(PARAM_NAME_COMPONENT_NO) String componentNo,
	        @QueryParam(PARAM_NAME_DISK_SIZE) String diskSize,
	        @QueryParam(PARAM_NAME_COMMENT) String comment,
	        @QueryParam(PARAM_NAME_CUSTOM_PARAM_1) String customParam1,
	        @QueryParam(PARAM_NAME_CUSTOM_PARAM_2) String customParam2,
	        @QueryParam(PARAM_NAME_CUSTOM_PARAM_3) String customParam3){

        EditComponentResponse response = new EditComponentResponse();

        try {
            // 入力チェック
            //FarmNo
            ApiValidate.validateFarmNo(farmNo);
            //ComponentNo
            ApiValidate.validateComponentNo(componentNo);
            //コンポーネント取得
            Component component = componentDao.read(Long.parseLong(componentNo));
            if (component == null || BooleanUtils.isTrue(component.getLoadBalancer())) {
                // コンポーネントが存在しない または ロードバランサーコンポーネント
                throw new AutoApplicationException("EAPI-100000", "Component",
                           PARAM_NAME_COMPONENT_NO, componentNo);
            }
            if (BooleanUtils.isFalse(component.getFarmNo().equals(Long.parseLong(farmNo)))) {
                //ファームとコンポーネントが一致しない
                throw new AutoApplicationException("EAPI-100022", "Component", farmNo, PARAM_NAME_COMPONENT_NO, componentNo);
            }
            //Componentに紐づくComponentInstance取得
            List<ComponentInstance> componentInstances = componentInstanceDao.readByComponentNo(Long.parseLong(componentNo));
            //ComponentStatus取得
            ComponentStatus status = getComponentStatus(componentInstances);
            if (ComponentStatus.STOPPED != status) {
                // コンポーネントが停止済みではない
                throw new AutoApplicationException("EAPI-100015", componentNo);
            }
            //DiskSize
            ApiValidate.validateDiskSize(diskSize);
            //Comments
            ApiValidate.validateComment(comment);
            //customParam1
            ApiValidate.validateCustomParam1(customParam1);
            //customParam2
            ApiValidate.validateCustomParam2(customParam2);
            //customParam3
            ApiValidate.validateCustomParam3(customParam3);

            // サービス編集
            componentService.updateComponent(Long.parseLong(componentNo), comment, Integer.parseInt(diskSize),
                    customParam1, customParam2, customParam3);

            response.setSuccess(true);
        } catch (Throwable e){
            String message = "";
            if (e instanceof AutoException || e instanceof AutoApplicationException) {
                message = e.getMessage();
            } else {
                message = MessageUtils.getMessage("EAPI-000000");
            }
            log.error(message, e);
            response.setMessage(message);
            response.setSuccess(false);
        }

        return  response;
	}

    /**
     *
     * Componentに紐づくComponentInstanceからComponentStatusを取得する
     *
     * @param componentInstances
     * @return ComponentStatus
     */
    private ComponentStatus getComponentStatus(List<ComponentInstance> componentInstances) {
        ComponentStatus status = null;
        List<ComponentInstanceStatus> statuses = new ArrayList<ComponentInstanceStatus>();
        for (ComponentInstance componentInstance: componentInstances) {
            statuses.add(ComponentInstanceStatus.fromStatus(componentInstance.getStatus()));
        }
        if (statuses.contains(ComponentInstanceStatus.WARNING)) {
            status = ComponentStatus.WARNING;
        } else if (statuses.contains(ComponentInstanceStatus.CONFIGURING)) {
            status = ComponentStatus.CONFIGURING;
        } else if (statuses.contains(ComponentInstanceStatus.RUNNING)) {
            if (statuses.contains(ComponentInstanceStatus.STARTING)) {
                status = ComponentStatus.CONFIGURING;
            } else if (statuses.contains(ComponentInstanceStatus.STOPPING)) {
                status = ComponentStatus.CONFIGURING;
            } else {
                status = ComponentStatus.RUNNING;
            }
        } else if (statuses.contains(ComponentInstanceStatus.STARTING)) {
            status = ComponentStatus.STARTING;
        } else if (statuses.contains(ComponentInstanceStatus.STOPPING)) {
            status = ComponentStatus.STOPPING;
        } else {
            status = ComponentStatus.STOPPED;
        }
        return status;
    }
}