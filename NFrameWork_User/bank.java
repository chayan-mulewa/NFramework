import com.chayan.mulewa.nframework.server.*;
import com.chayan.mulewa.nframework.server.annotations.*;

@Path("/banking")
public class bank
{
    @Path("/branchName")
    public String getBranchName(String city) throws bank_exception
    {
        if(city.equalsIgnoreCase("Ujjain"))
        {
            return "Tower";
        }
        if(city.equalsIgnoreCase("Indore"))
        {
            return "Silicon City";
        }
        throw new bank_exception("No Branch In The City");
    }
    public static void main(String[] gg)
    {
        NFrameworkServer s=new NFrameworkServer();
        s.registerClass(bank.class);
        s.start();
    }
}