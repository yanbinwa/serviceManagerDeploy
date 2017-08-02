package yanbinwa.iDeploy.data;

public interface DeployServiceInfoData extends DeployData
{
    public static final String DEVICE_NAME_KEY = "name";
    public static final String DEVICE_USER_KEY = "user";
    public static final String DEVICE_PASSWORD_KEY = "password";
    public static final String DEVICE_IP_KEY = "ip";
    public static final String DEVICE_PORT_KEY = "port"; 
    public static final String DEVICE_ROOTURL_KEY = "rootUrl";
    public static final String DEVICE_COMMAND_KEY = "command";
    public static final String DEVICE_DEOCKERIMAGE_KEY = "dockerImage";
    public static final String DEVICE_DEOCKERNET_KEY = "dockerNet";
    public static final String DEVICE_ANSIBLEFILE_KEY = "ansibleFile";
    public static final String DEVICE_APPLICATIONFILE_KEY = "applicationFile";
    
    public void setName(String name);
    
    public String getName();
    
    public void setUser(String user);
    
    public String getUser();
    
    public void setPassword(String password);
    
    public String getPassword();
    
    public void setIp(String ip);
    
    public String getIp();
    
    public void setPort(int port);
    
    public int getPort();
    
    public void setRootUrl(String rootUrl);
    
    public String getRootUrl();
    
    public void setCommand(String command);
    
    public String getCommand();
    
    public void setDockerImage(String dockerImage);
    
    public String getDockerImage();
    
    public void setDockerNet(String dockerNet);
    
    public String getDockerNet();
    
    public void setAnsibleFile(String ansibleFile);
    
    public String getAnsibleFile();
    
    public void setApplicationFile(String applicationFile);
    
    public String getApplicationFile();
}
