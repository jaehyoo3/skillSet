package com.skillset.livelectureservice.repository;

import com.skillset.livelectureservice.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}
