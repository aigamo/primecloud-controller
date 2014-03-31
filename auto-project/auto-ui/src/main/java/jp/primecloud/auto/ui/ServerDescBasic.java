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
package jp.primecloud.auto.ui;

import jp.primecloud.auto.common.status.ComponentInstanceStatus;
import jp.primecloud.auto.common.status.InstanceStatus;
import jp.primecloud.auto.entity.crud.ComponentType;
import jp.primecloud.auto.entity.crud.Instance;
import jp.primecloud.auto.entity.crud.InstanceConfig;
import jp.primecloud.auto.service.dto.ComponentDto;
import jp.primecloud.auto.service.dto.ComponentInstanceDto;
import jp.primecloud.auto.service.dto.ImageDto;
import jp.primecloud.auto.service.dto.InstanceDto;
import jp.primecloud.auto.service.dto.PlatformDto;
import jp.primecloud.auto.ui.data.ComponentDtoContainer;
import jp.primecloud.auto.ui.util.Icons;
import jp.primecloud.auto.ui.util.VaadinUtils;
import jp.primecloud.auto.ui.util.ViewProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.primecloud.auto.component.mysql.MySQLConstants;
import com.vaadin.data.Container;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * <p>
 * サーバView下部の基本情報を生成します。
 * </p>
 *
 */
@SuppressWarnings("serial")
public class ServerDescBasic extends Panel {

    BasicInfo left = new BasicInfo();

    AttachService right = new AttachService("", null);


    public ServerDescBasic() {
        setHeight("100%");
        addStyleName(Reindeer.PANEL_LIGHT);

        VerticalLayout panel = (VerticalLayout)getContent();
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setMargin(true);
        panel.setSpacing(false);
        panel.addStyleName("server-desc-basic");

        HorizontalLayout hlPanels = new HorizontalLayout();
        hlPanels.setWidth("100%");
        hlPanels.setHeight("100%");
        hlPanels.setMargin(true);
        hlPanels.setSpacing(true);
        hlPanels.addStyleName("server-desc-basic");
//        setContent(hlPanels);

        left.setWidth("100%");
        right.setHeight("100%");
        right.setWidth("100%");

        //表同士の間隔をあける
        Label padding = new Label(" ");
        padding.setWidth("7px");
        padding.setHeight("99%");
        padding.addStyleName("desc-padding");

        Label padding2 = new Label("");
        padding2.setWidth("1px");

        hlPanels.addComponent(left);
        hlPanels.addComponent(padding);
        hlPanels.addComponent(padding2);
        hlPanels.addComponent(right);
        hlPanels.setExpandRatio(left, 40);
        hlPanels.setExpandRatio(right, 60);

        panel.addComponent(hlPanels);
        panel.setExpandRatio(hlPanels, 1.0f);

    }

    //右側サーバ一覧パネル
    class AttachService extends Table {

        final String COLUMN_HEIGHT = "28px";

        String[] COLNAME = {
                ViewProperties.getCaption("field.serviceName"),
                ViewProperties.getCaption("field.managementUrl"),
                ViewProperties.getCaption("field.serviceStatus"),
                ViewProperties.getCaption("field.serviceDetail") };


