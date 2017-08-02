package yanbinwa.iDeploy.data;

public interface DeployFeatureInfoData extends DeployData
{
    
    public static final String SERVICEGROUP_KEY = "serviceGroup";
    
    void setServiceGroup(String serviceGroupList);
    
    String getName();
}
