package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

//CA7UserField.java - User-defined field
public class CA7UserField {
 private String fieldName;
 private String fieldValue;

 // Constructors
 public CA7UserField() {}

 public CA7UserField(String fieldName, String fieldValue) {
     this.fieldName = fieldName;
     this.fieldValue = fieldValue;
 }

 // Getters and Setters
 public String getFieldName() { return fieldName; }
 public void setFieldName(String fieldName) { this.fieldName = fieldName; }

 public String getFieldValue() { return fieldValue; }
 public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }

 @Override
 public String toString() {
     return String.format("CA7UserField{name='%s', value='%s'}", fieldName, fieldValue);
 }
}