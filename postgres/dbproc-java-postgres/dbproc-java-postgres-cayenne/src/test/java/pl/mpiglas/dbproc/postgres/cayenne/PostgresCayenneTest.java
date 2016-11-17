package pl.mpiglas.dbproc.postgres.cayenne;

import java.util.Optional;
import java.util.stream.Stream;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.QueryResponse;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ProcedureQuery;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests calling PostgreSQL procedures with Apache Cayenne.
 *
 * @author Milosz Piglas
 */
public class PostgresCayenneTest
{

    private ServerRuntime runtime;

    @Before
    public void initCayenne()
    {
        runtime = new ServerRuntime("cayenne-dbproc.xml");
    }

    /**
     * Calls method with two input and two output parameters.
     */
    @Test
    public void shouldReturnFromInOutProcedure()
    {
        ProcedureQuery query = new ProcedureQuery("modmul");
        query.addParameter("anum", 10);
        query.addParameter("bnum", 3);

        ObjectContext ctx = runtime.newContext();
        QueryResponse response = ctx.performGenericQuery(query);
        Stream<DataRow> outRowStream = response.firstList().stream().map(DataRow.class::cast);
        Optional<DataRow> outRow = outRowStream.findAny();
        Assertions.assertThat(outRow.isPresent()).isTrue();
        Assertions.assertThat(outRow.get()).containsEntry("result", 3).containsEntry("modulo", 1);
    }

    @After
    public void releaseResources()
    {
        runtime.shutdown();
    }
}
