package com.bluehouseinc.dataconverter.model;

import java.util.ArrayList;
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
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString(onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public abstract class BaseJobOrGroupObject  {

	@CsvIgnore
	protected List<? super BaseJobOrGroupObject> children = new ArrayList<>();

	@ToString.Include
	@EqualsAndHashCode.Include
	@CsvBindByName
	protected Integer id;

	@ToString.Include
	@EqualsAndHashCode.Include
	@CsvBindByName
	protected String name;

	@ToString.Include
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

	public <E extends BaseJobOrGroupObject> boolean addChildTest(E child) {
		
		try {
			addChild(child);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * Add a child to this object with TIDAL validation.
	 * Enforces the rule: no two objects at the same level can have the same name.
	 * 
	 * @param child The child object to add
	 * @throws IllegalArgumentException if child name conflicts with existing child
	 */
	public <E extends BaseJobOrGroupObject> void addChild(E child) {
		// TIDAL Validation: Check for name conflicts at this level
		if (hasChildWithName(child.getName())) {
			String errorMsg = String.format("TIDAL Validation Error: Cannot add child '%s' to '%s' - name already exists at this level. Full path would be: %s\\%s", child.getName(), this.getFullPath(), this.getFullPath(), child.getName());
			log.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		if(child.getName()==null) {
			child.getId();
		}
		// Set up parent-child relationship
		child.setParent(this);
		this.children.add(child);

		// Force path recalculation now that hierarchy is established
		child.invalidatePathCache();

		log.debug(String.format("Added child '%s' to '%s'. Full path: %s", child.getName(), this.getFullPath(), child.getFullPath()));
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

	/**
	 * Invalidate path cache for this object and all children recursively
	 */
	public void invalidatePathCache() {
		this.fullPath = null;

		for (Object child : children) {
			BaseJobOrGroupObject casted = (BaseJobOrGroupObject) child;
			casted.invalidatePathCache();
		}
	}

	/**
	 * Check if this object already has a child with the given name.
	 * Case-insensitive comparison for TIDAL compatibility.
	 */
	private boolean hasChildWithName(String childName) {
		if (childName == null) {
			return false;
		}

		return children.stream().anyMatch(child -> childName.equalsIgnoreCase(((BaseJobOrGroupObject) child).getName()));
	}

	/**
	 * Safe method to add child that returns success/failure instead of throwing exception.
	 * Useful for bulk imports where you want to continue processing other items.
	 */
	public <E extends BaseJobOrGroupObject> boolean tryAddChild(E child) {
		try {
			addChild(child);
			return true;
		} catch (IllegalArgumentException e) {
			log.error(String.format("Failed to add child: %s", e.getMessage()));
			return false;
		}
	}

	/**
	 * Get child by name (case-insensitive).
	 * Returns null if not found.
	 */
	@SuppressWarnings("unchecked")
	public <E extends BaseJobOrGroupObject> E getChildByName(String name) {
		if (name == null) {
			return null;
		}

		return (E) children.stream().filter(child -> name.equalsIgnoreCase(((BaseJobOrGroupObject) child).getName())).findFirst().orElse(null);
	}

	/**
	 * Remove child by name.
	 * Returns true if child was found and removed.
	 */
	public boolean removeChildByName(String name) {
		BaseJobOrGroupObject child = getChildByName(name);
		if (child != null) {
			children.remove(child);
			child.setParent(null);
			child.invalidatePathCache();
			log.trace(String.format("Removed child '%s' from '%s'", name, this.getFullPath()));
			return true;
		}
		return false;
	}

}
