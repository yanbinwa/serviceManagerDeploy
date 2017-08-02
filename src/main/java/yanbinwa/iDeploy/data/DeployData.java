package yanbinwa.iDeploy.data;

import java.util.Map;

public interface DeployData
{
    public void loadFromMap(Map<String, Object> map);
    
    public Map<String, Object> createMap();
}
