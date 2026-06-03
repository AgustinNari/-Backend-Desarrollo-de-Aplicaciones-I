package com.example.quickbid.quickbid.repository.app;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.quickbid.quickbid.dto.response.UsuarioDtos.HistoryItem;
import com.example.quickbid.quickbid.dto.response.UsuarioDtos.Notification;

@Repository
public class UsuarioQueryRepository {
	private static final String HISTORY_UNION = """
			SELECT 'puja' tipo,subasta_id,item_catalogo_id,NULL producto_id,monto,moneda,created_at,estado
			FROM app_pujas_live WHERE cuenta_id=?
			UNION ALL
			SELECT 'compra',subasta_id,item_catalogo_id,producto_id,monto_adjudicacion,moneda,created_at,estado
			FROM app_compras WHERE cuenta_comprador_id=?
			""";

	private final JdbcTemplate jdbc;

	public UsuarioQueryRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public StatisticsSnapshot statistics(Long accountId) {
		return jdbc.queryForObject("""
				SELECT
				  (SELECT COALESCE(SUM(monto),0) FROM app_pujas_live WHERE cuenta_id=?) total_bid,
				  (SELECT COALESCE(SUM(p.monto),0) FROM app_pagos p JOIN app_compras c ON c.id=p.compra_id
				   WHERE c.cuenta_comprador_id=? AND p.estado='aprobado') total_paid,
				  (SELECT COUNT(*) FROM app_pujas_live WHERE cuenta_id=?) bids,
				  (SELECT COUNT(*) FROM app_compras WHERE cuenta_comprador_id=?) purchases,
				  (SELECT COUNT(DISTINCT subasta_id) FROM app_pujas_live WHERE cuenta_id=?) auctions,
				  (SELECT COUNT(*) FROM app_pujas_live WHERE cuenta_id=? AND estado='ganadora') wins
				""", (rs, row) -> new StatisticsSnapshot(rs.getBigDecimal("total_bid"), rs.getBigDecimal("total_paid"),
						rs.getInt("bids"), rs.getInt("purchases"), rs.getInt("auctions"), rs.getInt("wins")),
				accountId, accountId, accountId, accountId, accountId, accountId);
	}

	public long countHistory(Long accountId) {
		return jdbc.queryForObject("SELECT COUNT(*) FROM (" + HISTORY_UNION + ") h", Long.class, accountId, accountId);
	}

	public List<HistoryItem> findHistory(Long accountId, int page, int size) {
		return jdbc.query("SELECT * FROM (" + HISTORY_UNION + ") h ORDER BY created_at DESC LIMIT ? OFFSET ?",
				(rs, row) -> new HistoryItem(rs.getString("tipo"), rs.getInt("subasta_id"),
						rs.getInt("item_catalogo_id"), (Integer) rs.getObject("producto_id"),
						rs.getBigDecimal("monto"), rs.getString("moneda"),
						rs.getObject("created_at", OffsetDateTime.class), rs.getString("estado")),
				accountId, accountId, size, page * size);
	}

	public long countNotifications(Long accountId, String type, Boolean read) {
		QueryFilter filter = notificationFilter(accountId, type, read);
		return jdbc.queryForObject("SELECT COUNT(*) FROM app_notificaciones" + filter.where(), Long.class,
				filter.args().toArray());
	}

	public List<Notification> findNotifications(Long accountId, String type, Boolean read, int page, int size) {
		QueryFilter filter = notificationFilter(accountId, type, read);
		filter.args().add(size);
		filter.args().add(page * size);
		return jdbc.query("SELECT * FROM app_notificaciones" + filter.where() + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
				(rs, row) -> new Notification(rs.getLong("id"), rs.getString("tipo"), rs.getString("titulo"),
						rs.getString("descripcion"), rs.getString("referencia_tipo"),
						(Long) rs.getObject("referencia_id"), rs.getBoolean("leida"),
						rs.getObject("created_at", OffsetDateTime.class)),
				filter.args().toArray());
	}

	public List<Activity> findMonthlyActivity(Long accountId) {
		return jdbc.query("""
				SELECT created_at,'puja' tipo FROM app_pujas_live WHERE cuenta_id=?
				UNION ALL
				SELECT created_at,'compra' FROM app_compras WHERE cuenta_comprador_id=?
				""", (rs, row) -> new Activity(rs.getObject("created_at", OffsetDateTime.class), rs.getString("tipo")),
				accountId, accountId);
	}

	private QueryFilter notificationFilter(Long accountId, String type, Boolean read) {
		StringBuilder where = new StringBuilder(" WHERE cuenta_id=?");
		List<Object> args = new ArrayList<>(List.of(accountId));
		if (type != null && !type.isBlank()) {
			where.append(" AND tipo=?");
			args.add(type);
		}
		if (read != null) {
			where.append(" AND leida=?");
			args.add(read);
		}
		return new QueryFilter(where.toString(), args);
	}

	public record StatisticsSnapshot(BigDecimal totalBid, BigDecimal totalPaid, int bids, int purchases, int auctions,
			int wins) {
	}

	public record Activity(OffsetDateTime createdAt, String type) {
	}

	private record QueryFilter(String where, List<Object> args) {
	}
}
