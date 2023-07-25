package com.chayan.mulewa.nframework.client;
import com.chayan.mulewa.nframework.common.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;

import javax.swing.text.html.StyleSheet;
public class NFrameworkClient
{
    public Object execute(String servicePath,Object ...arguments) throws Throwable
    {  
         try
        {
            Request request=new Request();
            request.setServicePath(servicePath);
            request.setArguments(arguments);
            String requestJSONString=JSONUtil.toJSON(request);
            byte objectBytes[]=requestJSONString.getBytes(StandardCharsets.UTF_8);
            int requestLength=objectBytes.length;
            byte header[]=new byte[1024];
            int x=requestLength;
            int i=1023;
            while(x>0)
            {
                header[i]=(byte)(x%10);
                x=x/10;
                i--;
            }
            Socket socket=new Socket("localhost",5500);
            OutputStream os=socket.getOutputStream();
            os.write(header,0,1024);
            os.flush();
        
            InputStream is=socket.getInputStream();
            byte ack[]=new byte[1];
            int byteReadCount;
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
            int j=0;
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
        
            int byteToReceive=1024;
            byte tmp[]=new byte[1024];
            int k;
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
        
            int responseLength=0;
            i=1;
            j=1023;
            while(j>=0)
            {
                responseLength=responseLength+(header[j]*i);
                i=i*10;
                j--;
            }
        
            ack[0]=1;
            os.write(ack,0,1);
            os.flush();
            
            byte response[]=new byte[responseLength];
            byteToReceive=responseLength;
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
                    response[i]=tmp[k];
                    i++;
                }
                j=j+byteReadCount;
            }
        
            ack[0]=1;
            os.write(ack);
            os.flush();
            socket.close();
            
            String responseJSONString=new String(response,StandardCharsets.UTF_8);
            Response responseObject=JSONUtil.fromJSON(responseJSONString,Response.class);
            if(responseObject.getSuccess())
            {
                // Object arg[]=responseObject.getResult();

                // int sourceRow = ((Double) arg[0]).intValue();
                // int sourceCol = ((Double) arg[1]).intValue();
                // int targetRow = ((Double) arg[2]).intValue();
                // int targetCol = ((Double) arg[3]).intValue();
                
                // System.out.println("seleted row "+arg[0]);
                // System.out.println("seleted col "+arg[1]);
                // System.out.println("target row "+arg[2]);
                // System.out.println("target col "+arg[3]);
            }
            else
            {
                System.out.println("else got called : ");
                throw responseObject.getException() ;
            }
        }catch(Exception exception)
        {
            System.out.println("NFrameworkClient : "+exception.getMessage());
        }
        return null;
    } 
}
