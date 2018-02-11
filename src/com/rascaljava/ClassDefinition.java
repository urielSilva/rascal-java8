package com.rascaljava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDefinition {
	
	private String qualifiedName;
	
	private List<String> extendedTypes = new ArrayList<>();
	private List<String> implementedTypes = new ArrayList<>();
	private List<String> annotations = new ArrayList<>();
	
	private List<MethodDefinition> methods = new ArrayList<>();
	private List<FieldDefinition> fields = new ArrayList<>();
	
	public void addMethodDefinition(String name, String returnType, Map<String, String> args) {
		methods.add(new MethodDefinition(name, returnType, args));
	}
	
	public void addFieldDefinition(String name, String type) {
		fields.add(new FieldDefinition(name, type));
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public List<String> getExtendedTypes() {
		return extendedTypes;
	}

	public void setExtendedTypes(List<String> extendedTypes) {
		this.extendedTypes = extendedTypes;
	}

	public List<String> getImplementedTypes() {
		return implementedTypes;
	}

	public void setImplementedTypes(List<String> implementedTypes) {
		this.implementedTypes = implementedTypes;
	}

	public List<MethodDefinition> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodDefinition> methods) {
		this.methods = methods;
	}

	public List<FieldDefinition> getFields() {
		return fields;
	}

	public void setFields(List<FieldDefinition> fields) {
		this.fields = fields;
	}

	private class MethodDefinition {
		private String name;
		private String returnType;
		private Map<String, String> args = new HashMap<>();
		
		public MethodDefinition(String name, String returnType, Map<String, String> args) {
			this.name = name;
			this.returnType = returnType;
			this.args = args;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getReturnType() {
			return returnType;
		}
		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}
		public Map<String, String> getArgs() {
			return args;
		}
		public void setArgs(Map<String, String> args) {
			this.args = args;
		}
		
	}
	
	private class FieldDefinition {
		private String name;
		private String type;
		
		public FieldDefinition(String name, String type) {
			this.name = name;
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<String> annotations) {
		this.annotations = annotations;
	}
}




