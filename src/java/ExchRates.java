import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.jrj.mdb.IMdbManager;
/**
 *
 * @author Michał Śliwa
 */
@Path("/exchangeRate")
public class ExchRates
{
    private final String MDB_DESC = "java:global/mdb-project/"+
                                    "MdbManager!pl.jrj.mdb.IMdbManager";
    
    /**
     * Return ratio of given currency to base currency
     * @param currCode 
     * @return
     */
    @GET
    @Path("/{currCode}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response calculateRate(@PathParam("currCode") String currCode)
    {
        //method output
        String output = null;
        //base currency code
        String baseCurr = null;
        try
        {
            //get new context
            Context ctx = new InitialContext();
            //lookup remote EJB component
            IMdbManager remote = (IMdbManager)ctx.lookup(MDB_DESC);
            //get base currency code
            baseCurr = remote.currencyId();
        }
        catch (NamingException ex)
        {
            //print exception to output
            output = ex.toString();
        }
        //if sucessfully retrieved base currency code
        if (baseCurr != null)
        {
            //get exchange rates of given currencies
            HashMap<String,Double> rates;
            rates = new RatesFinder().getExchangeRates(baseCurr, currCode);
            
            //if map contains both exchange rates
            if (rates.containsKey(baseCurr) && rates.containsKey(currCode))
            {
                //calculate ratio of exchange rates
                Double res = rates.get(currCode)/rates.get(baseCurr);
                //create math context for rounding rules
                MathContext mc = new MathContext(4,RoundingMode.HALF_EVEN);
                //output ratio correctly rounded to 4 decimal places
                output = new BigDecimal(res).round(mc).toString();
            }
        }
        //return response       
        return Response.ok().entity(output).build();
    }
}
