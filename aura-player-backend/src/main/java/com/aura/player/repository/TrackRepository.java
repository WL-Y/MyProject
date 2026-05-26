package com.aura.player.repository;

import com.aura.player.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, String> {

    @Query("SELECT t FROM Track t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.author) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.filename) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Track> search(@Param("q") String query);

    @Query("SELECT t FROM Track t WHERE t.subDir = :subDir")
    List<Track> findBySubDir(@Param("subDir") String subDir);
}
