package yanbinwa.iDeploy.task;

import java.util.Map;

import yanbinwa.iDeploy.data.DeployServiceInfoData;


/**
 * 
 * 之后考虑可以将Task持久化，保证每个task都可以运行
 * 
 * @author yanbinwa
 *
 */
public class DeployTaskImpl implements DeployTask
{
    private Map<String, Map<String, DeployServiceInfoData>> delServiceMap = null;
    
    private Map<String, Object> addServiceInfoDataMap = null;
    
    public DeployTaskImpl()
    {
        
    }
    
    public DeployTaskImpl(Map<String, Map<String, DeployServiceInfoData>> delServiceMap, Map<String, Object> addServiceInfoDataMap)
    {
        this.delServiceMap = delServiceMap;
        this.addServiceInfoDataMap = addServiceInfoDataMap;
    }
    
    @Override
    public Map<String, Map<String, DeployServiceInfoData>> getDelServiceDataMap()
    {
        return delServiceMap;
    }
    
    @Override
    public Map<String, Object> getAddServiceInfoDataMap()
    {
        return addServiceInfoDataMap;
    }
    
    @Override
    public String toString()
    {
        return "delServiceMap is: " + delServiceMap + "; addServiceInfoDataMap is: " + addServiceInfoDataMap;
    }
}