        public AttachService(String caption, Container dataSource) {
            super(caption, dataSource);
            setIcon(Icons.SERVICETAB.resource());

            addGeneratedColumn("componentName", new ColumnGenerator() {
                public Component generateCell(Table source, Object itemId, Object columnId) {
                    ComponentDto componentDto = (ComponentDto) itemId;
                    jp.primecloud.auto.entity.crud.Component p = componentDto.getComponent();

                    String name;
                    if (StringUtils.isEmpty(p.getComment())) {
                        name = p.getComponentName();
                    } else {
                        name = p.getComment() + "\n[" + p.getComponentName() + "]";
                    }
                    Label slbl = new Label(name, Label.CONTENT_PREFORMATTED);
                    slbl.setHeight(COLUMN_HEIGHT);
                    return slbl;

                }
            });

            addGeneratedColumn("urlIcon", new ColumnGenerator() {
                public Component generateCell(Table source, Object itemId, Object columnId) {
                    ComponentDto componentDto = (ComponentDto) itemId;
                    jp.primecloud.auto.entity.crud.Component p = componentDto.getComponent();
                    ComponentType componentType = componentDto.getComponentType();
                    String type = componentType.getComponentTypeName();

                    AutoApplication ap = (AutoApplication) getApplication();
                    InstanceDto dto = (InstanceDto) ap.myCloud.myCloudTabs.serverTable.getValue();

                    //リンクを追加する
                    String status = "";
                    String url = "";
                    for (ComponentInstanceDto componentInstance : dto.getComponentInstances()) {
                        if (componentInstance.getComponentInstance().getComponentNo().equals(p.getComponentNo())) {
                            status = componentInstance.getComponentInstance().getStatus();
                            url = componentInstance.getUrl();
                            break;
                        }
                    }
                    Icons icon = Icons.fromName(type);

                    //MySQLならMasterとSlaveでアイコンを変える
                    if(MySQLConstants.COMPONENT_TYPE_NAME.equals(type)){
                        // Master
                        Long masterInstanceNo = null;
                        for (InstanceConfig config : dto.getInstanceConfigs()) {
                            if (MySQLConstants.CONFIG_NAME_MASTER_INSTANCE_NO.equals(config.getConfigName())) {
                                if (StringUtils.isEmpty(config.getConfigValue())) {
                                    masterInstanceNo = config.getInstanceNo();
                                    break;
                                }
                            }
                        }
                        if(masterInstanceNo != null){
                            if(masterInstanceNo.equals(dto.getInstance().getInstanceNo())){
                                icon = Icons.MYSQL_MASTER;
                            }else{
                                icon = Icons.MYSQL_SLAVE;
                            }
                        }else{
                            icon = Icons.MYSQL_SLAVE;
                        }
                    }
                    Link slbl = new Link(ViewProperties.getCaption("field.managementLink"), new ExternalResource(url));
                    slbl.setTargetName("_blank");
                    slbl.setIcon(icon.resource());
                    slbl.setHeight(COLUMN_HEIGHT);
                    slbl.setEnabled(false);

                    if(status.equals(ComponentInstanceStatus.RUNNING.toString())){
                        slbl.setDescription(url);
                        slbl.setEnabled(true);
                    }

                    return slbl;

                }
            });

            addGeneratedColumn("status", new ColumnGenerator() {
                public Component generateCell(Table source, Object itemId, Object columnId) {
                    ComponentDto componentDto = (ComponentDto) itemId;
                    jp.primecloud.auto.entity.crud.Component p = componentDto.getComponent();
                    AutoApplication ap = (AutoApplication) getApplication();
                    InstanceDto dto = (InstanceDto) ap.myCloud.myCloudTabs.serverTable.getValue();

                    String status = "";
                    for (ComponentInstanceDto componentInstance : dto.getComponentInstances()) {
                        if (componentInstance.getComponentInstance().getComponentNo().equals(p.getComponentNo())) {
                            status = componentInstance.getComponentInstance().getStatus();
                            break;
                        }
                    }

                    String a = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                    Icons icon = Icons.fromName(a);
                    Label slbl = new Label("<img src=\"" + VaadinUtils.getIconPath(ServerDescBasic.this, icon)
                            + "\"><div>" + a + "</div>", Label.CONTENT_XHTML);
                    slbl.setHeight(COLUMN_HEIGHT);
                    return slbl;

                }
            });

            addGeneratedColumn("serviceDetail", new ColumnGenerator() {
                public Component generateCell(Table source, Object itemId, Object columnId) {
                    ComponentDto componentDto = (ComponentDto) itemId;
                    jp.primecloud.auto.entity.crud.Component p = componentDto.getComponent();
                    ComponentType componentType = componentDto.getComponentType();
                    String type = componentType.getComponentTypeName();
                    String name = componentType.getComponentTypeNameDisp();

                    //MySQLならMasterとSlaveでアイコンを変える
                    AutoApplication ap = (AutoApplication) getApplication();
                    InstanceDto dto = (InstanceDto) ap.myCloud.myCloudTabs.serverTable.getValue();

                    if(MySQLConstants.COMPONENT_TYPE_NAME.equals(type)){
                        // Master
                        Long masterInstanceNo = null;
                        for (InstanceConfig config : dto.getInstanceConfigs()) {
                            if (MySQLConstants.CONFIG_NAME_MASTER_INSTANCE_NO.equals(config.getConfigName())) {
                                if (StringUtils.isEmpty(config.getConfigValue())) {
                                    masterInstanceNo = config.getInstanceNo();
                                    break;
                                }
                            }
                        }
                        if(masterInstanceNo != null){
                            if(masterInstanceNo.equals(dto.getInstance().getInstanceNo())){
                                name = name + " " + ViewProperties.getComponentTypeName(type + ".master");
                            }else{
                                name = name + " " + ViewProperties.getComponentTypeName(type + ".slave");
                            }
                        }else{
                            name = name + " " + ViewProperties.getComponentTypeName(type + ".slave");
                        }
                    }
                    Label slbl = new Label (name);

                    return slbl;
                }
            });

            setColumnExpandRatio("serviceDetail", 100);

            //テーブルのカラムに対してStyleNameを設定
            setCellStyleGenerator(new Table.CellStyleGenerator() {
                public String getStyle(Object itemId, Object propertyId) {

                    if (propertyId == null) {
                        return "";
                    } else {
                        return propertyId.toString().toLowerCase();
                    }
                }
            });

        }

