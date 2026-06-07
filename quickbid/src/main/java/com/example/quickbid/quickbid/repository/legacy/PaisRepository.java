package com.example.quickbid.quickbid.repository.legacy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quickbid.quickbid.entity.legacy.Pais;

public interface PaisRepository extends JpaRepository<Pais, Integer> {

	@Query("""
			SELECT p FROM Pais p
			WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
			   OR LOWER(p.nombreCorto) LIKE LOWER(CONCAT('%', :buscar, '%'))
			   OR LOWER(p.nacionalidad) LIKE LOWER(CONCAT('%', :buscar, '%'))
			   OR LOWER(p.capital) LIKE LOWER(CONCAT('%', :buscar, '%'))
			""")
	Page<Pais> buscar(@Param("buscar") String buscar, Pageable pageable);
}
