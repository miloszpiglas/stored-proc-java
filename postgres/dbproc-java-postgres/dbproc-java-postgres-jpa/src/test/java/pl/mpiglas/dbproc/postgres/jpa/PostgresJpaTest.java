package pl.mpiglas.dbproc.postgres.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.StoredProcedureQuery;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test calling PostgreSQL procedures with JPA/Hibernate.
 *
 * @author Milosz Piglas
 */
public class PostgresJpaTest
{

    private EntityManager em;
    private EntityManagerFactory factory;

    @Before
    public void initEntityManager()
    {
        factory = Persistence.createEntityManagerFactory("postgresPu");
        em = factory.createEntityManager();
    }

    /**
     * Calls procedure with two input parameters and single return value.
     */
    @Test
    public void shouldReturnSingleValueFromInputParams()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("num_sum");

        // postgresql supports only positional parameters
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);

        proc.setParameter(1, 100);
        proc.setParameter(2, 11);

        Object result = proc.getSingleResult();
        Assertions.assertThat(result).isInstanceOf(Integer.class);
        Assertions.assertThat(result).isEqualTo(111);
    }

    /**
     * Calls procedure without input parameters, which returns single value,
     * defined as output param.
     */
    @Test
    public void shouldReturnSingleOutParam()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("out_text");

        Object result = proc.getSingleResult();
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(String.class);
        Assertions.assertThat(result).isEqualTo("out_text");
    }

    /**
     * Calls procedure without input parameter and single output parameter.
     *
     * In this case value is read explicitly from registered output parameter.
     * Compare with {@link PostgresJpaTest#shouldReturnSingleOutParam}
     */
    @Test
    public void shouldReadFromSingleOutParam()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("out_text");

        proc.registerStoredProcedureParameter(1, String.class, ParameterMode.OUT);
        proc.execute();
        Object result = proc.getOutputParameterValue(1);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(String.class);
        Assertions.assertThat(result).isEqualTo("out_text");
    }

    /**
     * Calls procedure with two input and two output parameters.
     *
     * Value of output parameters is read from return statement.
     */
    @Test
    public void shouldReturnFromInOutProcedure()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("modmul");
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);

        proc.setParameter(1, 10);
        proc.setParameter(2, 3);

        // hibernate autmatically finds output parameters and returns them as single array
        Object[] result = (Object[]) proc.getSingleResult();

        Assertions.assertThat(result.length).isEqualTo(2);
        Assertions.assertThat(result[0]).isEqualTo(3); // 10 / 3
        Assertions.assertThat(result[1]).isEqualTo(1); // 10 % 3
    }

    /**
     * Calls procedure with two input and two output parameters.
     *
     * Values calculated in procedure are directly read from output parameters.
     */
    @Test
    public void shouldReadOutFromInOutProcedure()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("modmul");
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);

        // before reading directly from output parameters we have to register them first
        proc.registerStoredProcedureParameter(3, Integer.class, ParameterMode.OUT);
        proc.registerStoredProcedureParameter(4, Integer.class, ParameterMode.OUT);

        proc.setParameter(1, 10);
        proc.setParameter(2, 3);

        proc.execute();

        Assertions.assertThat(proc.getOutputParameterValue(3)).isEqualTo(3); // 10 / 3
        Assertions.assertThat(proc.getOutputParameterValue(4)).isEqualTo(1); // 10 % 3
    }

    /**
     * Calls procedure with single input parameter which returns list of
     * calculated values.
     */
    @Test
    public void shouldReturnListOfIntegers()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("int_set");
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.setParameter(1, 6);

        List<Integer> result = proc.getResultList();
        Assertions.assertThat(result).hasSize(6);
        Assertions.assertThat(result).containsExactly(100, 200, 300, 400, 500, 600);
    }

    @Test
    public void shouldReturnListOfRecords()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("gen_rows");
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.setParameter(1, 6);

        List<Object[]> result = proc.getResultList();
        Assertions.assertThat(result).hasSize(6);
    }

    @Test
    public void shouldReturnListOfMappedRecords()
    {
        StoredProcedureQuery proc = em.createStoredProcedureQuery("gen_rows", "GenRowRecord");
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.setParameter(1, 6);

        List<StrIntRecord> result = proc.getResultList();
        Assertions.assertThat(result).hasSize(6);
        Assertions.assertThat(result).contains(rec(0), rec(1), rec(2), rec(3), rec(4), rec(5));
    }

    private static StrIntRecord rec(int index)
    {
        return new StrIntRecord("ROW" + index, index + 1);
    }

    @After
    public void releaseResources()
    {
        em.close();
        factory.close();
    }
}
