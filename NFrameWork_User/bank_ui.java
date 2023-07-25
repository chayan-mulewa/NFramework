import com.chayan.mulewa.nframework.client.*;
class bank_ui
{
    public static void main(String[] gg)
    {
        NFrameworkClient c=new NFrameworkClient();
        try
        {
            String branchName=(String)c.execute("/banking/branchName",gg[0]);
            System.out.println("Branch Name : "+branchName);   
        }catch (Throwable t)
        {
            System.out.println(t.getMessage());
        }
    }
}
