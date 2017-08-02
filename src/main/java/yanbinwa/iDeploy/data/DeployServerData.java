package yanbinwa.iDeploy.data;

public class DeployServerData 
{
    private String username = null;
    
    private String password = null;
    
    public DeployServerData()
    {
        
    }
    
    public DeployServerData(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getPassword()
    {
        return this.password;
    }
}
