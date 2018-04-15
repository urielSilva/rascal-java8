package com.rascaljava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				"(class_id int NOT NULL PRIMARY KEY AUTO_INCREMENT, qualified_name VARCHAR(255), method_id int, " +
				"superclass_id int, is_class tinyint(1), CONSTRAINT fk_superclass FOREIGN KEY (superclass_id) REFERENCES CLASS_DEFINITION(class_id))"
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
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ClassDefinition saveToDb(ClassDefinition classDef) {
	    try {
	    	int class_id = 0;
	    	PreparedStatement stmt;
	    	saveClass(classDef);
			stmt = connection.prepareStatement("SELECT * FROM CLASS_DEFINITION ORDER BY class_id DESC LIMIT 1 ");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				class_id = rs.getInt(1);
				classDef.setId(class_id);
			}
//			essa parte foi comentada para melhorar o tempo de execucao
			
//			for(MethodDefinition m : classDef.getMethods()) {
//				stmt = connection.prepareStatement("INSERT INTO METHOD_DEFINITION (name, return_type, class_id) values(?, ?, ?)");
//				stmt.setString(1, m.getName());
//				stmt.setString(2, m.getReturnType());
//				stmt.setInt(3, class_id);
//				stmt.executeUpdate();
//				
//				stmt = connection.prepareStatement("SELECT * FROM METHOD_DEFINITION ORDER BY method_id DESC LIMIT 1 ");
//				rs = stmt.executeQuery();
//				if (rs.next()) {
//					method_id = rs.getInt(1);
//				}
//			}
//			
////			for(FieldDefinition f : classDef.getFields()) {
////				stmt = connection.prepareStatement("INSERT INTO FIELD_DEFINITION (name, type, class_id) values(?, ?, ?)");
////				stmt.setString(1, f.getName());
////				stmt.setString(2, f.getType());
////				stmt.setInt(3, class_id);
////				stmt.executeUpdate();
////			}
			
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return classDef;
	}

	public void saveClass(ClassDefinition classDef) {
		try {
			PreparedStatement stmt;
			ClassDefinition superClass = getSuperClassFromClassDef(classDef);
			if(superClass != null) {
				stmt = connection.prepareStatement("INSERT INTO CLASS_DEFINITION (qualified_name, superclass_id, is_class) values(?,?,?)");
				stmt.setString(1, classDef.getQualifiedName());
				stmt.setInt(2, superClass.getId());
				stmt.setInt(3, classDef.isClass() ? 1 : 0);
			} else {
				stmt = connection.prepareStatement("INSERT INTO CLASS_DEFINITION (qualified_name, is_class) values(?,?)");
				stmt.setString(1, classDef.getQualifiedName());
				stmt.setInt(2, classDef.isClass() ? 1 : 0);
			}
;			stmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ClassDefinition getSuperClassFromClassDef(ClassDefinition classDef) {
		if(classDef.getSuperClass() == null) {
			return null;
		} else if(classDef.getSuperClass().getId() == null) {
			return findByQualifiedName(classDef.getSuperClass().getQualifiedName());
		} else {
			return classDef.getSuperClass();
		}
	}
	

	public ClassDefinition findByQualifiedName(String qualifiedName) {
		PreparedStatement stmt;
		try {
			ClassDefinition classDef = null;
			stmt = connection.prepareStatement("SELECT * FROM CLASS_DEFINITION where qualified_name = ?");
			stmt.setString(1, qualifiedName);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				classDef = new ClassDefinition();
				classDef.setId(rs.getInt("class_id"));
				classDef.setQualifiedName(rs.getString("qualified_name"));
				classDef.setClass(rs.getInt("is_class") != 0 ? true : false);
			}
			return classDef;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void fetchFromDb() {
		Map<String, Integer> names = new HashMap<>();
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT class_id, qualified_name, superclass_id FROM CLASS_DEFINITION");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				System.out.println("id: " + rs.getInt("class_id") + " nome: " + rs.getString("qualified_name") + " superclass_id: " + rs.getInt("superclass_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String findQualifiedName(String className) {
		PreparedStatement stmt;
		try {
			stmt = connection.prepareStatement(
				"SELECT * FROM CLASS_DEFINITION WHERE qualified_name like ? "
			);
			stmt.setString(1, "%." + className);
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

	public boolean isPersisted(String qualifiedName) {
		return findByQualifiedName(qualifiedName) != null;
	}

	public List<ClassDefinition> getAllAncestors(String qualifiedName) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT a.* from CLASS_DEFINITION a ");
		sb.append("INNER JOIN CLASS_DEFINITION b on a.class_id = b.superclass_id ");
		sb.append("WHERE b.qualified_name = ?");
		try {
			PreparedStatement stmt = connection.prepareStatement(sb.toString());
			stmt.setString(1, qualifiedName);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				List<ClassDefinition> list = new ArrayList<>();
				ClassDefinition classDef = new ClassDefinition();
				classDef.setId(rs.getInt("class_id"));
				classDef.setQualifiedName(rs.getString("qualified_name"));
				classDef.setClass(rs.getInt("is_class") != 0 ? true : false);
				list.add(classDef);
				if(classDef.getQualifiedName().equals("java.lang.Object")) {
					return list;
				} else {
					return Stream.concat(list.stream(), getAllAncestors(classDef.getQualifiedName()).stream()).collect(Collectors.toList());
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
		
	}

	public Integer countInserted() {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT count(*) from CLASS_DEFINITION");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
}
