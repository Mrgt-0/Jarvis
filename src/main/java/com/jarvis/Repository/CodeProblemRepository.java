package com.jarvis.Repository;
import com.jarvis.Model.Entity.CodeProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeProblemRepository extends JpaRepository<CodeProblem, Long> { }
