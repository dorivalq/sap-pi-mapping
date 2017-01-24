package br.com.cpfl;

public class JcoConnectionTest {
	
/*
	ÁClient mConnection =
		JCO.createClient("160", // SAP client
		                 "ct06170", // userid
		                 "Guilherme4*", // password
		                 "EN", // language (null for the default language)
		                 "192.168.35.150", // application server host name
		"00"); // system number
*/		
	
	/*
		try 
		{
		// Connect to R/3
		   mConnection.connect();

		//Creating repository
		   JCO.Repository mRepository = new JCO.Repository("JCO Tutorial", mConnection);

		//Creating function template
		   IFunctionTemplate functionTemplate =
		mRepository.getFunctionTemplate("THE_RFM_NAME");

		//Getting JCO function
		   JCO.Function function = functionTemplate.getFunction();

		//Setting import parameters

		//Simple parameter
		   String myImportValue = "someValue";

		   function.getImportParameterList().setValue(myImportValue, "MY_IMPORT_VALUE");

		//Table paramter
		//getting JCO.Table
		   JCO.Table myTable = function.getTableParameterList().getTable("MY_TABLE");

		   myTable.appendRow();
		   myTable.setValue("TEST1", "MY_VALUE1");
		   myTable.setValue("TEST2", "My_VALUE2");

		//Executing Remote Function Module
		  mConnection.execute(function);

		//Export parameters
		//retrieving structure
		   JCO.Structure returnStructure = function.getExportParameterList().getStructure("RETURN_STRUCTURE");

		//printing structure field
		   System.out.println(returnStructure.getString("SOME_VALUE"));

		//printing table values
		   JCO.Table myTable = function.getTableParameterList().getTable("MY_TABLE");
		   for (int i = 0; i < myTable.getNumRows(); i++, myTable.nextRow()) 
		   {
		      System.out.println(myTable.getString("MY_VALUE1"));
		   }
		}
		catch (Exception ex) 
		{
		   ex.printStackTrace();
		   System.exit(0);
		}
		finally
		{
		//ensure that a connection is closed after the function module has been executed
		   mConnection.disconnect();
		}
	*/	

}
