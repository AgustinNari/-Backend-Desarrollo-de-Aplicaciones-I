package com.example.quickbid.quickbid.repository.app;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Document;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Summary;

@Repository
public class PurchaseReadRepository {
	private final JdbcTemplate jdbc;

	public PurchaseReadRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public long countByAccountId(Long accountId, String state) {
		if (state == null) {
			return jdbc.queryForObject("SELECT COUNT(*) FROM app_compras WHERE cuenta_comprador_id=?", Long.class, accountId);
		}
		return jdbc.queryForObject("SELECT COUNT(*) FROM app_compras WHERE cuenta_comprador_id=? AND estado=?",
				Long.class, accountId, state);
	}

	public List<Summary> findByAccountId(Long accountId, String state, int page, int size) {
		if (state != null) {
			return jdbc.query("""
					SELECT id,subasta_id,item_catalogo_id,producto_id,monto_adjudicacion,moneda,estado,created_at
					FROM app_compras WHERE cuenta_comprador_id=? AND estado=?
					ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?
					""", (rs, row) -> summary(rs), accountId, state, size, page * size);
		}
		return jdbc.query("""
				SELECT id,subasta_id,item_catalogo_id,producto_id,monto_adjudicacion,moneda,estado,created_at
				FROM app_compras WHERE cuenta_comprador_id=? ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?
				""", (rs, row) -> summary(rs), accountId, size, page * size);
	}

	public List<Document> findAvailableDocuments(Long purchaseId) {
		return jdbc.query("""
				SELECT id,tipo,estado,archivo_id,filename_original,content_type,size_bytes,created_at
				FROM (
					SELECT d.id,d.tipo,d.estado,d.archivo_id,a.filename_original,a.content_type,a.size_bytes,d.created_at,
					       ROW_NUMBER() OVER (PARTITION BY d.tipo ORDER BY d.created_at DESC,d.id DESC) rn
					FROM app_documentos d JOIN app_archivos a ON a.id=d.archivo_id
					WHERE d.referencia_tipo='compra' AND d.referencia_id=? AND d.estado='disponible'
				) latest
				WHERE rn=1
				ORDER BY created_at,id
				""", (rs, row) -> new Document(rs.getLong("id"), rs.getString("tipo"), rs.getString("estado"),
						rs.getLong("archivo_id"), rs.getString("filename_original"), rs.getString("content_type"),
						rs.getLong("size_bytes"), rs.getObject("created_at", OffsetDateTime.class)), purchaseId);
	}

	private Summary summary(java.sql.ResultSet rs) throws java.sql.SQLException {
		return new Summary(rs.getLong("id"), rs.getInt("subasta_id"), rs.getInt("item_catalogo_id"),
				rs.getInt("producto_id"), rs.getBigDecimal("monto_adjudicacion"), rs.getString("moneda"),
				rs.getString("estado"), rs.getObject("created_at", OffsetDateTime.class));
	}
}
