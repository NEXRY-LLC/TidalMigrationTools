//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.09 at 11:04:38 AM EST
//

package com.bluehouseinc.dataconverter.parsers.bmc.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for QuantitativeResourceData complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="QuantitativeResourceData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="NAME" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="QUANT" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ONFAIL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ONOK" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantitativeResourceData")
public class QuantitativeResourceData {

	@XmlAttribute(name = "NAME")
	protected String name;
	@XmlAttribute(name = "QUANT")
	protected Integer quant;
	@XmlAttribute(name = "ONFAIL")
	protected String onfail;
	@XmlAttribute(name = "ONOK")
	protected String onok;

	/**
	 * Gets the value of the name property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getNAME() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setNAME(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the quant property.
	 *
	 * @return
	 *         possible object is
	 *         {@link Integer }
	 *
	 */
	public Integer getQUANT() {
		return quant;
	}

	/**
	 * Sets the value of the quant property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link Integer }
	 *
	 */
	public void setQUANT(Integer value) {
		this.quant = value;
	}

	/**
	 * Gets the value of the onfail property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getONFAIL() {
		return onfail;
	}

	/**
	 * Sets the value of the onfail property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setONFAIL(String value) {
		this.onfail = value;
	}

	/**
	 * Gets the value of the onok property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getONOK() {
		return onok;
	}

	/**
	 * Sets the value of the onok property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setONOK(String value) {
		this.onok = value;
	}

	@Override
	public String toString() {
		return "QuantitativeResourceData [name=" + name + ", quant=" + quant + ", onfail=" + onfail + ", onok=" + onok + "]";
	}

}
