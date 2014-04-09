package com.siemens.cto.aem.persistence.jpa.service;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JpaQueryPaginatorTest {

    private JpaQueryPaginator paginator;
    private Query query;

    @Before
    public void setup() {
        paginator = new JpaQueryPaginator();
        query = mock(Query.class);
    }

    @Test
    public void testPaginateAll() {

        final PaginationParameter pagination = PaginationParameter.all();

        paginator.paginate(query,
                           pagination);

        verify(query, times(1)).setFirstResult(eq(pagination.getOffset()));
        verify(query, never()).setMaxResults(Matchers.anyInt());
    }

    @Test
    public void testPaginateLimited() {

        final PaginationParameter pagination = new PaginationParameter(12,
                                                                       24);

        paginator.paginate(query,
                           pagination);

        verify(query, times(1)).setFirstResult(eq(pagination.getOffset()));
        verify(query, times(1)).setMaxResults(eq(pagination.getLimit()));
    }
}
