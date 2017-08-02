package yanbinwa.iDeploy.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import yanbinwa.common.configClient.ConfigCallBack;
import yanbinwa.common.configClient.ConfigClient;
import yanbinwa.common.configClient.ConfigClientImpl;
import yanbinwa.common.configClient.ServiceConfigState;
import yanbinwa.common.constants.CommonConstants;
import yanbinwa.common.http.HttpMethod;
import yanbinwa.common.http.HttpResult;
import yanbinwa.common.ssh.RemoteShellExecutor;
import yanbinwa.common.ssh.ScpClient;
import yanbinwa.common.ssh.SshResult;
import yanbinwa.common.utils.HttpUtil;
import yanbinwa.common.utils.JsonUtil;
import yanbinwa.common.utils.MapUtil;
import yanbinwa.common.utils.YamlUtil;
import yanbinwa.common.zNodedata.ZNodeServiceData;
import yanbinwa.common.zNodedata.ZNodeServiceDataImpl;
import yanbinwa.iDeploy.data.DeployBaseInfoData;
import yanbinwa.iDeploy.data.DeployBaseInfoDataImpl;
import yanbinwa.iDeploy.data.DeployFeatureInfoData;
import yanbinwa.iDeploy.data.DeployFeatureInfoDataImpl;
import yanbinwa.iDeploy.data.DeployGroupInfoData;
import yanbinwa.iDeploy.data.DeployGroupInfoDataImpl;
import yanbinwa.iDeploy.data.DeployServiceInfoData;
import yanbinwa.iDeploy.task.DeployTask;
import yanbinwa.iDeploy.task.DeployTaskImpl;

/**
 * 
 * 调用ansible来进行部署，这个服务就是限定在主机上部署的，调用CLI来进行，如果当前正在进行部署任务时，会阻塞其他的部署服务
 * 或者可以设置线程池来并行部署，但是同一个service不能部署两遍。这里可以将Deploy分为两步，第一步是部署基础设施，包括zookeeper
 * Config service和Deploy service，第二步是通过deploy server把其余的服务部署起来
 * 
 * Deployservice需要知道真正run ansible的server的信息，即用户名和密码，这个信息可以预存在application.yml中
 * 
 * 对于Deploy信息按照Group level和service level
 * 
 * manifest中只存放初始的配置，之后通过API来添加和删除（先这样处理）
 * 
 * 对于添加一个服务时，需要提供group信息，group的feature信息，还有service信息
 * 
 * 通过Task来驱动，当出现job时，会生成一个task，写入到队列中，这样就可以顺序执行，task包含了manifest文件中的内容，以及需要删除的service的信息
 * 
 * 需要将当前的Deploy信息存放到本地或者zookeeper中？？？
 * 
 * @author yanbinwa
 *
 */

@Service("deployService")
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "serviceProperties")
public class DeployServiceImpl implements DeployService
{

    private static final Logger logger = Logger.getLogger(DeployServiceImpl.class);
    
    private Map<String, String> serviceDataProperties;
    private Map<String, String> zNodeInfoProperties;
    
    public void setServiceDataProperties(Map<String, String> properties)
    {
        this.serviceDataProperties = properties;
    }
    
    public Map<String, String> getServiceDataProperties()
    {
        return this.serviceDataProperties;
    }
    
    public void setZNodeInfoProperties(Map<String, String> properties)
    {
        this.zNodeInfoProperties = properties;
    }
    
    public Map<String, String> getZNodeInfoProperties()
    {
        return this.zNodeInfoProperties;
    }
    
    private ZNodeServiceData serviceData = null;
    private String zookeeperHostIp = null;
    private ConfigClient configClient = null;
    private ConfigCallBack configCallBack = new DeployConfigCallBack();
    
    private DeployBaseInfoData deployBaseInfoData = new DeployBaseInfoDataImpl();
    private Map<String, DeployFeatureInfoData> commonFeaturesMap = new HashMap<String, DeployFeatureInfoData>();
    private Map<String, DeployGroupInfoData> groupInfoDataMap = new HashMap<String, DeployGroupInfoData>();
    
    private boolean isRunning = false;
    private boolean isConfiged = false;
    
    private ReentrantLock lock = new ReentrantLock();
    
    private BlockingQueue<DeployTask> deployTaskQueue = new LinkedBlockingQueue<DeployTask>();
    
