package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.tidal.api.model.TrueFalse;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileWatcherJob;
import com.bluehouseinc.tidal.api.model.job.filewatcher.TimeUnit;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class FileWatcherJobTransformer implements ITransformer<CsvFileWatcherJob, FileWatcherJob> {

	FileWatcherJob base;
	TidalAPI tidal;

	public FileWatcherJobTransformer(FileWatcherJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public FileWatcherJob transform(CsvFileWatcherJob in) throws TransformationException {

		this.base.setType(JobType.FILEWATCHER);

		if(StringUtils.isBlank(in.getFilemask())){
			in.setFilemask("*");
		}
		
		if(in.getFileExist() == TrueFalse.YES) {
			this.base.doSetFileExist(in.getDirectory(), in.getFilemask());
		}else {
			this.base.doSetFileNotExist(in.getDirectory(), in.getFilemask());
		}

		if (in.getFileActivity() != null) {
			this.base.doSetFileWatcher(in.getFileActivity(), in.getFileActivityInterval(), in.getFileActivityTimeUnit(), null, null);
		}

		if (in.getPollInterval() != null) {
			this.base.setPollinterval(in.getPollInterval());
			this.base.setPollintervalunit(TimeUnit.SECOND);
		}

		if (in.getPollMaxDuration() != null) {
			this.base.setPolllifetime(in.getPollMaxDuration());
			this.base.setPolllifetimeunit(TimeUnit.SECOND);
		}

		if(in.getRecursive()==TrueFalse.YES) {
			this.base.doSetSearchSubDirectories();
		}
		
		if(in.getPollContinuously()== TrueFalse.YES) {
			this.base.doSetPollAwlays(null, null);
		}
		this.base.setCommand("//$FileWatcher");
		
		return this.base;
	}

}
