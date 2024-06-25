package com.skillset.livelectureservice.repository;

import com.skillset.livelectureservice.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findTopByUserIdAndClassroomIdOrderByStartTimeDesc(String userId, Long classroomId);

}
