package yanbinwa.iDeploy.data;

import java.util.HashMap;
import java.util.Map;

public class DeployServiceInfoDataImpl implements DeployServiceInfoData
{

    private String name = null;
    
    private String user = null;
    
    private String password = null;
    
    private String ip = null;
    
    private int port = -1;
    
    private String rootUrl = null;
    
    private String command = null;
    
    private String dockerImage = null;
    
    private String dockerNet = null;
    
    private String ansibleFile = null;
    
    private String applicationFile = null;
    
    
    public DeployServiceInfoDataImpl()
    {
        
    }
    
    @Override
    public void loadFromMap(Map<String, Object> map)
    {
        name = (String) map.get(DEVICE_NAME_KEY);
        user = (String) map.get(DEVICE_USER_KEY);
        password = (String) map.get(DEVICE_PASSWORD_KEY);
        ip = (String) map.get(DEVICE_IP_KEY);
        port = (Integer) map.get(DEVICE_PORT_KEY);
        rootUrl = (String) map.get(DEVICE_ROOTURL_KEY);
        command = (String) map.get(DEVICE_COMMAND_KEY);
        dockerImage = (String) map.get(DEVICE_DEOCKERIMAGE_KEY);
        dockerNet = (String) map.get(DEVICE_DEOCKERNET_KEY);
        ansibleFile = (String) map.get(DEVICE_ANSIBLEFILE_KEY);
        applicationFile = (String) map.get(DEVICE_APPLICATIONFILE_KEY);
    }

    @Override
    public Map<String, Object> createMap()
    {
        Map<String, Object> deployServiceInfoDataMap = new HashMap<String, Object>();
        deployServiceInfoDataMap.put(DEVICE_NAME_KEY, name);
        deployServiceInfoDataMap.put(DEVICE_USER_KEY, user);
        deployServiceInfoDataMap.put(DEVICE_PASSWORD_KEY, password);
        deployServiceInfoDataMap.put(DEVICE_IP_KEY, ip);
        deployServiceInfoDataMap.put(DEVICE_PORT_KEY, port);
        deployServiceInfoDataMap.put(DEVICE_ROOTURL_KEY, rootUrl);
        deployServiceInfoDataMap.put(DEVICE_COMMAND_KEY, command);
        deployServiceInfoDataMap.put(DEVICE_DEOCKERIMAGE_KEY, dockerImage);
        deployServiceInfoDataMap.put(DEVICE_DEOCKERNET_KEY, dockerNet);
        deployServiceInfoDataMap.put(DEVICE_ANSIBLEFILE_KEY, ansibleFile);
        deployServiceInfoDataMap.put(DEVICE_APPLICATIONFILE_KEY, applicationFile);
        return deployServiceInfoDataMap;
    }
    
    @Override
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public void setUser(String user)
    {
        this.user = user;
    }
    
    @Override
    public String getUser()
    {
        return user;
    }
    
    @Override
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    @Override
    public String getPassword()
    {
        return password;
    }
    
    @Override
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    
    @Override
    public String getIp()
    {
        return ip;
    }
    
    @Override
    public void setPort(int port)
    {
        this.port = port;
    }
    
    @Override
    public int getPort()
    {
        return port;
    }
    
    @Override
    public void setRootUrl(String rootUrl)
    {
        this.rootUrl = rootUrl;
    }
    
    @Override
    public String getRootUrl()
    {
        return rootUrl;
    }
    
    @Override
    public void setCommand(String command)
    {
        this.command = command;
    }
    
    @Override
    public String getCommand()
    {
        return command;
    }
    
    @Override
    public void setDockerImage(String dockerImage)
    {
        this.dockerImage = dockerImage;
    }
    
    @Override
    public String getDockerImage()
    {
        return dockerImage;
    }
    
    @Override
    public void setDockerNet(String dockerNet)
    {
        this.dockerNet = dockerNet;
    }
    
    @Override
    public String getDockerNet()
    {
        return dockerNet;
    }
    
    @Override
    public void setAnsibleFile(String ansibleFile)
    {
        this.ansibleFile = ansibleFile;
    }
    
    @Override
    public String getAnsibleFile()
    {
        return ansibleFile;
    }
    
    @Override
    public void setApplicationFile(String applicationFile)
    {
        this.applicationFile = applicationFile;
    }
    
    @Override
    public String getApplicationFile()
    {
        return applicationFile;
    }

    @Override
    public String toString()
    {        
        StringBuilder ret = new StringBuilder();
        ret.append("name is: " + name).append("; ")
           .append("user is: " + user).append("; ")
           .append("password is: " + password).append("; ")
           .append("ip is: " + ip).append("; ")
           .append("port is: " + port).append("; ")
           .append("rootUrl is: " + rootUrl).append("; ")
           .append("command is: " + command).append("; ")
           .append("dockerImage is: " + dockerImage).append("; ")
           .append("dockerNet is: " + dockerNet).append("; ")
           .append("ansibleFile is: " + ansibleFile).append("; ")
           .append("applicationFile is: " + applicationFile);
        
        return ret.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
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
        if(this.toString().equals(other.toString()))
        {
            return false;
        }
        else
        {
            return false;
        }
    }
    
}
