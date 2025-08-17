package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

// CA7Resource.java - Resource constraint definition
public class CA7Resource {
	private String resourceName;
	private String usage;
	private int quantity;

	// Constructors
	public CA7Resource() {
	}

	public CA7Resource(String resourceName, String usage, int quantity) {
		this.resourceName = resourceName;
		this.usage = usage;
		this.quantity = quantity;
	}

	// Getters and Setters
	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return String.format("CA7Resource{name='%s', usage='%s', qty=%d}", resourceName, usage, quantity);
	}
}