        @Override
        public void setContainerDataSource(Container newDataSource) {
            super.setContainerDataSource(newDataSource);
            setColumnHeaderMode(Table.COLUMN_HEADER_MODE_EXPLICIT);
            addStyleName("server-desc-basic-service");
            setCaption(ViewProperties.getCaption("table.serverServices"));
            setSortDisabled(true);
            setMultiSelect(false);
            setImmediate(false);
            setVisible(true);
        }

        public void refresh(ComponentDtoContainer dataSource) {
            setContainerDataSource(dataSource);
            setVisibleColumns(ComponentDtoContainer.SERVER_DESC);
            if (dataSource != null && dataSource.size() > 0  ){
                setColumnHeaders(COLNAME);
            }
        }


    }

    //左側基本情報の初期化
    class BasicInfo extends Panel {


        protected Log log = LogFactory.getLog(BasicInfo.class);

        final String COLUMN_HEIGHT = "30px";
        final String COLUMN_HEIGHT_DOUBLE = "60px";

        Label hostName ;

        Label fqdn ;

        Label comment ;

        Label ipAddress ;

        Label platform ;

        Label status ;

        CssLayout layoutOsType;
        Label ostype ;
        Button getPassword;

        //項目名
        String[] CAPNAME = {
//                ViewProperties.getCaption("field.serverName"),
                ViewProperties.getCaption("field.fqdn"),
                ViewProperties.getCaption("field.ipAddress"),
                ViewProperties.getCaption("field.platform"),
                ViewProperties.getCaption("field.os"),
                ViewProperties.getCaption("field.serverOsStatus"),
                ViewProperties.getCaption("field.comment")
                };

        GridLayout layout;

