package yanbinwa.iDeploy.service;

import org.springframework.beans.factory.InitializingBean;

import yanbinwa.common.exceptions.ServiceUnavailableException;
import yanbinwa.common.iInterface.ConfigServiceIf;
import yanbinwa.common.iInterface.ServiceLifeCycle;

public interface DeployService extends InitializingBean, ServiceLifeCycle, ConfigServiceIf 
{
    public static final String DEPLOY_BASEINFO_KEY = "baseInfo";
    public static final String DEPLOY_COMMONS_KEY = "commons";
    public static final String DEPLOY_COMPONENTS_KEY = "components";
    
    public static final String DEPLOY_DEVICES_KEY = "devices";
    
    public static final int DEPLOY_TASK_POLL_TIMEOUT = 1000;
    
    public static final String HTTP_HEAD = "http://";
    public static final String STOP_MANAGER_SERVICE_ACTION = "stopManageService";
    
    public static final String BIN_SHELL = "/bin/bash";
    
    public static final String MANIFEST_FILE_TMP = "/tmp/manifest.yml";
    public static final String MANIFEST_FILE_REMOTE_DIR = "/tmp";
    public static final String MANIFEST_FILE_REMOTE_PATH = "/tmp/manifest.yml";
    
    public static final String CHECK_SERVICE_EXIST_CMD = "docker ps | grep ";
    
    String getServiceName() throws ServiceUnavailableException;
    
    void startManageService();

    void stopManageService();
}
