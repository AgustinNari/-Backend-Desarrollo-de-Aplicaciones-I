package com.example.quickbid.quickbid.repository.app;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.quickbid.quickbid.dto.admin.AdminDtos.Auction;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Consignment;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.PaymentMethod;

@Repository
public class AdminQueryRepository {
	private final JdbcTemplate jdbc;

	public AdminQueryRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public List<PaymentMethod> findPaymentMethods(String state) {
		String filter = state == null || state.isBlank() ? "" : " WHERE estado=?";
		Object[] args = filter.isEmpty() ? new Object[0] : new Object[] { state };
		return jdbc.query("""
				SELECT id,cuenta_id,tipo,moneda,estado,alias_visible,motivo_rechazo,verificado_hasta
				FROM app_medios_pago
				""" + filter + " ORDER BY id", (rs, row) -> new PaymentMethod(rs.getLong("id"),
						rs.getLong("cuenta_id"), rs.getString("tipo"), rs.getString("moneda"), rs.getString("estado"),
						rs.getString("alias_visible"), rs.getString("motivo_rechazo"),
						rs.getObject("verificado_hasta", OffsetDateTime.class)), args);
	}

	public List<Consignment> findConsignments() {
		return jdbc.query("""
				SELECT id,cuenta_id,titulo,estado,producto_id,item_catalogo_id,subasta_id
				FROM app_solicitudes_consignacion ORDER BY id
				""", (rs, row) -> new Consignment(rs.getLong("id"), rs.getLong("cuenta_id"), rs.getString("titulo"),
						rs.getString("estado"), (Integer) rs.getObject("producto_id"),
						(Integer) rs.getObject("item_catalogo_id"), (Integer) rs.getObject("subasta_id")));
	}

	public Optional<Auction> findAuction(Integer id) {
		return jdbc.query("""
				SELECT s.identificador,e.titulo,s.fecha,s.hora,s.ubicacion,s.categoria,e.moneda,e.estado_operativo
				FROM subastas s JOIN app_subasta_ext e ON e.subasta_id=s.identificador WHERE s.identificador=?
				""", (rs, row) -> new Auction(rs.getInt("identificador"), rs.getString("titulo"),
						rs.getDate("fecha").toLocalDate(), rs.getTime("hora").toLocalTime(), rs.getString("ubicacion"),
						rs.getString("categoria"), rs.getString("moneda"), rs.getString("estado_operativo")), id)
				.stream().findFirst();
	}
}
