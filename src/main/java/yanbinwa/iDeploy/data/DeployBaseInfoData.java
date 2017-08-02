package yanbinwa.iDeploy.data;

public interface DeployBaseInfoData extends DeployData
{
    public static final String DEPLOY_SERVER_IP = "deploy_server_ip";
    public static final String DEPLOY_SERVER_USERNAME = "deploy_server_username";
    public static final String DEPLOY_SERVER_PASSWORD = "deploy_server_password";
    public static final String ROOT_PATH = "root_path";
    public static final String ANSIBLE_HOST = "ansible_host";
    public static final String ANSIBLE_MAIN = "ansible_main";
    public static final String DOCKER_CONTAINER = "docker_container";
    public static final String MANIFEST_DIR = "manifest_dir";
    public static final String DEPLOY_INSTALL_SCRIPT = "deploy_install_script";
    public static final String DEPLOY_UNINSTALL_SCRIPT = "deploy_uninstall_script";
    
    public void setDeployServerIp(String deployServerIp);
    
    public String getDeployServerIp();
    
    public void setDeployServerUsername(String deployServerUsername);
    
    public String getDeployServerUsername();
    
    public void setDeployServerPassword(String deployServerPassword);
    
    public String getDeployServerPassword();
    
    public void setRootPath(String rootPath);
    
    public String getRootPath();
    
    public void setAnsibleHost(String ansibleHost);
    
    public String getAnsibleHost();
    
    public void setAnsibleMain(String ansibleMain);
    
    public String getAnsibleMain();
    
    public void setDockerContainer(String dockerContainer);
    
    public String getDockerContainer();
    
    public void setManifestDir(String manifestDir);
    
    public String getManifestDir();
    
    public void setDeployInstallScript(String deployInstallScript);
    
    public String getDeployInstallScript();
    
    public void setDeployUninstallScript(String deployUninstallScript);
    
    public String getDeployUninstallScript();

}
