package pl.mpiglas.dbproc.postgres.cayenne;

import java.util.List;
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

    /**
     * Calling method that return single value. In this case we define
     * artificial OUT parameter 'ret', which Cayenne uses to set value returned
     * from procedure.
     */
    @Test
    public void shouldReturnSingleValueToOutParameter()
    {
        ProcedureQuery query = new ProcedureQuery("num_sum");
        query.addParameter("anum", 100);
        query.addParameter("bnum", 11);

        ObjectContext ctx = runtime.newContext();
        QueryResponse response = ctx.performGenericQuery(query);
        List<DataRow> outRows = response.firstList();
        Assertions.assertThat(outRows).hasSize(1);
        Assertions.assertThat(outRows.get(0)).containsEntry("ret", 111);
    }

    /**
     * Calling procedure, that returns set of integers. Procedure is called from
     * SELECT statement.
     */
    @Test
    public void shouldReturnSetOfInt()
    {
        ProcedureQuery query = new ProcedureQuery("int_set");
        query.addParameter("len", 10);

        ObjectContext ctx = runtime.newContext();

        // instead performGenericQuery, we use ObjectContext.performQuery which will
        // read all rows from procedure
        List<DataRow> response = ctx.performQuery(query);
        Assertions.assertThat(response.size()).isEqualTo(10);
        for (int r = 0; r < response.size(); r++)
        {
            Assertions.assertThat(response.get(r)).containsEntry("result", 100 * (r + 1));
        }

    }

    /**
     * Calling procedure, that returns set of records. Procedure is called from
     * SELECT statement.
     */
    @Test
    public void shouldReturnSetOfRecords()
    {
        ProcedureQuery query = new ProcedureQuery("gen_rows");
        query.addParameter("nrows", 10);

        ObjectContext ctx = runtime.newContext();

        // instead performGenericQuery, we use ObjectContext.performQuery which will
        // read all rows from procedure
        List<DataRow> response = ctx.performQuery(query);
        Assertions.assertThat(response.size()).isEqualTo(10);
        for (int r = 0; r < response.size(); r++)
        {
            Assertions.assertThat(response.get(r)).containsEntry("str", "ROW" + r).containsEntry("num", r + 1);
        }
    }

    /**
     * Calls procedure and reads result from single output parameter.
     */
    @Test
    public void shouldReadResultFromOutParam()
    {
        ProcedureQuery query = new ProcedureQuery("out_text");

        ObjectContext ctx = runtime.newContext();
        QueryResponse response = ctx.performGenericQuery(query);
        List<DataRow> outRows = response.firstList();
        Assertions.assertThat(outRows).hasSize(1);
        Assertions.assertThat(outRows.get(0)).containsEntry("txt", "out_text");
    }

    @After
    public void releaseResources()
    {
        runtime.shutdown();
    }
}
