package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvPeopleSoftJob;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.service.ServiceJob;
import com.bluehouseinc.tidal.api.model.service.Service;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class PeopleSoftJobTransformer implements ITransformer<CsvPeopleSoftJob, ServiceJob> {

	ServiceJob base;
	TidalAPI tidal;
	private static Integer serviceid;

	public static final String GUID = "{B20EC120-2EB5-4d5f-8133-73FA37225667}";

	public PeopleSoftJobTransformer(ServiceJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public ServiceJob transform(CsvPeopleSoftJob in) throws TransformationException {

		this.base.setType(JobType.PSJOB);
		this.base.setExtendedinfo(in.getExtendedInfo());

		if (serviceid == null) {
			Service adapter = tidal.getAdapterByGUID(GUID);

			if (adapter == null) {
				throw new RuntimeException("Check that the PS adapter is installed in this master");
			}

			serviceid = adapter.getId();
		}

		this.base.setServiceid(serviceid);
		this.base.setServicename("PeopleSoft Adapter");

		return this.base;
	}

}
