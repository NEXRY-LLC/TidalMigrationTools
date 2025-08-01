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
 * Java class for WCMNote complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="WCMNote"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="MESSAGE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="TIME_STAMP" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="NOTE_CREATOR"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="REQUESTER"/&gt;
 *             &lt;enumeration value="SCHEDULER"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="USER_NAME" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WCMNote")
public class WCMNote {

	@XmlAttribute(name = "MESSAGE")
	protected String message;
	@XmlAttribute(name = "TIME_STAMP")
	protected String timestamp;
	@XmlAttribute(name = "NOTE_CREATOR")
	protected String notecreator;
	@XmlAttribute(name = "USER_NAME")
	protected String username;

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
	 * Gets the value of the timestamp property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getTIMESTAMP() {
		return timestamp;
	}

	/**
	 * Sets the value of the timestamp property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setTIMESTAMP(String value) {
		this.timestamp = value;
	}

	/**
	 * Gets the value of the notecreator property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getNOTECREATOR() {
		return notecreator;
	}

	/**
	 * Sets the value of the notecreator property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setNOTECREATOR(String value) {
		this.notecreator = value;
	}

	/**
	 * Gets the value of the username property.
	 *
	 * @return
	 *         possible object is
	 *         {@link String }
	 *
	 */
	public String getUSERNAME() {
		return username;
	}

	/**
	 * Sets the value of the username property.
	 *
	 * @param value
	 *              allowed object is
	 *              {@link String }
	 *
	 */
	public void setUSERNAME(String value) {
		this.username = value;
	}

}
