package com.chayan.mulewa.nframework.server;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.chayan.mulewa.nframework.common.*;
public class RequestProcessor extends Thread
{
    private NFrameworkServer nFrameworkServer;
    private Socket socket;
    RequestProcessor( NFrameworkServer nFrameworkServer,Socket socket)
    {
        this.nFrameworkServer=nFrameworkServer;
        this.socket=socket;
        start();
    }
    public void run()
    {
        try
        {
            InputStream is=socket.getInputStream();
            OutputStream os=socket.getOutputStream();
            int byteReadCount;
            int byteToReceive=1024;
            byte header[]=new byte[1024];
            byte tmp[]=new byte[1024];
            int k,i,j;
            i=0;
            j=0;
            while(j<byteToReceive)
            {
                byteReadCount=is.read(tmp);
                if(byteReadCount==-1)
                {
                    continue;
                }
                for(k=0;k<byteReadCount;k++)
                {
                    header[i]=tmp[k];
                    i++;
                }
                j=j+byteReadCount;
            }
            int requestLength=0;
            i=1;
            j=1023;
            while(j>=0)
            {
                requestLength=requestLength+(header[j]*i);
                i=i*10;
                j--;
            }
            byte ack[]=new byte[1];
            ack[0]=1;
            os.write(ack,0,1);
            os.flush();

            byte request[]=new byte[requestLength];
            byteToReceive=requestLength;
            i=0;
            j=0;
            while(j<byteToReceive)
            {
                byteReadCount=is.read(tmp);
                if(byteReadCount==-1)
                {
                    continue;
                }
                for(k=0;k<byteReadCount;k++)
                {
                    request[i]=tmp[k];
                    i++;
                }
                j=j+byteReadCount;
            }
            String requestJSONString=new String(request,StandardCharsets.UTF_8);
            Request requestObject=JSONUtil.fromJSON(requestJSONString,Request.class);

            Object arg[]=requestObject.getArguments();

            int sourceRow = ((Double) arg[0]).intValue();
            int sourceCol = ((Double) arg[1]).intValue();
            int targetRow = ((Double) arg[2]).intValue();
            int targetCol = ((Double) arg[3]).intValue();

            String servicePath=requestObject.getServicePath();
            TCPService tcpService=this.nFrameworkServer.getTCPService(servicePath);
            Response responseObject=new Response();
            if(tcpService==null)
            {
                responseObject.setSuccess(false);
                responseObject.setResult(null);
                responseObject.setException(new RuntimeException("Invalid Path : "+servicePath));
            }
            else
            {
                Class c=tcpService.c;
                Method method=tcpService.method;
                try 
                {
                    Constructor constructor = c.getDeclaredConstructor();
                    Object serviceObject = constructor.newInstance();
                    Object result = method.invoke(serviceObject,sourceRow,sourceCol,targetRow,targetCol);
                    
                    responseObject.setSuccess(true);
                    responseObject.setResult(result);
                    responseObject.setException(null);
                }catch (InstantiationException instantiationException)
                {
                    responseObject.setSuccess(false);
                    responseObject.setResult(null);
                    responseObject.setException(new RuntimeException("Unable To Create Object To Service Class Associated With Path : "+servicePath));
                    System.out.println("catch 1");
                }
                catch (IllegalAccessException illegalAccessException)
                {
                    System.out.println(illegalAccessException.getMessage());
                    responseObject.setSuccess(false);
                    responseObject.setResult(null);
                    responseObject.setException(new RuntimeException("Unable To Create Object To Service Class Associated With Path : "+servicePath));
                    System.out.println("catch 2");
                }
                catch (InvocationTargetException invocationTargetException)
                {
                    Throwable throwable=invocationTargetException.getCause();
                    System.out.println("InvocationTargetException : "+throwable.getMessage());
                    responseObject.setSuccess(false);
                    responseObject.setResult(null);
                    responseObject.setException(throwable);
                    System.out.println("catch 3");
                }
            }

            // responseObject.setResult(arg);
            // responseObject.setSuccess(true);
            // responseObject.setException(null);

            String responseJSONString=JSONUtil.toJSON(responseObject);
            byte objectBytes[]=responseJSONString.getBytes(StandardCharsets.UTF_8);
            requestLength=objectBytes.length;
            header=new byte[1024];
            int x=requestLength;
            i=1023;
            while(x>0)
            {
                header[i]=(byte)(x%10);
                x=x/10;
                i--;
            }
            os.write(header,0,1024);
            os.flush();

            while(true)
            {
                byteReadCount=is.read(ack);
                if(byteReadCount==-1)
                {
                    continue;
                }
                break;
            }

            int byteToSend=requestLength;
            int chunkSize=1024;
            j=0;
            while(j<byteToSend)
            {
                if((byteToSend-j)<chunkSize)
                {
                    chunkSize=byteToSend-j;
                }
                os.write(objectBytes,j,chunkSize);
                os.flush();
                j=j+chunkSize;
            }

            while(true)
            {
                byteReadCount=is.read(ack);
                if(byteReadCount==-1)
                {
                    continue;
                }
                break;
            }
            socket.close();
        }catch(Exception exception)
        {
            System.out.println("RequestProcessor : "+exception);
        }
    }
}
