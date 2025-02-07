package com.sami.sami_app.infrastructure.abstract_services;

import com.sami.sami_app.api.dto.request.create.ServiceEntityRequest;
import com.sami.sami_app.api.dto.response.ServiceEntityResponse;
import com.sami.sami_app.util.enums.SortType;
import org.springframework.data.domain.Page;


public interface IServiceEntityService extends CrudService <ServiceEntityRequest, ServiceEntityResponse, Long> {
    //Pagination
    Page<ServiceEntityResponse> getAll(int page, int size, SortType sortType);

    public final String FIELD_BY_SORT = "status";

}
