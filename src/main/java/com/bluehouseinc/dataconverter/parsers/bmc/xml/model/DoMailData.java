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

import lombok.ToString;

/**
 * <p>
 * Java class for DoMailData complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DoMailData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="URGENCY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DEST" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="CC_DEST" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="SUBJECT" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="MESSAGE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ATTACH_SYSOUT" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DoMailData")
public class DoMailData {

	@XmlAttribute(name = "URGENCY")
	protected String urgency;
	@XmlAttribute(name = "DEST")
	protected String dest;
	@XmlAttribute(name = "CC_DEST")
	protected String ccdest;
	@XmlAttribute(name = "SUBJECT")
	protected String subject;
	@XmlAttribute(name = "MESSAGE")
	protected String message;
	@XmlAttribute(name = "ATTACH_SYSOUT")
	protected String attachsysout;

	/**
	 * Gets the value of the urgency property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getURGENCY() {
		return urgency;
	}

	/**
	 * Sets the value of the urgency property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setURGENCY(String value) {
		this.urgency = value;
	}

	/**
	 * Gets the value of the dest property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getDEST() {
		return dest;
	}

	/**
	 * Sets the value of the dest property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setDEST(String value) {
		this.dest = value;
	}

	/**
	 * Gets the value of the ccdest property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getCCDEST() {
		return ccdest;
	}

	/**
	 * Sets the value of the ccdest property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setCCDEST(String value) {
		this.ccdest = value;
	}

	/**
	 * Gets the value of the subject property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getSUBJECT() {
		return subject;
	}

	/**
	 * Sets the value of the subject property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setSUBJECT(String value) {
		this.subject = value;
	}

	/**
	 * Gets the value of the message property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getMESSAGE() {
		return message;
	}

	/**
	 * Sets the value of the message property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setMESSAGE(String value) {
		this.message = value;
	}

	/**
	 * Gets the value of the attachsysout property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getATTACHSYSOUT() {
		return attachsysout;
	}

	/**
	 * Sets the value of the attachsysout property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setATTACHSYSOUT(String value) {
		this.attachsysout = value;
	}

}
