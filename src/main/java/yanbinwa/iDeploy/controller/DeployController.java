package yanbinwa.iDeploy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import yanbinwa.common.exceptions.ServiceUnavailableException;
import yanbinwa.iDeploy.service.DeployService;

@RestController
@RequestMapping("/deploy")
public class DeployController
{
    @Autowired
    DeployService deployService;
    
    @RequestMapping(value="/getServiceName",method=RequestMethod.GET)
    public String getServiceName() throws ServiceUnavailableException
    {
        return deployService.getServiceName();
    }
    
    @RequestMapping(value="/startManageService",method=RequestMethod.POST)
    public void startManageService()
    {
        deployService.startManageService();
    }
    
    @RequestMapping(value="/stopManageService",method=RequestMethod.POST)
    public void stopManageService()
    {
        deployService.stopManageService();
    }
    
    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Config service is stop")
    @ExceptionHandler(ServiceUnavailableException.class)
    public void serviceUnavailableExceptionHandler() 
    {
        
    }
}
