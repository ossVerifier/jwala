package com.siemens.cto.aem.persistence.jpa.service;

import javax.persistence.Query;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public class JpaQueryPaginator {

    public void paginate(final Query aQuery,
                         final PaginationParameter somePagination) {

        aQuery.setFirstResult(somePagination.getOffset());
        if (somePagination.isLimited()) {
            aQuery.setMaxResults(somePagination.getLimit());
        }
    }
}
