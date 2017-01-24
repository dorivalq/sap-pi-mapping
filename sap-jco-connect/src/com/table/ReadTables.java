package com.table;

import java.io.FileWriter;
import java.io.IOException;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

public class ReadTables {

	private static final String DESTINATION_NAME2 = null;

	public static void readTables() throws JCoException, IOException {
		
		final JCoDestination destination = JCoDestinationManager.getDestination(DESTINATION_NAME2);
		final JCoFunction function = destination.getRepository().getFunction("RFC_READ_TABLE");

		function.getImportParameterList().setValue("QUERY_TABLE", "DD02L");
		function.getImportParameterList().setValue("DELIMITER", ",");

		if (function == null) {
			throw new RuntimeException("BAPI RFC_READ_TABLE not found in SAP.");
		}

		try {
			function.execute(destination);
		} catch (final AbapException e) {
			System.out.println(e.toString());
			return;
		}

		/*
		 JCoTable table = function.getTableParameterList().getTable("FIELDS");
		table.appendRow();
		table.setValue("FIELDNAME", "TABNAME");
		table.appendRow();
		table.setValue("FIELDNAME", "TABCLASS");
		 * */
		final JCoTable codes = function.getTableParameterList().getTable("FIELDS");
		String header = "SN";
		for (int i = 0; i < codes.getNumRows(); i++) {
			codes.setRow(i);
			header += "," + codes.getString("FIELDNAME");
		}
		final FileWriter outFile = new FileWriter("out.csv");
		outFile.write(header + "\n");
		final JCoTable rows = function.getTableParameterList().getTable("DATA");

		for (int i = 0; i < rows.getNumRows(); i++) {
			rows.setRow(i);
			outFile.write(i + "," + rows.getString("WA") + "\n");
			outFile.flush();
		}
		outFile.close();

	}
}
