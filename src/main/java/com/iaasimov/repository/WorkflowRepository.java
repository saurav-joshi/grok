package com.iaasimov.repository;

import org.springframework.data.repository.CrudRepository;
import com.iaasimov.tables.Workflow;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowRepository extends CrudRepository<Workflow, Long> {
    List<Workflow> findByuserEmail(String email);

}
