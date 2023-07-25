package com.chayan.mulewa.chess.main;
import com.chayan.mulewa.nframework.server.*;
import com.chayan.mulewa.chess.ui.*;
public class ChessServer
{
    public static void main(String gg[]) 
    {
        NFrameworkServer server=new NFrameworkServer();
        server.registerClass(ChessForServer.class);
        server.start();
    }
}