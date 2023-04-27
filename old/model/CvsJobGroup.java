package com.dataconverter.csv.model;

import java.util.ArrayList;
import java.util.List;

import com.opencsv.bean.CsvIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CvsJobGroup extends CvsAbstractJobOrGroup {

	@CsvIgnore
	List<CvsAbstractJobOrGroup> children =new ArrayList<CvsAbstractJobOrGroup>();;

	
	@Override
	public void setId(Integer id) {
		super.setId(id);
		
		this.children.stream().forEach(f -> f.setParentId(id));
	}
	
	
	public CvsJobGroup() {
		super();
	}
	
	/*
	 * Question: Should we fix duplicate names here or leave that for the import validation? 
	 */
	public void addChildJobOrGroup(CvsAbstractJobOrGroup child) {
		//child.parent = this; // Set the parent to me.
		child.setParent(this);
		child.setParentId(this.getId());
		child.setOwner(this.getOwner());
		child.setCalender(this.getCalender());
		this.children.add(child);
	}
	
}
