package com.iaasimov.repository;

import org.springframework.data.repository.CrudRepository;
import com.iaasimov.tables.Workflow;

import java.util.List;

public interface WorkflowRepository extends CrudRepository<Workflow, Long> {
    List<Workflow> findByuserEmail(String email);

}
