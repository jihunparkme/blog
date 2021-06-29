package com.example.demo.gallery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.gallery.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	@Query("SELECT m "
			+ "FROM Member m "
			+ "WHERE m.name = :name")
	Optional<Member> findByName(@Param("name") String name);
}