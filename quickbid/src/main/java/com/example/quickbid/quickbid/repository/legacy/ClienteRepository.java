package com.example.quickbid.quickbid.repository.legacy;
import org.springframework.data.jpa.repository.JpaRepository; import com.example.quickbid.quickbid.entity.legacy.Cliente;
public interface ClienteRepository extends JpaRepository<Cliente,Integer>{}
