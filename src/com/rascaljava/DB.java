package com.rascaljava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB {
	
	private Connection connection;
	private static DB instance = null;
	
	private DB(){
	}
	
	public static DB getInstance() {
      if(instance == null) {
         instance = new DB();
      }
      return instance;
	}
	
	public void setup() {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.
			        getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
			PreparedStatement stmt = connection.prepareStatement(
				"CREATE TABLE CLASS_DEFINITION" +
				"(class_id int NOT NULL PRIMARY KEY AUTO_INCREMENT, qualified_name VARCHAR(255), method_id int)"
			);
			stmt.execute();
			stmt = connection.prepareStatement(
				"CREATE TABLE METHOD_DEFINITION" +
				"(method_id int NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), return_type VARCHAR(255), " +
				"class_id int,  CONSTRAINT fk_methodclass FOREIGN KEY (class_id) REFERENCES CLASS_DEFINITION(class_id))"
			);
			stmt.execute();
			
			stmt = connection.prepareStatement("ALTER TABLE CLASS_DEFINITION ADD CONSTRAINT fk_exception_method FOREIGN KEY (method_id) REFERENCES METHOD_DEFINITION(method_id)"); 
			stmt.execute();
			stmt = connection.prepareStatement(
				"CREATE TABLE FIELD_DEFINITION" +
				"(field_id int NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), type VARCHAR(255)," +
				"class_id int, constraint fk_fieldclass FOREIGN KEY (class_id) REFERENCES CLASS_DEFINITION(class_id))"
			);
			stmt.execute();
			
			List<String> exceptions = new ArrayList<>();
			exceptions.add("java.io.IOException");
			for(String e : exceptions) {
				stmt = connection.prepareStatement("INSERT INTO CLASS_DEFINITION (qualified_name) values(?)");
				stmt.setString(1, e);
				stmt.executeUpdate();
			}
			
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveToDb(ClassDefinition classDef) {
	    try {
	    	int method_id = 0, class_id = 0;	
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO CLASS_DEFINITION (qualified_name) values(?)");
			stmt.setString(1, classDef.getQualifiedName());
			stmt.executeUpdate();
			stmt = connection.prepareStatement("SELECT * FROM CLASS_DEFINITION ORDER BY class_id DESC LIMIT 1 ");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				class_id = rs.getInt(1);
			}
			for(MethodDefinition m : classDef.getMethods()) {
				stmt = connection.prepareStatement("INSERT INTO METHOD_DEFINITION (name, return_type, class_id) values(?, ?, ?)");
				stmt.setString(1, m.getName());
				stmt.setString(2, m.getReturnType());
				stmt.setInt(3, class_id);
				stmt.executeUpdate();
				
				stmt = connection.prepareStatement("SELECT * FROM METHOD_DEFINITION ORDER BY method_Id DESC LIMIT 1 ");
				rs = stmt.executeQuery();
				if (rs.next()) {
					method_id = rs.getInt(1);
				}
			}
			
			for(FieldDefinition f : classDef.getFields()) {
				stmt = connection.prepareStatement("INSERT INTO FIELD_DEFINITION (name, type, class_id) values(?, ?, ?)");
				stmt.setString(1, f.getName());
				stmt.setString(2, f.getType());
				stmt.setInt(3, class_id);
				stmt.executeUpdate();
			}
			
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Integer fetchFromDb() {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM CLASS_DEFINITION WHERE method_id is not null");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String findQualifiedName(String className) {
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(
				"SELECT * FROM CLASS_DEFINITION WHERE qualified_name like ? "
			);
			stmt.setString(1, "%" + className + "%");
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return rs.getString("qualified_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Connection getConnection() {
		return connection;
	}
}
