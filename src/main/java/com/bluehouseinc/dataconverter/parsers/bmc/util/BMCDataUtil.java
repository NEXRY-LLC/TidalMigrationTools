package com.bluehouseinc.dataconverter.parsers.bmc.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.InConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OutConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.QuantitativeResourceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

public class BMCDataUtil {

	/**
	 * OUTCOND - BMC Objects set these to let other BMC Objects know that they have
	 * done something.
	 *
	 * @param data
	 * @return
	 */
	public static List<OutConditionData> getOutConditionData(List<JAXBElement<?>> data) {
		return getDataType(data, OutConditionData.class);
	}

	/**
	 * INCOND - BMC Objects set these as the conditions they need in order to run.
	 * Looking for a OUTCOND from another BMC object
	 *
	 * @param data
	 * @return
	 */
	public static List<InConditionData> getInConditionData(List<JAXBElement<?>> data) {
		return getDataType(data, InConditionData.class);
	}

	/**
	 *
	 * @param data
	 * @return
	 */
	public static List<SetVarData> getSetVarData(List<JAXBElement<?>> data) {
		return getDataType(data, SetVarData.class);
	}

	public static List<QuantitativeResourceData> getQuantitativeResourceData(List<JAXBElement<?>> data) {
		return getDataType(data, QuantitativeResourceData.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getDataType(List<JAXBElement<?>> data, Class<T> classob) {
		List<T> trans = new ArrayList<>();
		if (!data.isEmpty()) {

			for (JAXBElement<?> el : data) {

				Object obj = el.getValue();
				if (classob.isInstance(obj)) {
					trans.add((T) obj);
				}
			}

			if (trans.isEmpty()) {
				// System.out.println("No Data Found for Class [" + classob.getName() + "]");
			} else {
				// System.out.println("Found["+trans.size()+"] for Class [" + classob.getName()+ "]");
			}
		}
		return trans;
	}

}
