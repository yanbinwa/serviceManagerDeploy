package yanbinwa.iDeploy.data;

import java.util.Map;

import yanbinwa.common.utils.MapUtil;

public class DeployFeatureInfoDataImpl implements DeployFeatureInfoData
{

    String name = null;
    
    Map<String, Object> featureInfoMap = null;
    
    public DeployFeatureInfoDataImpl()
    {
        
    }
    
    public DeployFeatureInfoDataImpl(String name)
    {
        this.name = name;
    }
    
    @Override
    public void loadFromMap(Map<String, Object> map)
    {
        this.featureInfoMap = map;
    }

    @Override
    public Map<String, Object> createMap()
    {
        return featureInfoMap;
    }

    @Override
    public void setServiceGroup(String serviceGroupList)
    {
        if (featureInfoMap == null)
        {
            return;
        }
        featureInfoMap.put(DeployFeatureInfoData.SERVICEGROUP_KEY, serviceGroupList);
    }

    @Override
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        ret.append("feature name is: " + name).append("; ")
           .append("featureInfoMap is: " + featureInfoMap);
        
        return ret.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DeployFeatureInfoDataImpl))
        {
            return false;
        }
        DeployFeatureInfoDataImpl other = (DeployFeatureInfoDataImpl)obj;
        if(!this.name.equals(other.name))
        {
            return false;
        }
        if(MapUtil.compareMap(this.featureInfoMap, other.featureInfoMap))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
