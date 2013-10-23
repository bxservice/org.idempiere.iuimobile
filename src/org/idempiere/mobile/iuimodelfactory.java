package org.idempiere.mobile;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.MRole;
import org.compiere.model.PO;
import org.compiere.util.Env; 

public class iuimodelfactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		 if (tableName.equals(MRole.Table_Name)) {
		     return MRole.class;
		 }
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		 if (tableName.equals(MRole.Table_Name)) {
		     return new MRole(Env.getCtx(), Record_ID, trxName);
		 }
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if (tableName.equals(MRole.Table_Name)) {
		     return new MRole(Env.getCtx(), rs, trxName);
		   }
		return null;
	}
}