        BasicInfo() {
            setCaption(ViewProperties.getCaption("table.serverBasicInfo"));
            setHeight("100%");
            setStyleName("server-desc-basic-panel");

            VerticalLayout vlay = (VerticalLayout )getContent();
            vlay.setStyleName("server-desc-basic-panel");
            vlay.setMargin(true);

            layout = new GridLayout(2,CAPNAME.length);

            layout.setWidth("100%");
            layout.setStyleName("server-desc-basic-info");
            layout.setColumnExpandRatio(0, 35);
            layout.setColumnExpandRatio(1, 65);
            vlay.addComponent(layout);

            //項目名設定
            for (int i=0; i<CAPNAME.length ; i++){
                Label lbl1 = new Label(CAPNAME[i], Label.CONTENT_XHTML);
                Label lbl2 = new Label("");
                lbl1.setHeight(COLUMN_HEIGHT);
                layout.addComponent(lbl1,0,i);
                layout.addComponent(lbl2,1,i);
            }

            //EC2 Windows用パスワード取得ボタン
            getPassword = new Button(ViewProperties.getCaption("button.getPassword"));
            getPassword.setDescription(ViewProperties.getCaption("description.getPassword"));
            getPassword.setIcon(Icons.LOGIN.resource());
            getPassword.addStyleName("getpassword");
            getPassword.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    AutoApplication ap = (AutoApplication) getApplication();
                    InstanceDto dto = (InstanceDto) ap.myCloud.myCloudTabs.serverTable.getValue();
                    Long instanceNo = (Long) getPassword.getData();
                    WinPassword winPassword = new WinPassword(dto,instanceNo);
                    getWindow().addWindow(winPassword);
                }
            });


        }

        public void setItem(InstanceDto instanceDto) {
            int line=0;

            if (instanceDto != null) {
                Instance instance = instanceDto.getInstance();
//                hostName = new Label(instance.getInstanceName(), Label.CONTENT_TEXT);
//                layout.removeComponent( 1, line );
//                layout.addComponent(hostName , 1, line++ );

                fqdn = new Label(instance.getFqdn(), Label.CONTENT_TEXT);
                layout.removeComponent( 1, line );
                layout.addComponent(fqdn , 1, line++ );

                ipAddress = new Label(instance.getPublicIp(), Label.CONTENT_TEXT);
                layout.removeComponent( 1, line );
                layout.addComponent(ipAddress , 1, line++ );

                //プラットフォームの表示
                PlatformDto platformDto = instanceDto.getPlatform();

                // TODO: アイコン名の取得ロジックのリファクタリング
                Icons icon = Icons.NONE;
                if ("aws".equals(platformDto.getPlatform().getPlatformType())) {
                    if (platformDto.getPlatformAws().getEuca()) {
                        icon = Icons.EUCALYPTUS;
                    } else {
                        icon = Icons.AWS;
                    }
                } else if ("vmware".equals(platformDto.getPlatform().getPlatformType())) {
                    icon = Icons.VMWARE;
                } else if ("nifty".equals(platformDto.getPlatform().getPlatformType())) {
                    icon = Icons.NIFTY;
                } else if ("cloudstack".equals(platformDto.getPlatform().getPlatformType())) {
                    icon = Icons.CLOUD_STACK;
                }

                String description = platformDto.getPlatform().getPlatformNameDisp();
                platform = new Label("<img src=\"" + VaadinUtils.getIconPath(ServerDescBasic.this, icon) + "\"><div>"
                        + description + "</div>", Label.CONTENT_XHTML);
                layout.removeComponent( 1, line );
                layout.addComponent(platform , 1, line++ );


                //OS名の表示
                ImageDto imageDto = instanceDto.getImage();
                // TODO: アイコン名取得ロジックのリファクタリング
                String os = imageDto.getImage().getOsDisp();
                Icons osIcon = Icons.NONE;
                if (imageDto.getImage().getOs().startsWith("centos")) {
                    osIcon = Icons.CENTOS;
                }else if (imageDto.getImage().getOs().startsWith("windows")) {
                    osIcon = Icons.WINDOWS;
                }
                layoutOsType = new CssLayout();
                ostype = new Label("<img src=\"" + VaadinUtils.getIconPath(ServerDescBasic.this, osIcon) + "\"><div>"
                        + os + "</div>", Label.CONTENT_XHTML);
                layoutOsType.setSizeFull();
                layoutOsType.setMargin(false);
                layoutOsType.addComponent(ostype);

                //OSがWindowsの場合パスワード取得ボタンを表示
                if (imageDto.getImage().getOs().startsWith("windows")) {
                    //ただしEUCALYPTUSは除外
                   if (platformDto.getPlatformAws() == null || platformDto.getPlatformAws().getEuca() == false){
                    InstanceStatus instanceStatus = InstanceStatus.fromStatus(instance.getStatus());
                    if(instanceStatus == InstanceStatus.RUNNING){
                        getPassword.setEnabled(true);
                        getPassword.setData(instance.getInstanceNo());
                    } else {
                        getPassword.setEnabled(false);
                    }
                    layoutOsType.addComponent(getPassword);
                    layoutOsType.setHeight("60px");
                }
                }

                layout.removeComponent( 1, line );
                layout.addComponent(layoutOsType , 1, line++ );

                //ステータスの表示
                String stat = instance.getStatus().substring(0, 1).toUpperCase()
                        + instance.getStatus().substring(1).toLowerCase();
                Icons icon2 = Icons.fromName(stat);

                status = new Label("<img src=\"" + VaadinUtils.getIconPath(ServerDescBasic.this, icon2) + "\"><div>"
                        + stat + "</div>", Label.CONTENT_XHTML);
                layout.removeComponent( 1, line );
                layout.addComponent(status , 1, line++ );

                //コメント
                comment = new Label(instance.getComment(), Label.CONTENT_XHTML);
                layout.removeComponent( 1, line);
                layout.addComponent(comment , 1, line++ );


            } else {
//                hostName = new Label("", Label.CONTENT_TEXT);
//                layout.removeComponent( 1, line );
//                layout.addComponent(hostName , 1, line++ );

                fqdn = new Label("", Label.CONTENT_TEXT);
                layout.removeComponent( 1, line );
                layout.addComponent(fqdn , 1, line++ );

                ipAddress = new Label("", Label.CONTENT_TEXT);
                layout.removeComponent( 1, line );
                layout.addComponent(ipAddress , 1, line++ );

                //プラットフォームの表示
                platform = new Label("", Label.CONTENT_XHTML);
                layout.removeComponent( 1, line );
                layout.addComponent(platform , 1, line++ );

                //OS名の表示
                layoutOsType = new CssLayout();
                ostype = new Label("", Label.CONTENT_XHTML);
                layoutOsType.setSizeFull();
                layoutOsType.setMargin(false);
                layoutOsType.addComponent(ostype);
                layout.removeComponent( 1, line );
                layout.addComponent(layoutOsType , 1, line++ );


                //ステータスの表示
                status = new Label("", Label.CONTENT_XHTML);
                layout.removeComponent( 1, line );
                layout.addComponent(status , 1, line++ );

                //コメント
                comment = new Label("", Label.CONTENT_XHTML);
                layout.removeComponent( 1, line );
                layout.addComponent(comment , 1, line++ );


            }

        }
    }

    public void initializeData() {
        right.getContainerDataSource().removeAllItems();
        left.setItem(null);
    }

}