    private Thread deployTaskThread = null;
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        zookeeperHostIp = zNodeInfoProperties.get(CommonConstants.ZOOKEEPER_HOSTPORT_KEY);
        if (zookeeperHostIp == null)
        {
            logger.error("Zookeeper host and port should not be null");
            return;
        }
        
        String serviceGroupName = serviceDataProperties.get(CommonConstants.SERVICE_SERVICEGROUPNAME);
        String serviceName = serviceDataProperties.get(CommonConstants.SERVICE_SERVICENAME);
        String ip = serviceDataProperties.get(CommonConstants.SERVICE_IP);
        String portStr = serviceDataProperties.get(CommonConstants.SERVICE_PORT);
        int port = Integer.parseInt(portStr);
        String rootUrl = serviceDataProperties.get(CommonConstants.SERVICE_ROOTURL);
        serviceData = new ZNodeServiceDataImpl(ip, serviceGroupName, serviceName, port, rootUrl);
        
        configClient = new ConfigClientImpl(serviceData, configCallBack, zookeeperHostIp, zNodeInfoProperties);
        start();
    }

    @Override
    public void start()
    {
        if(!isRunning)
        {
            isRunning = true;
            configClient.start();
            logger.info("Start deploy service ...");
        }
        else
        {
            logger.info("Deploy service has already started ...");
        }
    }

    @Override
    public void stop()
    {
        if(isRunning)
        {
            isRunning = false;
            configClient.stop();
            logger.info("Stop deploy service ...");
        }
        else
        {
            logger.info("Deploy service has already stopped ...");
        }
    }
    
    @Override
    public void startWork()
    {
        logger.info("Start work deploy serivce ...");
        init();
        deployTaskThread = new Thread(new Runnable() {

            @Override
            public void run()
            {
                handerDeployTask();
            }
            
        });
        deployTaskThread.start();
    }

    @Override
    public void stopWork()
    {
        logger.info("Stop work deploy serivce ...");
        if (deployTaskThread != null)
        {
            deployTaskThread.interrupt();
            deployTaskThread = null;
        }
        reset();
    }
    
    private void init()
    {
        
    }
    
    /**
     * deploy servie shutdown之后，但是其它的服务并没有被删除，这时如何处理，可以在deploy addService时做判断，如果发现当前docker存在，就避开这个deploy
     * 
     */
    private void reset()
    {
        deployBaseInfoData = new DeployBaseInfoDataImpl();
        commonFeaturesMap.clear();
        groupInfoDataMap.clear();
    }
    
    /**
     * 
     * 这里deploy的task需要串行执行的，否则会有冲突的
     * 
     */
    private void handerDeployTask()
    {
        logger.info("Starting handler deploy task... ");
        while(isRunning)
        {
            try
            {
                DeployTask task = deployTaskQueue.poll(DEPLOY_TASK_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
                if (task == null)
                {
                    continue;
                }
                logger.trace("The task is: " + task);
                runDepoyTask(task);
            } 
            catch (InterruptedException e)
            {
                if (!isRunning)
                {
                    logger.info("Stop this thread");
                }
                else
                {
                    logger.error(e.getMessage());
                }
            }
        }
    }
    
    /**
     * 这里的流程如下：
     * 
     * 1. 向需要删除的device发送stopManageService
     * 
     * 2. 删除需要删除的device的docker容器
     * 
     * 3. deploy新的docker容器
     * 
     * @param task
     */
    private void runDepoyTask(DeployTask task)
    {
        if (task == null)
        {
            logger.error("task should not be null");
            return;
        }
        stopDeleteService(task);
        removeDeleteServiceDocker(task);
        deployAddService(task);
    }
    
    private void stopDeleteService(DeployTask task)
    {
        logger.info("start stopDeleteService ...");
        Map<String, Map<String, DeployServiceInfoData>> delServiceDataMap = task.getDelServiceDataMap();
        if (delServiceDataMap == null || delServiceDataMap.isEmpty())
        {
            logger.info("delServiceDataMap is null or empty.");
            return;
        }
        
        for (Map.Entry<String, Map<String, DeployServiceInfoData>> entry : delServiceDataMap.entrySet())
        {
            String serviceGroupName = entry.getKey();
            Map<String, DeployServiceInfoData> serviceNameToServiceDataMap = entry.getValue();
            if (serviceNameToServiceDataMap == null || serviceNameToServiceDataMap.isEmpty())
            {
                logger.error("serviceNameToServiceDataMap is null or empty. serviceGroupName is " + serviceGroupName);
                continue;
            }
            for(Map.Entry<String, DeployServiceInfoData> entry1 : serviceNameToServiceDataMap.entrySet())
            {
                String serviceName = entry1.getKey();
                DeployServiceInfoData serviceData = entry1.getValue();
                if (serviceData == null)
                {
                    logger.error("serviceData is null or empty. serviceGroupName is " + serviceGroupName + "; serviceName is " + serviceName);
                    continue;
                }
                stopService(serviceData);
                logger.info("stop the service " + serviceName);
            }
        }
    }
    
    private void stopService(DeployServiceInfoData serviceData)
    {
        String ip = serviceData.getIp();
        int port = serviceData.getPort();
        String rootUrl = serviceData.getRootUrl();
        String url = HTTP_HEAD + ip + ":" + port + "/" + rootUrl + "/" + STOP_MANAGER_SERVICE_ACTION;
        HttpResult ret = HttpUtil.httpRequest(url, null, HttpUtil.getHttpMethodStr(HttpMethod.POST));
        logger.trace("result status is " + ret.getStateCode() + "; response is " + ret.getResponse());
    }
    
    private void removeDeleteServiceDocker(DeployTask task)
    {
        logger.info("start removeDeleteServiceDocker ...");
        List<String> delServiceList = new ArrayList<String>();
        Map<String, Map<String, DeployServiceInfoData>> delServiceDataMap = task.getDelServiceDataMap();
        if (delServiceDataMap == null || delServiceDataMap.isEmpty())
        {
            logger.info("delServiceDataMap is null or empty.");
            return;
        }
        
        for (Map.Entry<String, Map<String, DeployServiceInfoData>> entry : delServiceDataMap.entrySet())
        {
            String serviceGroupName = entry.getKey();
            Map<String, DeployServiceInfoData> serviceNameToServiceDataMap = entry.getValue();
            if (serviceNameToServiceDataMap == null || serviceNameToServiceDataMap.isEmpty())
            {
                logger.error("serviceNameToServiceDataMap is null or empty. serviceGroupName is " + serviceGroupName);
                continue;
            }
            for(Map.Entry<String, DeployServiceInfoData> entry1 : serviceNameToServiceDataMap.entrySet())
            {
                String serviceName = entry1.getKey();
                DeployServiceInfoData serviceData = entry1.getValue();
                if (serviceData == null)
                {
                    logger.error("serviceData is null or empty. serviceGroupName is " + serviceGroupName + "; serviceName is " + serviceName);
                    continue;
                }
                delServiceList.add(serviceName);
            }
        }
        if (!delServiceList.isEmpty())
        {
            removeServices(delServiceList);
        }
    }
    
    private void removeServices(List<String> delServiceList)
    {
        if (delServiceList == null || delServiceList.isEmpty())
        {
            return;
        }
        StringBuilder serviceNameListStr = new StringBuilder();
        for (int i = 0; i < delServiceList.size(); i ++)
        {
            if (i != 0)
            {
                serviceNameListStr.append(",");
            }
            serviceNameListStr.append(delServiceList.get(i));
        }
        String rootPath = deployBaseInfoData.getRootPath();
        String uninstallScript = deployBaseInfoData.getDeployUninstallScript();
        String cmd = BIN_SHELL + " " + rootPath + uninstallScript + " " + serviceNameListStr;
        
        String deployServiceIp = deployBaseInfoData.getDeployServerIp();
        String deployServiceUsername = deployBaseInfoData.getDeployServerUsername();
        String deployServicePassword = deployBaseInfoData.getDeployServerPassword();
        RemoteShellExecutor executor = new RemoteShellExecutor(deployServiceIp, deployServiceUsername, deployServicePassword);
        try
        {
            executor.exec(cmd);
            logger.info("Remove service successful " + delServiceList);
        } 
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
    }
    
    private void deployAddService(DeployTask task)
    {
        logger.info("start deployAddService ...");
        Map<String, Object> addServiceInfoDataMap = task.getAddServiceInfoDataMap();
        adjustAddService(addServiceInfoDataMap);
        if (addServiceInfoDataMap == null || addServiceInfoDataMap.isEmpty())
        {
            logger.info("addServiceInfoDataMap is null or empty");
            return;
        }
        try
        {
            YamlUtil.setMapToFile(addServiceInfoDataMap, MANIFEST_FILE_TMP);
        } 
        catch (FileNotFoundException e)
        {
            logger.error("Fail the write map to file " + MANIFEST_FILE_TMP + "; the message is " + e.getMessage()); 
            return;
        }
        
        String deployServiceIp = deployBaseInfoData.getDeployServerIp();
        String deployServiceUsername = deployBaseInfoData.getDeployServerUsername();
        String deployServicePassword = deployBaseInfoData.getDeployServerPassword();
        
        ScpClient client = new ScpClient(deployServiceIp, deployServiceUsername, deployServicePassword);
        try
        {
            client.putFile(MANIFEST_FILE_TMP, MANIFEST_FILE_REMOTE_DIR);
        } 
        catch (IOException e)
        {
            logger.error("Can not scp the file " + MANIFEST_FILE_TMP + " to remote dir " + MANIFEST_FILE_REMOTE_DIR + "; the message is " + e.getMessage());
            return;
        }
        
        String rootPath = deployBaseInfoData.getRootPath();
        String installScript = deployBaseInfoData.getDeployInstallScript();
        String cmd = BIN_SHELL + " " + rootPath + installScript + " " + MANIFEST_FILE_REMOTE_PATH;
        RemoteShellExecutor executor = new RemoteShellExecutor(deployServiceIp, deployServiceUsername, deployServicePassword);
        try
        {
            logger.info("Depoly cmd is " + cmd);
            SshResult ret = executor.exec(cmd);
            logger.info("Deploy service successful. Reture is: " + ret);
        } 
        catch (Exception e)
        {
            logger.error("Fail to deploy the add service ; the message is " + e.getMessage());
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private void adjustAddService(Map<String, Object> addServiceInfoDataMap)
    {
        if (addServiceInfoDataMap == null || addServiceInfoDataMap.isEmpty())
        {
            return;
        }
        Map<String, Object> componentsMap = (Map<String, Object>) addServiceInfoDataMap.get(DEPLOY_COMPONENTS_KEY);
        if (componentsMap == null)
        {
            logger.error("componentsMap is null");
            return;
        }
        for (Map.Entry<String, Object> entry : componentsMap.entrySet())
        {
            String serviceGroupName = entry.getKey();
            Map<String, Object> serviceGroupInfoMap = (Map<String, Object>) entry.getValue();
            if (serviceGroupInfoMap == null)
            {
                logger.error("serviceGroupInfoMap is null for serviceGourp " + serviceGroupName);
                continue;
            }
            Map<String, Object> devicesInfoMap = (Map<String, Object>) serviceGroupInfoMap.get(DEPLOY_DEVICES_KEY);
            if (devicesInfoMap == null)
            {
                logger.error("devicesInfoMap is null for serviceGourp " + serviceGroupName);
                continue;
            }
            for (String serviceName : devicesInfoMap.keySet())
            {
                try
                {
                    if (isServiceExist(serviceName))
                    {
                        devicesInfoMap.remove(serviceName);
                        logger.info("service " + serviceName + " has already exist. Skip this service");
                    }
                } 
                catch (Exception e)
                {
                    logger.error(e.getMessage());
                    continue;
                }
            }
        }
    }
    
    private boolean isServiceExist(String serviceName) throws Exception
    {
        String deployServiceIp = deployBaseInfoData.getDeployServerIp();
        String deployServiceUsername = deployBaseInfoData.getDeployServerUsername();
        String deployServicePassword = deployBaseInfoData.getDeployServerPassword();
        
        String cmd = CHECK_SERVICE_EXIST_CMD + "\" " + serviceName + "$\""; 
        RemoteShellExecutor executor = new RemoteShellExecutor(deployServiceIp, deployServiceUsername, deployServicePassword);
        SshResult sshResult = executor.exec(cmd);
        String stdOut = sshResult.getStdOut();
        if (stdOut.trim().equals(""))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * 这里是对于当前的服务进行全局的更新，进行全部的对比. 如果在baseInfo和commons中出现不一致，对于原有的系统不做处理，如果component出现变动，才会生成新的task来进行deploy
     * 
     * @param serviceConfigPropertiesObj
     */
    @SuppressWarnings("unchecked")
    private void updateServiceConfigProperties(JSONObject serviceConfigPropertiesObj)
    {
        if (serviceConfigPropertiesObj == null)
        {
            logger.error("serviceConfigPropertiesObj should not be null");
            return;
        }
        else
        {
            logger.info("start to update the serviceConfigPropertiesObj " + serviceConfigPropertiesObj);
        }
        Map<String, Object> serviceConfigProperties = (Map<String, Object>) JsonUtil.JsonStrToMap(serviceConfigPropertiesObj.toString());
        
        Object baseInfoObj = serviceConfigProperties.get(DEPLOY_BASEINFO_KEY);
        if (baseInfoObj == null || !(baseInfoObj instanceof Map))
        {
            logger.error("baseInfoObject is null or not map. baseInfoObj: " + baseInfoObj);
            return;
        }
        Map<String, Object> baseInfoMap = (Map<String, Object>) baseInfoObj;
        
        Object commonsObj = serviceConfigProperties.get(DEPLOY_COMMONS_KEY);
        Map<String, Object> commonsMap = null;
        if (commonsObj == null)
        {
            logger.info("commonsObj is null. commonsMap will be empty");
            commonsMap = new HashMap<String, Object>();
        }
        else if (!(commonsObj instanceof Map))
        {
            logger.error("commonsObj is not map. commonsObj: " + commonsObj);
            return;
        }
        else
        {
            commonsMap = (Map<String, Object>) commonsObj;
        }
        
        Object componentsObj = serviceConfigProperties.get(DEPLOY_COMPONENTS_KEY);
        Map<String, Object> componentsMap = null;
        if (componentsObj == null)
        {
            logger.info("componentsObj is null. componentsMap will be empty");
        }
        else if (!(componentsObj instanceof Map))
        {
            logger.error("componentsObj is not map. componentsObj: " + componentsObj);
            return;
        }
        else
        {
            componentsMap = (Map<String, Object>) componentsObj;
        }
        
        lock.lock();
        try
        {
            updateDeployBaseInfoData(baseInfoMap);
            updateCommonFeaturesMap(commonsMap);
            updateGroupInfoDataMap(componentsMap);
        }
        finally
        {
            lock.unlock();
        }
        if (!isConfiged)
        {
            startWork();
            isConfiged = true;
        }
    }
    
    private void updateDeployBaseInfoData(Map<String, Object> baseInfoMap)
    {
        lock.lock();
        try
        {
            if (baseInfoMap == null)
            {
                logger.error("baseInfoMap should not be null");
                return;
            }
            
            boolean ret = MapUtil.compareMap(baseInfoMap, deployBaseInfoData.createMap());
            if (!ret)
            {
                deployBaseInfoData.loadFromMap(baseInfoMap);
                logger.info("deployBaseInfoData has reload from map " + baseInfoMap);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * 
     * 计算增加和删除的feature
     * 
     * @param commonsMap
     */
    @SuppressWarnings("unchecked")
    private void updateCommonFeaturesMap(Map<String, Object> commonsMap)
    {
        lock.lock();
        try
        {
            if (commonsMap == null)
            {
                logger.error("commonsMap should not be null");
                return;
            }
            Map<String, DeployFeatureInfoData> commonFeaturesMapCopy = new HashMap<String, DeployFeatureInfoData>(commonFeaturesMap);
            
            Map<String, DeployFeatureInfoData> addDeployFeatureInfoData = new HashMap<String, DeployFeatureInfoData>();
            List<String> delDeployFeatureInfoDataNames = new ArrayList<String>();
            for(Map.Entry<String, Object> entry : commonsMap.entrySet())
            {
                String featureName = entry.getKey();
                Object featureObj = entry.getValue();
                if (featureObj == null || !(featureObj instanceof Map))
                {
                    logger.error("featureObj is null or not Map. The featureName is: " + featureName + "; And the featureObj is: " + featureObj);
                    continue;
                }
                Map<String, Object> featureMap = (Map<String, Object>) featureObj;
                DeployFeatureInfoData newCommonFeature = new DeployFeatureInfoDataImpl();
                newCommonFeature.loadFromMap(featureMap);
                
                if (commonFeaturesMapCopy.containsKey(featureName))
                {
                    
                    DeployFeatureInfoData commonFeature = commonFeaturesMapCopy.get(featureName);
                    if (commonFeature == null)
                    {
                        addDeployFeatureInfoData.put(featureName, newCommonFeature);
                        delDeployFeatureInfoDataNames.add(featureName);
                        logger.info("commonFeature is null. Need to update the feature " + featureName);
                        continue;
                    }
                    boolean ret = MapUtil.compareMap(commonFeature.createMap(), featureMap);
                    if (!ret)
                    {
                        addDeployFeatureInfoData.put(featureName, newCommonFeature);
                        delDeployFeatureInfoDataNames.add(featureName);
                        logger.info("update the featureMap for " + featureName);
                    }
                }
                else
                {
                    addDeployFeatureInfoData.put(featureName, newCommonFeature);
                    logger.info("add the featureMap for " + featureName);
                }
            }
            
            for(String featureName : commonFeaturesMapCopy.keySet())
            {
                if (!commonsMap.containsKey(featureName))
                {
                    delDeployFeatureInfoDataNames.add(featureName);
                    logger.info("delete the featureMap for " + featureName);
                }
            }
            
            for (String featureName : delDeployFeatureInfoDataNames)
            {
                commonFeaturesMapCopy.remove(featureName);
            }
            
            commonFeaturesMapCopy.putAll(addDeployFeatureInfoData);
            commonFeaturesMap = commonFeaturesMapCopy;
            
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * 这里会创建deploy task
     * 
     * @param componentsMap
     */
    @SuppressWarnings("unchecked")
    private void updateGroupInfoDataMap(Map<String, Object> componentsMap)
    {
        lock.lock();
        try
        {
            if (componentsMap == null)
            {
                logger.error("componentsMap should not be null");
                return;
            }
            
            Map<String, DeployGroupInfoData> groupInfoDataMapCopy = new HashMap<String, DeployGroupInfoData>(groupInfoDataMap);
            Map<String, Map<String, DeployServiceInfoData>> addServiceInfoData = new HashMap<String, Map<String, DeployServiceInfoData>>();
            Map<String, Map<String, DeployServiceInfoData>> delServiceInfoData = new HashMap<String, Map<String, DeployServiceInfoData>>();
            
            for(Map.Entry<String, Object> entry : componentsMap.entrySet())
            {
                String serviceGroupName = entry.getKey();
                Object serviceGroupInfoDataObj = entry.getValue();
                if (serviceGroupInfoDataObj == null || !(serviceGroupInfoDataObj instanceof Map))
                {
                    logger.error("serviceGroupInfoDataObj is null or not Map. The serviceGroupName is: " + serviceGroupName + 
                            "; And the serviceGroupInfoDataObj is: " + serviceGroupInfoDataObj);
                    componentsMap.remove(serviceGroupName);
                    continue;
                }
                Map<String, Object> serviceGroupInfoData = (Map<String, Object>) serviceGroupInfoDataObj;
                DeployGroupInfoData newGroupInfoData = new DeployGroupInfoDataImpl(serviceGroupName);
                newGroupInfoData.loadFromMap(serviceGroupInfoData);
                
                DeployGroupInfoData oldGroupInfoData = groupInfoDataMapCopy.get(serviceGroupName);
                if (oldGroupInfoData == null)
                {
                    oldGroupInfoData = new DeployGroupInfoDataImpl();
                }
                
                Map<String, DeployServiceInfoData> addServiceInfoDataMap = new HashMap<String, DeployServiceInfoData>();
                Map<String, DeployServiceInfoData> delServiceInfoDataMap = new HashMap<String, DeployServiceInfoData>();
                
                newGroupInfoData.compareGroupInfoData(oldGroupInfoData, addServiceInfoDataMap, delServiceInfoDataMap);
                
                if (!addServiceInfoDataMap.isEmpty())
                {
                    Map<String, DeployServiceInfoData> addServiceInfoMap = addServiceInfoData.get(serviceGroupName);
                    if (addServiceInfoMap == null)
                    {
                        addServiceInfoMap = new HashMap<String, DeployServiceInfoData>();
                        addServiceInfoData.put(serviceGroupName, addServiceInfoMap);
                    }
                    addServiceInfoMap.putAll(addServiceInfoDataMap);
                }
                
                if (!delServiceInfoDataMap.isEmpty())
                {
                    Map<String, DeployServiceInfoData> delServiceInfoMap = delServiceInfoData.get(serviceGroupName);
                    if (delServiceInfoMap == null)
                    {
                        delServiceInfoMap = new HashMap<String, DeployServiceInfoData>();
                        delServiceInfoData.put(serviceGroupName, delServiceInfoMap);
                    }
                    delServiceInfoMap.putAll(delServiceInfoDataMap);
                }
                
                groupInfoDataMapCopy.put(serviceGroupName, newGroupInfoData);
            }
            
            for (Map.Entry<String, DeployGroupInfoData> entry : groupInfoDataMapCopy.entrySet())
            {
                String serviceGroupName = entry.getKey();
                DeployGroupInfoData oldGroupInfoData = entry.getValue();
                
                if (oldGroupInfoData == null)
                {
                    logger.error("oldGroupInfoData is null. The serviceGroupName is: " + serviceGroupName);
                    groupInfoDataMapCopy.remove(serviceGroupName);
                    continue;
                }
                
                if (!componentsMap.containsKey(serviceGroupName))
                {
                    Map<String, DeployServiceInfoData> addServiceInfoDataMap = new HashMap<String, DeployServiceInfoData>();
                    Map<String, DeployServiceInfoData> delServiceInfoDataMap = new HashMap<String, DeployServiceInfoData>();
                    
                    DeployGroupInfoData newGroupInfoData = new DeployGroupInfoDataImpl(serviceGroupName);
                    newGroupInfoData.compareGroupInfoData(oldGroupInfoData, addServiceInfoDataMap, delServiceInfoDataMap);
                    
                    if (!addServiceInfoDataMap.isEmpty())
                    {
                        Map<String, DeployServiceInfoData> addServiceInfoMap = addServiceInfoData.get(serviceGroupName);
                        if (addServiceInfoMap == null)
                        {
                            addServiceInfoMap = new HashMap<String, DeployServiceInfoData>();
                            addServiceInfoData.put(serviceGroupName, addServiceInfoMap);
                        }
                        addServiceInfoMap.putAll(addServiceInfoDataMap);
                    }
                    
                    if (!delServiceInfoDataMap.isEmpty())
                    {
                        Map<String, DeployServiceInfoData>  delServiceInfoMap = delServiceInfoData.get(serviceGroupName);
                        if (delServiceInfoMap == null)
                        {
                            delServiceInfoMap = new HashMap<String, DeployServiceInfoData>();
                            delServiceInfoData.put(serviceGroupName, delServiceInfoMap);
                        }
                        delServiceInfoMap.putAll(delServiceInfoDataMap);
                    }
                    
                    groupInfoDataMapCopy.remove(serviceGroupName);
                }
            }
            
            groupInfoDataMap = groupInfoDataMapCopy;
            
            if (!addServiceInfoData.isEmpty() || !delServiceInfoData.isEmpty())
            {
                createDeployTask(addServiceInfoData, delServiceInfoData);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * 这里的delServiceInfoData是需要有data信息的，包括Ip，port等，这样才能发送stopManager
     * 
     * @param addServiceInfoData
     * @param delServiceInfoData
     */
    @SuppressWarnings("unchecked")
    private void createDeployTask(Map<String, Map<String, DeployServiceInfoData>> addServiceInfoData, Map<String, Map<String, DeployServiceInfoData>> delServiceInfoData)
    {
        Map<String, Object> deployManifestMap = null;
        if (addServiceInfoData != null && !addServiceInfoData.isEmpty())
        {
            deployManifestMap = new HashMap<String, Object>();
            Map<String, Object> addServiceInfoDataMap = new HashMap<String, Object>();
            
            for(Map.Entry<String, Map<String, DeployServiceInfoData>> entry : addServiceInfoData.entrySet())
            {
                String serviceGroupName = entry.getKey();
                Map<String, DeployServiceInfoData> addServiceInfoMap = entry.getValue();
                if (addServiceInfoMap == null || addServiceInfoMap.isEmpty())
                {
                    logger.error("addServiceInfoMap shoud not be null or empty, serviceGroup is: " + serviceGroupName);
                    continue;
                }
                DeployGroupInfoData groupInfoData = groupInfoDataMap.get(serviceGroupName);
                if (groupInfoData == null)
                {
                    logger.error("groupInfoData should not be null. serviceGroup is: " + serviceGroupName);
                    continue;
                }
                Map<String, Object> addServiceInfoDataByGroup = groupInfoData.getGroupInfoDataByServiceList(addServiceInfoMap.keySet());
                if (addServiceInfoDataByGroup == null)
                {
                    logger.error("addServiceInfoDataByGroup is null. serviceGroup is: " + serviceGroupName);
                    continue;
                }
                addServiceInfoDataMap.put(serviceGroupName, addServiceInfoDataByGroup);
            }
            
            deployManifestMap.put(DEPLOY_COMPONENTS_KEY, addServiceInfoDataMap);
            
            if (deployBaseInfoData != null)
            {
                deployManifestMap.put(DEPLOY_BASEINFO_KEY, deployBaseInfoData.createMap());
            }
            
            // 这里需要填写每个feature中的serviceGroup信息
            if (commonFeaturesMap != null && !commonFeaturesMap.isEmpty())
            {
                Map<String, Object> addServiceInfoDataByGroup = (Map<String, Object>) deployManifestMap.get(DEPLOY_COMPONENTS_KEY);
                Map<String, List<String>> featureNameToGroupNameMap = getFeatureNameToGroupNameMap(addServiceInfoDataByGroup.keySet());
                if (featureNameToGroupNameMap == null || featureNameToGroupNameMap.isEmpty())
                {
                    logger.info("featureNameToGroupNameMap is null or empty");
                }
                else
                {
                    Map<String, Object> commonFeaturesMapTmp = new HashMap<String, Object>();
                    for (Map.Entry<String, DeployFeatureInfoData> entry : commonFeaturesMap.entrySet())
                    {
                        String featureName = entry.getKey();
                        DeployFeatureInfoData featureInfoData = entry.getValue();
                        if (featureInfoData == null)
                        {
                            logger.error("featureInfoData is null for feature : " + featureName);
                            continue;
                        }
                        List<String> groupNameList = featureNameToGroupNameMap.get(featureName);
                        if (groupNameList == null || groupNameList.isEmpty())
                        {
                            logger.info("groupNameList is null or empty for feature: " + featureName);
                            continue;
                        }
                        StringBuilder serviceGroupStr = new StringBuilder();
                        for(int i = 0; i < groupNameList.size(); i ++)
                        {
                            if (i != 0)
                            {
                                serviceGroupStr.append(",");
                            }
                            serviceGroupStr.append(groupNameList.get(i));
                        }
                        featureInfoData.setServiceGroup(serviceGroupStr.toString());
                        commonFeaturesMapTmp.put(featureName, featureInfoData.createMap());
                    }
                    if (!commonFeaturesMapTmp.isEmpty())
                    {
                        deployManifestMap.put(DEPLOY_COMMONS_KEY, commonFeaturesMapTmp);
                    }
                }
            }
        }
        DeployTask task = new DeployTaskImpl(delServiceInfoData, deployManifestMap);
        deployTaskQueue.offer(task);
    }
    
    private Map<String, List<String>> getFeatureNameToGroupNameMap(Set<String> serviceGroupNameList)
    {
        if (serviceGroupNameList == null || serviceGroupNameList.isEmpty())
        {
            logger.error("serviceGroupNameList is null or empty");
            return null;
        }
        logger.info("serviceGroupNameList " + serviceGroupNameList);
        Map<String, List<String>> featureNameToGroupNameMap = new HashMap<String, List<String>>();
        for(String serviceGroup : serviceGroupNameList)
        {
            DeployGroupInfoData groupInfoData = groupInfoDataMap.get(serviceGroup);
            if (groupInfoData == null)
            {
                logger.error("groupInfoData should not be null. The service group is: " + serviceGroup);
                continue;
            }
            List<String> featureNameList = groupInfoData.getFeatureNameList();
            logger.info("serviceGroup " + serviceGroup + " has the feature name list is " + featureNameList);
            for (String featureName : featureNameList)
            {
                List<String> groupNameList = featureNameToGroupNameMap.get(featureName);
                if (groupNameList == null)
                {
                    groupNameList = new ArrayList<String>();
                    featureNameToGroupNameMap.put(featureName, groupNameList);
                }
                groupNameList.add(serviceGroup);
            }
        }
        return featureNameToGroupNameMap;
    }
    
    class DeployConfigCallBack implements ConfigCallBack
    {

        @Override
        public void handleServiceConfigChange(ServiceConfigState state)
        {
            logger.info("Service config state is: " + state);
            if (state == ServiceConfigState.CREATED || state == ServiceConfigState.CHANGED)     //应该不会调用CHANGE
            {
                JSONObject serviceConfigPropertiesObj = configClient.getServiceConfigProperties();
                updateServiceConfigProperties(serviceConfigPropertiesObj);
            }
            else if (state == ServiceConfigState.DELETED || state == ServiceConfigState.CLOSE)
            {
                if (isConfiged)
                {
                    stopWork();
                }
                isConfiged = false;
            }
            else
            {
                logger.error("Unknow ServiceConfigState: " + state);
            }
        }
    }
}