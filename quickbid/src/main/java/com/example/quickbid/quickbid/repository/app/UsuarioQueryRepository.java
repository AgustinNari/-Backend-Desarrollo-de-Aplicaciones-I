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

	public StatisticsSnapshot statistics(Long accountId, OffsetDateTime since) {
		String bidPeriod = since == null ? "" : " AND created_at >= ?";
		String paymentPeriod = since == null ? "" : " AND p.created_at >= ?";
		String purchasePeriod = since == null ? "" : " AND created_at >= ?";
		String consignmentPeriod = since == null ? "" : " AND created_at >= ?";
		String liquidationPeriod = since == null ? "" : " AND paid_at >= ?";
		List<Object> args = new ArrayList<>();
		String sql = """
				SELECT
				  (SELECT COALESCE(SUM(monto),0) FROM app_pujas_live WHERE cuenta_id=?%s) total_bid,
				  (SELECT COALESCE(SUM(p.monto),0) FROM app_pagos p JOIN app_compras c ON c.id=p.compra_id
				   WHERE c.cuenta_comprador_id=? AND p.estado='aprobado'%s) total_paid,
				  (SELECT COUNT(*) FROM app_pujas_live WHERE cuenta_id=?%s) bids,
				  (SELECT COUNT(*) FROM app_compras WHERE cuenta_comprador_id=?%s) purchases,
				  (SELECT COUNT(DISTINCT subasta_id) FROM app_pujas_live WHERE cuenta_id=?%s) auctions,
				  (SELECT COUNT(*) FROM app_pujas_live WHERE cuenta_id=? AND estado='ganadora'%s) wins,
				  (SELECT COUNT(*) FROM app_solicitudes_consignacion WHERE cuenta_id=?%s) consignments,
				  (SELECT COUNT(*) FROM app_solicitudes_consignacion WHERE cuenta_id=?
				    AND estado IN ('vendida','comprada_por_empresa','liquidada')%s) sold_consignments,
				  (SELECT COUNT(*) FROM app_solicitudes_consignacion WHERE cuenta_id=? AND estado='liquidada'%s)
				    liquidated_consignments,
				  (SELECT COALESCE(SUM(l.monto_neto),0)
				   FROM app_liquidaciones_consignacion l JOIN app_solicitudes_consignacion s ON s.id=l.solicitud_id
				   WHERE s.cuenta_id=? AND l.estado='pagada'%s) total_liquidated
				""".formatted(bidPeriod, paymentPeriod, bidPeriod, purchasePeriod, bidPeriod, bidPeriod,
				consignmentPeriod, consignmentPeriod, consignmentPeriod, liquidationPeriod);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		return jdbc.queryForObject(sql, (rs, row) -> new StatisticsSnapshot(rs.getBigDecimal("total_bid"),
						rs.getBigDecimal("total_paid"), rs.getInt("bids"), rs.getInt("purchases"),
						rs.getInt("auctions"), rs.getInt("wins"), rs.getInt("consignments"),
						rs.getInt("sold_consignments"), rs.getInt("liquidated_consignments"),
						rs.getBigDecimal("total_liquidated")),
				args.toArray());
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

	public List<Activity> findMonthlyActivity(Long accountId, OffsetDateTime since) {
		String period = since == null ? "" : " AND created_at >= ?";
		List<Object> args = new ArrayList<>();
		addPeriodArgs(args, accountId, since);
		addPeriodArgs(args, accountId, since);
		return jdbc.query("""
				SELECT created_at,'puja' tipo FROM app_pujas_live WHERE cuenta_id=?
				%s
				UNION ALL
				SELECT created_at,'compra' FROM app_compras WHERE cuenta_comprador_id=?
				%s
				""".formatted(period, period), (rs, row) -> new Activity(rs.getObject("created_at", OffsetDateTime.class),
						rs.getString("tipo")), args.toArray());
	}

	private void addPeriodArgs(List<Object> args, Long accountId, OffsetDateTime since) {
		args.add(accountId);
		if (since != null) args.add(since);
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
			int wins, int consignments, int soldConsignments, int liquidatedConsignments, BigDecimal totalLiquidated) {
	}

	public record Activity(OffsetDateTime createdAt, String type) {
	}

	private record QueryFilter(String where, List<Object> args) {
	}
}
