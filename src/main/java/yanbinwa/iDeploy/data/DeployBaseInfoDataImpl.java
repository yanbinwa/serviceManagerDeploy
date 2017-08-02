package yanbinwa.iDeploy.data;

import java.util.HashMap;
import java.util.Map;

public class DeployBaseInfoDataImpl implements DeployBaseInfoData
{
    private String deployServerIp = null;
    
    private String deployServerUsername = null;
    
    private String deployServerPassword = null;
    
    private String rootPath = null;
    
    private String ansibleHost = null;
    
    private String ansibleMain = null;
    
    private String dockerContainer = null;
    
    private String manifestDir = null;
        
    private String deployInstallScript = null;
    
    private String deployUninstallScript = null;
    
    public DeployBaseInfoDataImpl()
    {
        
    }
    
    public DeployBaseInfoDataImpl(String deployServerIp, String deployServerUsername, String deployServerPassword,
                    String rootPath, String ansibleHost, String ansibleMain, String dockerContainer, 
                    String manifestDir, String deployInstallScript, String deployUninstallScript)
    {
        this.deployServerIp = deployServerIp;
        this.deployServerUsername = deployServerUsername;
        this.deployServerPassword = deployServerPassword;
        this.rootPath = rootPath;
        this.ansibleHost = ansibleHost;
        this.ansibleMain = ansibleMain;
        this.dockerContainer = dockerContainer;
        this.manifestDir = manifestDir;
        this.deployInstallScript = deployInstallScript;
        this.deployUninstallScript = deployUninstallScript;
    }
    
    @Override
    public void setDeployServerIp(String deployServerIp)
    {
        this.deployServerIp = deployServerIp;
    }
    
    @Override
    public String getDeployServerIp()
    {
        return this.deployServerIp;
    }
    
    @Override
    public void setDeployServerUsername(String deployServerUsername)
    {
        this.deployServerUsername = deployServerUsername;
    }
    
    @Override
    public String getDeployServerUsername()
    {
        return this.deployServerUsername;
    }
    
    @Override
    public void setDeployServerPassword(String deployServerPassword)
    {
        this.deployServerPassword = deployServerPassword;
    }
    
    @Override
    public String getDeployServerPassword()
    {
        return this.deployServerPassword;
    }
    
    @Override
    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }
    
    @Override
    public String getRootPath()
    {
        return this.rootPath;
    }
    
    @Override
    public void setAnsibleHost(String ansibleHost)
    {
        this.ansibleHost = ansibleHost;
    }
    
    @Override
    public String getAnsibleHost()
    {
        return this.ansibleHost;
    }
    
    @Override
    public void setAnsibleMain(String ansibleMain)
    {
        this.ansibleMain = ansibleMain;
    }
    
    @Override
    public String getAnsibleMain()
    {
        return this.ansibleMain;
    }
    
    @Override
    public void setDockerContainer(String dockerContainer)
    {
        this.dockerContainer = dockerContainer;
    }
    
    @Override
    public String getDockerContainer()
    {
        return this.dockerContainer;
    }
    
    @Override
    public void setManifestDir(String manifestDir)
    {
        this.manifestDir = manifestDir;
    }
    
    @Override
    public String getManifestDir()
    {
        return this.manifestDir;
    }
    
    @Override
    public void setDeployInstallScript(String deployInstallScript)
    {
        this.deployInstallScript = deployInstallScript;
    }
    
    @Override
    public String getDeployInstallScript()
    {
        return this.deployInstallScript;
    }
    
    @Override
    public void setDeployUninstallScript(String deployUninstallScript)
    {
        this.deployUninstallScript = deployUninstallScript;
    }
    
    @Override
    public String getDeployUninstallScript()
    {
        return this.deployUninstallScript;
    }

    @Override
    public void loadFromMap(Map<String, Object> map)
    {
        this.deployServerIp = (String) map.get(DeployBaseInfoData.DEPLOY_SERVER_IP);
        this.deployServerUsername = (String) map.get(DeployBaseInfoData.DEPLOY_SERVER_USERNAME);
        this.deployServerPassword = (String) map.get(DeployBaseInfoData.DEPLOY_SERVER_PASSWORD);
        this.rootPath = (String) map.get(DeployBaseInfoData.ROOT_PATH);
        this.ansibleHost = (String) map.get(DeployBaseInfoData.ANSIBLE_HOST);
        this.ansibleMain = (String) map.get(DeployBaseInfoData.ANSIBLE_MAIN);
        this.dockerContainer = (String) map.get(DeployBaseInfoData.DOCKER_CONTAINER);
        this.manifestDir = (String) map.get(DeployBaseInfoData.MANIFEST_DIR);
        this.deployInstallScript = (String) map.get(DeployBaseInfoData.DEPLOY_INSTALL_SCRIPT);
        this.deployUninstallScript = (String) map.get(DeployBaseInfoData.DEPLOY_UNINSTALL_SCRIPT);
    }

    @Override
    public Map<String, Object> createMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DeployBaseInfoData.DEPLOY_SERVER_IP, deployServerIp);
        map.put(DeployBaseInfoData.DEPLOY_SERVER_USERNAME, deployServerUsername);
        map.put(DeployBaseInfoData.DEPLOY_SERVER_PASSWORD, deployServerPassword);
        map.put(DeployBaseInfoData.ROOT_PATH, rootPath);
        map.put(DeployBaseInfoData.ANSIBLE_HOST, ansibleHost);
        map.put(DeployBaseInfoData.ANSIBLE_MAIN, ansibleMain);
        map.put(DeployBaseInfoData.DOCKER_CONTAINER, dockerContainer);
        map.put(DeployBaseInfoData.MANIFEST_DIR, manifestDir);
        map.put(DeployBaseInfoData.DEPLOY_INSTALL_SCRIPT, deployInstallScript);
        map.put(DeployBaseInfoData.DEPLOY_UNINSTALL_SCRIPT, deployUninstallScript);
        return map;
    }
    
    @Override
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        ret.append("deployServerIp : " + deployServerIp).append("; ")
           .append("deployServerUsername : " + deployServerUsername).append("; ")
           .append("deployServerPassword : " + deployServerPassword).append("; ")
           .append("rootPath : " + rootPath).append("; ")
           .append("ansibleHost : " + ansibleHost).append("; ")
           .append("ansibleMain : " + ansibleMain).append("; ")
           .append("dockerContainer : " + dockerContainer).append("; ")
           .append("manifestDir : " + manifestDir).append("; ")
           .append("deployInstallScript : " + deployInstallScript).append("; ")
           .append("deployUninstallScript : " + deployUninstallScript);
        
        return ret.toString();
    }
    
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DeployBaseInfoDataImpl))
        {
            return false;
        }
        DeployBaseInfoDataImpl other = (DeployBaseInfoDataImpl)obj;
        if(this.toString().equals(other.toString()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
