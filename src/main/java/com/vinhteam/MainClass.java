/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vinhteam;

/**
 *
 * @author vinh
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class MainClass {

    private static String dbURL = "jdbc:derby:/home/vinh/Documents/projects/vinataxi/db/vinataxi;user=APP;password=APP";

    private static Connection conn = null;
    private static Statement stmt = null;

    public static void main(String[] args) throws Exception {
        createConnection();
        getAllTables();
        selectTable("USERS");
        shutdown();
    }

    private static List<String> getAllTables() throws SQLException {

        List<String> ret = new ArrayList<>();

        String sql = "select t.tablename "
                + " from sys.systables t, sys.sysschemas s  "
                + " where t.schemaid = s.schemaid "
                + " and t.tabletype = 'T' and s.schemaname = 'APP' "
                + " order by s.schemaname, t.tablename";

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        String tableName;
        while (rs.next()) {
            tableName = rs.getString(1);
            
            
            selectTable(tableName);
            
            ret.add(rs.getString(1));
        }
        return ret;
    }

    private static void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL);
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    private static void shutdown() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                //DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }
        } catch (SQLException sqlExcept) {

        }

    }

    private static void selectTable(String tableName) {
        try {
            // get data base metadata
            DatabaseMetaData metaData = conn.getMetaData();
// get columns
            ResultSet rs = metaData.getColumns(null, "APP", tableName, "%");
            List<String> columns = new ArrayList<String>();
            String columnName, columnType, columnLength;
            
            String sql = "CREATE TABLE " +tableName + " (\n";
            boolean isFirst = true;
            while (rs.next()) {
                // 1: none
                // 2: schema
                // 3: table name
                // 4: column name
                // 5: length
                // 6: data type (CHAR, VARCHAR, TIMESTAMP, ...)
                columnName  = rs.getString("COLUMN_NAME");
                columnLength= rs.getString("COLUMN_SIZE");
                columnType  = rs.getString(6);
                columns.add(columnName);
                
                if(columnType.equals("VARCHAR")){
                   columnLength = "(" + columnLength +")";
                }else{
                    columnLength = "";
                }
                if ("BLOB".equals(columnType)){
                    columnType = "BYTEA";
                    columnLength = "";
                }
                
                if(isFirst){
                    isFirst = false;
                    sql += String.format("\t%s %s %s\n", columnName, columnType, columnLength);
                }else{
                    sql += String.format("\t,%s %s %s\n", columnName, columnType, columnLength);
                }
                
                
            }
            sql += ");";
            
            System.out.println(sql);
            
            rs.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
}
