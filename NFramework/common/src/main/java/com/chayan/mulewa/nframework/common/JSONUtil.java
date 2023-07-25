package com.chayan.mulewa.nframework.common;
import com.google.gson.*;
public class JSONUtil
{
    private JSONUtil()
    {

    }
    public static String toJSON(java.io.Serializable serializable)
    {
        try
        {
            Gson gson = new Gson();
            return gson.toJson(serializable);
        }catch (JsonParseException e)
        {
            System.out.println("To JSON Exception : "+e.getMessage());
            return "{}";
        }
    }
    public static <T> T fromJSON(String jsonString,Class<T> c)
    {
        try
        {
            Gson gson=new Gson();
            return gson.fromJson(jsonString,c);
        } catch (JsonParseException e)
        {
            System.out.println("From JSON Exception : "+e.getMessage());
            return null;
        }
    }
}
