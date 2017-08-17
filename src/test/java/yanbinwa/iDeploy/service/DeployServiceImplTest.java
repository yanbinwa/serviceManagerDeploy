package yanbinwa.iDeploy.service;

import org.junit.Test;

import yanbinwa.common.ssh.RemoteShellExecutor;
import yanbinwa.common.ssh.SshResult;
import yanbinwa.iDeploy.constants.DeployConstantsTest;

public class DeployServiceImplTest
{

    @Test
    public void test() throws Exception
    {
        String deployServiceIp = DeployConstantsTest.TEST_SERVER_IP;
        String deployServiceUsername = "root";
        String deployServicePassword = "emotibot";
        
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
