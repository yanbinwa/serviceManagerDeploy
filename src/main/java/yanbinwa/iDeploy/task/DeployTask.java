package yanbinwa.iDeploy.task;

import java.util.Map;

import yanbinwa.iDeploy.data.DeployServiceInfoData;

public interface DeployTask
{
    Map<String, Map<String, DeployServiceInfoData>> getDelServiceDataMap();
    
    Map<String, Object> getAddServiceInfoDataMap();
}
