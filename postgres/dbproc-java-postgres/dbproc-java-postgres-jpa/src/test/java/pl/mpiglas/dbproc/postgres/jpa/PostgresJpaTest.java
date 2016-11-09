package pl.mpiglas.dbproc.postgres.jpa;

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
        proc.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
        proc.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);

        proc.setParameter(1, 100);
        proc.setParameter(2, 11);

        Object result = proc.getSingleResult();
        Assertions.assertThat(result).isInstanceOf(Integer.class);
        Assertions.assertThat(result).isEqualTo(111);

    }

    @After
    public void releaseResources()
    {
        em.close();
        factory.close();
    }
}
