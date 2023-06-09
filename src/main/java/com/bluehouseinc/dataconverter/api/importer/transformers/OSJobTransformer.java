package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.tidal.api.model.job.ExitCodeNormalOperatorType;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.os.OSJob;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class OSJobTransformer implements ITransformer<CsvOSJob, OSJob> {

	OSJob base;
	TidalAPI tidal;

	public OSJobTransformer(OSJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public OSJob transform(CsvOSJob in) throws TransformationException {

		this.base.setType(JobType.OSJOB);

		if (in.getCommandLine() != null) {
			this.base.setCommand(in.getCommandLine());
		} else {
			this.base.setCommand("ERROR");
		}

		this.base.setParameters(in.getParamaters());
		this.base.setWorkingdirectory(in.getWorkingDirectory());
		this.base.setEnvironmentfile(in.getEnvironmentFile());
		this.base.setAlternateoutputfile(in.getAlternativeOutputFile());

		if (in.getExitcode() != null) {
			Integer start = in.getExitcode().getExitStart();
			Integer end = in.getExitcode().getExitEnd();

			if (start != null) {
				// If not set, set to start;
				end = end == null ? start : end;

				switch (in.getExitcode().getExitLogic()) {

				case EQ:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.EQ);
					break;
				case NE:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.NE);
					break;
				case LT:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.LT);
					break;
				case GT:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.GT);
					break;
				case LTE:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.LTE);
					break;
				case GTE:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.GTE);
					break;
				case ODD:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.ODD);
					break;
				case EVEN:
					this.base.setExitcodenormaloperator(ExitCodeNormalOperatorType.EVEN);
					break;
				}
				
				this.base.setNormalexitfromrange(start);
				this.base.setNormalexittorange(end);
			}
		}
		return this.base;
	}

}
