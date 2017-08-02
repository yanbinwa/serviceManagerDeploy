package yanbinwa.iDeploy.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DeployGroupInfoData extends DeployData
{ 
    public static final String DEVICES_KEY = "devices";
    public static final String FEATURES_KEY = "features";
    
    String getServiceGroupName();  
    
    void compareGroupInfoData(DeployGroupInfoData oldGroupInfoData, Map<String, DeployServiceInfoData> addServiceInfoDataMap, Map<String, DeployServiceInfoData> delServiceInfoDataMap);
    
    Map<String, Object> getGroupInfoDataByServiceList(Set<String> serviceNameList);
    
    List<String> getFeatureNameList();
}
