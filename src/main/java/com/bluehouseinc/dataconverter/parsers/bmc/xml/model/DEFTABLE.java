//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.09 at 11:04:38 AM EST
//

package com.bluehouseinc.dataconverter.parsers.bmc.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;choice&gt;
 *           &lt;element name="WORKSPACE" type="{}WorkspaceData" minOccurs="0"/&gt;
 *           &lt;element name="FOLDER" type="{}SimpleFolder" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="SCHED_TABLE" type="{}SimpleFolder" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="TABLE" type="{}SimpleFolder" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="SMART_FOLDER" type="{}SmartFolder" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="SMART_TABLE" type="{}SmartTable" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="SCHED_GROUP" type="{}SmartTable" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "workspaceOrFOLDEROrSCHEDTABLE" })
@XmlRootElement(name = "DEFTABLE")
public class DEFTABLE {

	@XmlElementRefs({ @XmlElementRef(name = "WORKSPACE", type = JAXBElement.class, required = false), @XmlElementRef(name = "FOLDER", type = JAXBElement.class, required = false),
			@XmlElementRef(name = "SCHED_TABLE", type = JAXBElement.class, required = false), @XmlElementRef(name = "TABLE", type = JAXBElement.class, required = false),
			@XmlElementRef(name = "SMART_FOLDER", type = JAXBElement.class, required = false), @XmlElementRef(name = "SMART_TABLE", type = JAXBElement.class, required = false),
			@XmlElementRef(name = "SCHED_GROUP", type = JAXBElement.class, required = false) })
	protected List<JAXBElement<?>> workspaceOrFOLDEROrSCHEDTABLE;

	/**
	 * Gets the value of the workspaceOrFOLDEROrSCHEDTABLE property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the workspaceOrFOLDEROrSCHEDTABLE property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getWORKSPACEOrFOLDEROrSCHEDTABLE().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link WorkspaceData }{@code >}
	 * {@link JAXBElement }{@code <}{@link SimpleFolder }{@code >}
	 * {@link JAXBElement }{@code <}{@link SimpleFolder }{@code >}
	 * {@link JAXBElement }{@code <}{@link SimpleFolder }{@code >}
	 * {@link JAXBElement }{@code <}{@link SmartFolder }{@code >}
	 * {@link JAXBElement }{@code <}{@link SmartTable }{@code >}
	 * {@link JAXBElement }{@code <}{@link SmartTable }{@code >}
	 *
	 *
	 */
	public List<JAXBElement<?>> getWORKSPACEOrFOLDEROrSCHEDTABLE() {
		if (workspaceOrFOLDEROrSCHEDTABLE == null) {
			workspaceOrFOLDEROrSCHEDTABLE = new ArrayList<>();
		}
		return this.workspaceOrFOLDEROrSCHEDTABLE;
	}

}
