package com.rascaljava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;

public class ClassDefinition {
	
	private Integer id;
	private String qualifiedName;
	
	private ClassDefinition superClass;
	private List<String> implementedTypes = new ArrayList<>();
	private List<String> annotations = new ArrayList<>();
	private boolean isClass;
	
	private List<MethodDefinition> methods = new ArrayList<>();
	private List<FieldDefinition> fields = new ArrayList<>();
	
	public ClassDefinition() {
	}
	
	public ClassDefinition(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}
	
	@Override
	public String toString() {
		return qualifiedName;
	}
	
	public void addMethodDefinition(String name, String returnType, Map<String, String> args, List<ClassDefinition> exceptions) {
		methods.add(new MethodDefinition(name, returnType, args, exceptions));
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

	public ClassDefinition getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ClassDefinition superClass) {
		this.superClass = superClass;
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
	
	public List<String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<String> annotations) {
		this.annotations = annotations;
	}

	public boolean isClass() {
		return isClass;
	}

	public void setClass(boolean isClass) {
		this.isClass = isClass;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}