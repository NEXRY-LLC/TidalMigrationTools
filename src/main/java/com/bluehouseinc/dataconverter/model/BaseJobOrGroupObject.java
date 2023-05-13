package com.bluehouseinc.dataconverter.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public abstract class BaseJobOrGroupObject {


	@CsvIgnore
	protected List<? super BaseJobOrGroupObject> children = new LinkedList<>();

	@Include
	@EqualsAndHashCode.Include
	@CsvBindByName
	protected Integer id;


	@Include
	@EqualsAndHashCode.Include
	@CsvBindByName
	protected String name;

	@Include
	@EqualsAndHashCode.Include
	@CsvBindByName
	protected String fullPath;

	// @Getter(value = AccessLevel.PRIVATE)
	@CsvIgnore
	protected BaseJobOrGroupObject parent;

	@CsvBindByName
	Integer parentId;

	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	private static final AtomicInteger count = new AtomicInteger(Integer.MIN_VALUE);

	public BaseJobOrGroupObject() {
		this.id = count.getAndIncrement();
	}

	public String getFullPath() {
		return doGetFullPath();
	}

	private String doGetFullPath() {

		String path = this.name;
		BaseJobOrGroupObject parent = this.getParent();

		if (parent != null) {
			this.fullPath = parent.getFullPath() + "\\" + path;
		} else {
			this.fullPath = "\\" + path;
		}

		return this.fullPath;
	}

	/**
	 * Add a child to this object. This method handles the setting up objects in
	 * this object correctly.
	 */

	public <E extends BaseJobOrGroupObject> void addChild(E child) {
		child.setParent(this);
		this.children.add(child);
	}

	void setParent(BaseJobOrGroupObject parent) {
		this.parent = parent;
		this.parentId = parent.getId();
	}

	@SuppressWarnings("unchecked")
	public <E extends BaseJobOrGroupObject> List<E> getChildren() {
		return (List<E>) this.children;
	}

	public abstract boolean isGroup();

}
