package com.chayan.mulewa.nframework.server;
import com.chayan.mulewa.nframework.server.annotations.*;
import java.net.*;
import java.util.*;

import javax.swing.text.html.StyleSheet;

import java.awt.SystemTray;
import java.lang.reflect.*;
public class NFrameworkServer
{
    private ServerSocket serverSocket;
    private Socket socket;
    private RequestProcessor requestProcessor;
    
    private Class c;
    private Set<Class> tcpNetworkServiceClasses;
    private Map<String,TCPService> services;
    public NFrameworkServer()
    {
        this.services=new HashMap<>();
        this.tcpNetworkServiceClasses=new HashSet<>();
    }
    public void registerClass(Class c)
    {
        this.c=c;
        Path pathOnType=null;
        Path pathOnMethod=null;
        Method []methods;
        String fullPath="";
        TCPService tcpService;
        int methodWithoutPathAnnotaion=0;
        pathOnType=(Path)c.getAnnotation(Path.class);
        if(pathOnType==null)
        {
            return;
        }
        methods=c.getMethods();
        for(Method method:methods)
        {
            pathOnMethod=(Path)method.getAnnotation(Path.class);
            if(pathOnMethod==null)
            {
                continue;
            }
            methodWithoutPathAnnotaion++;
            fullPath=pathOnType.value()+pathOnMethod.value();
            tcpService=new TCPService();
            tcpService.c=c;
            tcpService.method=method;
            tcpService.path=fullPath;
            services.put(fullPath,tcpService);
        }
        if(methodWithoutPathAnnotaion>0)
        {
            this.tcpNetworkServiceClasses.add(c);
        }
    }
    public TCPService getTCPService(String path)
    {
        if(services.containsKey(path))
        {
            return services.get(path);
        }
        else
        {
            return null;
        }
    }
    public void start()
    {
        try
        {
            serverSocket=new ServerSocket(5500);
            while(true)
            {
                System.out.println("Server Is Started");
                socket=serverSocket.accept();
                requestProcessor=new RequestProcessor(this,socket);
            }    
        }catch (Exception e)
        {
            System.out.println("NFrameworkServer : "+e.getMessage());
        }
    }
}
