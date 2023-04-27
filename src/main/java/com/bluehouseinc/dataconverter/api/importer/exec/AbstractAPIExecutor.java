package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.AbstractExecutor;
import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAPIExecutor extends AbstractExecutor {

	private TidalAPI tidalApi;
	private TidalDataModel dataModel;
	private ProgressBarBuilder pbb;

	public AbstractAPIExecutor(TidalAPI tdl, TidalDataModel mdl, ConfigurationProvider cfgProvider) {
		super(cfgProvider);
		tidalApi = tdl;
		dataModel = mdl;
		this.pbb = new ProgressBarBuilder().setInitialMax(getProgressBarTotal()).setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK).setTaskName(getProgressBarName());
		// pbb.clearDisplayOnFinish();
		// pbb.continuousUpdate();
		pbb.hideETA();
		pbb.setMaxRenderedLength(120);

	}

	public abstract void doExecute(ExecutorService executor, ProgressBar bar);

	public abstract void doExecute(ProgressBar bar);

	@Override
	public void doExecute() {
		doExecute(this.pbb.build());
	}

	@Override
	public void doExecute(ExecutorService executor) {
		doExecute(executor, this.pbb.build());
	}

	@Override
	protected int getThreadCount() {
		return 0;
	}

	public abstract String getProgressBarName();

	public abstract int getProgressBarTotal();

}
