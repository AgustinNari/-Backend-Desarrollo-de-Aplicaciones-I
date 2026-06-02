package com.example.quickbid.quickbid.repository.app;
import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import com.example.quickbid.quickbid.entity.app.NotificacionApp;
public interface NotificacionAppRepository extends JpaRepository<NotificacionApp,Long>{Optional<NotificacionApp> findByIdAndCuentaId(Long id,Long cuentaId);}
