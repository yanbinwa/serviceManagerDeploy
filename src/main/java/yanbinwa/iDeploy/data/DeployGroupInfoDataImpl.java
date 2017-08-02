package yanbinwa.iDeploy.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import yanbinwa.common.utils.MapUtil;

public class DeployGroupInfoDataImpl implements DeployGroupInfoData
{

    private static final Logger logger = Logger.getLogger(DeployGroupInfoDataImpl.class);
    
    private String serviceGroupName = null;
    
    private Map<String, Object> groupBaseInfoMap = new HashMap<String, Object>();
    
    private Map<String, DeployFeatureInfoData> groupFeatureInfoMap = new HashMap<String, DeployFeatureInfoData>();
    
    private Map<String, DeployServiceInfoData> serviceInfoDataMap = new HashMap<String, DeployServiceInfoData>();
    
    
    public DeployGroupInfoDataImpl()
    {
        
    }
    
    public DeployGroupInfoDataImpl(String serviceGroupName)
    {
        this.serviceGroupName = serviceGroupName;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void loadFromMap(Map<String, Object> map)
    {
        if (map == null)
        {
            return;
        }
        groupBaseInfoMap.clear();
        groupBaseInfoMap.putAll(map);
        if (groupBaseInfoMap.containsKey(DEVICES_KEY))
        {
            Object servicesMapObj = groupBaseInfoMap.remove(DEVICES_KEY);
            if (servicesMapObj == null || !(servicesMapObj instanceof Map))
            {
                logger.error("deviceMapObj is null or not a map");
            }
            else
            {
                serviceInfoDataMap.clear();
                Map<String, Object> servicesMap = (Map<String, Object>) servicesMapObj;
                for(Map.Entry<String, Object> entry : servicesMap.entrySet())
                {
                    String serviceName = entry.getKey();
                    Object serviceInfoObj = entry.getValue();
                    if (serviceInfoObj == null || !(serviceInfoObj instanceof Map))
                    {
                        logger.error("serviceInfoObj is null or not map. The serviceName is: " + serviceName);
                        continue;
                    }
                    Map<String, Object> serviceInfo = (Map<String, Object>)serviceInfoObj;
                    DeployServiceInfoData serviceInfoData = new DeployServiceInfoDataImpl();
                    serviceInfoData.loadFromMap(serviceInfo);
                    serviceInfoDataMap.put(serviceName, serviceInfoData);
                }
            }
        }
        if (groupBaseInfoMap.containsKey(FEATURES_KEY))
        {
            Object featuresMapObj = groupBaseInfoMap.remove(FEATURES_KEY);
            if (featuresMapObj == null || !(featuresMapObj instanceof Map))
            {
                logger.error("featuresMapObj is null or not a map");
            }
            else
            {
                groupFeatureInfoMap.clear();
                Map<String, Object> featuresMap = (Map<String, Object>) featuresMapObj;
                for(Map.Entry<String, Object> entry : featuresMap.entrySet())
                {
                    String featureName = entry.getKey();
                    Object featureObj = entry.getValue();
                    if (featureObj == null || !(featureObj instanceof Map))
                    {
                        logger.error("featureObj is null or not map. The featureName is: " + featureName);
                        continue;
                    }
                    Map<String, Object> featureInfo = (Map<String, Object>)featureObj;
                    DeployFeatureInfoData featureInfoData = new DeployFeatureInfoDataImpl();
                    featureInfoData.loadFromMap(featureInfo);
                    groupFeatureInfoMap.put(featureName, featureInfoData);
                }
            }
        }
    }

    @Override
    public Map<String, Object> createMap()
    {
        if (serviceInfoDataMap == null || serviceInfoDataMap.isEmpty())
        {
            return null;
        }
        Map<String, Object> deployGroupInfoDataMap = new HashMap<String, Object>();
        if (groupBaseInfoMap != null && !groupBaseInfoMap.isEmpty())
        {
            deployGroupInfoDataMap.putAll(groupBaseInfoMap);
        }
        groupBaseInfoMap.put(DEVICES_KEY, serviceInfoDataMap);
        if (groupFeatureInfoMap != null && !groupFeatureInfoMap.isEmpty())
        {
            groupBaseInfoMap.put(FEATURES_KEY, groupFeatureInfoMap);
        }
        return deployGroupInfoDataMap;
    }
    
    @Override
    public void compareGroupInfoData(DeployGroupInfoData oldGroupInfoData, Map<String, DeployServiceInfoData> addServiceInfoDataMap, 
                                Map<String, DeployServiceInfoData> delServiceInfoDataMap)
    {
        if (addServiceInfoDataMap == null || delServiceInfoDataMap == null)
        {
            logger.error("addServiceInfoDataMap or delServiceInfoDataMap should not be null");
            return;
        }
        
        if (oldGroupInfoData == null)
        {
            oldGroupInfoData = new DeployGroupInfoDataImpl();
        }
        
        if (!(oldGroupInfoData instanceof DeployGroupInfoDataImpl))
        {
            logger.error("compareGroupInfoData other is not DeployGroupInfoDataImpl");
            return;
        }
        DeployGroupInfoDataImpl oldGroupInfoDataImpl = (DeployGroupInfoDataImpl) oldGroupInfoData;
        Map<String, DeployServiceInfoData> oldServiceInfoDataMap = oldGroupInfoDataImpl.serviceInfoDataMap;
        
        for(Map.Entry<String, DeployServiceInfoData> entry : serviceInfoDataMap.entrySet())
        {
            String serviceName = entry.getKey();
            DeployServiceInfoData serviceInfoData = entry.getValue();
            
            if (serviceInfoData == null)
            {
                logger.error("serviceInfoData should not be null");
                serviceInfoDataMap.remove(serviceName);
                continue;
            }
            if (oldServiceInfoDataMap.containsKey(serviceName))
            {
                DeployServiceInfoData oldServiceInfoData = oldServiceInfoDataMap.get(serviceName);
                if (oldServiceInfoData == null)
                {
                    logger.error("oldServiceInfoData should not be null");
                    oldServiceInfoDataMap.remove(serviceName);
                    addServiceInfoDataMap.put(serviceName, serviceInfoData);
                    continue;
                }
                else
                {
                    boolean ret = MapUtil.compareMap(serviceInfoData.createMap(), oldServiceInfoData.createMap());
                    if (!ret)
                    {
                        addServiceInfoDataMap.put(serviceName, serviceInfoData);
                        delServiceInfoDataMap.put(serviceName, oldServiceInfoData);
                        logger.info("Update the serviceInfoData for service " + serviceName);
                    }
                }
            }
            else
            {
                addServiceInfoDataMap.put(serviceName, serviceInfoData);
                logger.info("Add the serviceInfoData for service " + serviceName);
            }
        }
        
        for (String serviceName : oldServiceInfoDataMap.keySet())
        {
            if (!serviceInfoDataMap.containsKey(serviceName))
            {
                delServiceInfoDataMap.put(serviceName, oldServiceInfoDataMap.get(serviceName));
                logger.info("Delete the serviceInfoData for service " + serviceName);
            }
        }
        
//        logger.info("newGroupInfoData is " + this);
//        logger.info("oldGroupInfoData is " + oldGroupInfoData);
//        logger.info("addServiceInfoDataMap " + addServiceInfoDataMap);
//        logger.info("delServiceInfoDataMap " + delServiceInfoDataMap);
    }
    

    @Override
    public Map<String, Object> getGroupInfoDataByServiceList(Set<String> serviceNameList)
    {
        if (serviceNameList == null || serviceNameList.isEmpty())
        {
            return null;
        }
        Map<String, Object> groupInfoDataMapTmp = new HashMap<String, Object>();
        Map<String, Object> serviceInfoDataMapTmp = new HashMap<String, Object>();
        for(String serviceName : serviceNameList)
        {
            DeployServiceInfoData serviceInfoData = serviceInfoDataMap.get(serviceName);
            if (serviceInfoData == null)
            {
                logger.error("serviceInfoData is null. serviceName: " + serviceName);
                continue;
            }
            serviceInfoDataMapTmp.put(serviceName, serviceInfoData.createMap());
        }
        groupInfoDataMapTmp.put(DEVICES_KEY, serviceInfoDataMapTmp);
        if (groupFeatureInfoMap != null && !groupFeatureInfoMap.isEmpty())
        {
            groupInfoDataMapTmp.put(FEATURES_KEY, createMapForGroupFeatureInfoMap());
        }
        groupInfoDataMapTmp.putAll(groupBaseInfoMap);
        
        return groupInfoDataMapTmp;
    }

    @Override
    public String getServiceGroupName()
    {
        return this.serviceGroupName;
    }
    
    @Override
    public List<String> getFeatureNameList()
    {
        List<String> featureNameList = new ArrayList<String>();
        for(String featureaName : groupFeatureInfoMap.keySet())
        {
            featureNameList.add(featureaName);
        }
        return featureNameList;
    }
    
    @Override
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        ret.append("serviceGroup name is: " + serviceGroupName).append("; ")
           .append("serviceGroup info map is: " + createMap());
        
        return ret.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.serviceGroupName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DeployGroupInfoDataImpl))
        {
            return false;
        }
        DeployGroupInfoDataImpl other = (DeployGroupInfoDataImpl)obj;
        if(!this.serviceGroupName.equals(other.serviceGroupName))
        {
            return false;
        }
        if(MapUtil.compareMap(this.createMap(), other.createMap()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private Map<String, Object> createMapForGroupFeatureInfoMap()
    {
        if (groupFeatureInfoMap == null || groupFeatureInfoMap.isEmpty())
        {
            return null;
        }
        Map<String, Object> groupFeatureInfoMapTmp = new HashMap<String, Object>();
        for(Map.Entry<String, DeployFeatureInfoData> entry : groupFeatureInfoMap.entrySet())
        {
            String featureName = entry.getKey();
            DeployFeatureInfoData featureInfo = entry.getValue();
            if (featureInfo == null)
            {
                logger.error("featureInfo should not be null");
                continue;
            }
            groupFeatureInfoMapTmp.put(featureName, featureInfo.createMap());
        }
        return groupFeatureInfoMapTmp;
    }
}
