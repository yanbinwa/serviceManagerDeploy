package yanbinwa.iDeploy.service;

import org.junit.Test;

import yanbinwa.common.ssh.RemoteShellExecutor;
import yanbinwa.common.ssh.SshResult;

public class DeployServiceImplTest
{

    @Test
    public void test() throws Exception
    {
        String deployServiceIp = "192.168.56.17";
        String deployServiceUsername = "root";
        String deployServicePassword = "Wyb13403408973";
        
        String serviceName = "config_standalone1";
        String cmd = DeployService.CHECK_SERVICE_EXIST_CMD + "\" " + serviceName + "$\""; 
        RemoteShellExecutor executor = new RemoteShellExecutor(deployServiceIp, deployServiceUsername, deployServicePassword);
        SshResult sshResult = executor.exec(cmd);
        String stdOut = sshResult.getStdOut();
        if (stdOut.trim().equals(""))
        {
            System.out.println("service " + serviceName + " is not exist");
        }
        else
        {
            System.out.println("service " + serviceName + " is exist");
        }
    }

}
