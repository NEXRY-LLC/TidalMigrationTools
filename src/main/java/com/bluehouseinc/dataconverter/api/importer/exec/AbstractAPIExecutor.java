package com.bluehouseinc.dataconverter.api.importer.exec;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.AbstractExecutor;
import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.exceptions.TesResultExceptionHandler;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.exceptions.TidalHttpResponseException;
import com.bluehouseinc.tidal.api.exceptions.TidalUnauthorizedException;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.google.api.client.http.HttpResponseException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

@Log4j2
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

	public <C extends BaseAPIObject> TesResult doCreate(C object) {
		return doCreate(object, 0);
	}

	private <C extends BaseAPIObject> TesResult doCreate(C object, int attemptNumber) {

		final int MAX_AUTH_RETRIES = 20;
		final long RETRY_DELAY_MS = 2000;

		try {

			TesResult res = getExecutorAPI(object).create(object);

			return res;

		} catch (Exception e) {

			// Check if this is an authentication error we should retry
			if (isAuthenticationError(e)) {
				log.info("Authentication failure creating object [{}]. Attempting recovery (attempt {} of {})...", object.getClass().getCanonicalName(), attemptNumber + 1, MAX_AUTH_RETRIES + 1);

				// Attempt to recover authentication
				if (attemptAuthenticationRecovery(0)) {
					log.info("Authentication recovery successful. Retrying object creation for [{}]...", object.getClass().getCanonicalName());

					try {
						Thread.sleep(RETRY_DELAY_MS);
					} catch (InterruptedException e1) {
						// e1.printStackTrace();
					}
					// Retry the operation
					return doCreate(object, attemptNumber + 1);

				} else {
					log.info("Authentication recovery failed for object [{}]. Giving up after {} attempts.", object.getClass().getCanonicalName(), attemptNumber + 1);
					throw new TidalException("Failed to create object [" + object.getClass().getCanonicalName() + "] - authentication recovery failed", e);
				}

			} else {
				// Don't retry - either not an auth error or exceeded max retries
				String reason = !isAuthenticationError(e) ? "not an authentication error" : "exceeded maximum retry attempts (" + MAX_AUTH_RETRIES + ")";

				
				if(e instanceof TidalHttpResponseException){
					log.error("Failed to create object [{}] - {} (attempt {})", object.getClass().getCanonicalName(), reason, attemptNumber + 1);
					throw new TidalException("Failed to create object [" + object.getClass().getCanonicalName() + "] - " + reason, e);
				}else {
					
					throw new RuntimeException(reason,e);
				}
		
			}
		}
	}

	public abstract <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object);

	/**
	 * Check if the exception is an authentication-related error
	 */
	private boolean isAuthenticationError(Exception e) {
		if (e instanceof TidalUnauthorizedException) {
			return true;
		}

		if (e instanceof TidalHttpResponseException) {
			TidalHttpResponseException httpEx = (TidalHttpResponseException) e;
			// Check if the underlying cause is an HTTP 401
			Throwable cause = httpEx.getCause();
			if (cause instanceof HttpResponseException) {
				HttpResponseException httpResponseEx = (HttpResponseException) cause;
				return httpResponseEx.getStatusCode() == 401;
			}
		}

		if (e instanceof SocketTimeoutException) {
			return false;
		}

		return false;
	}

	// Alternative implementation if you don't have direct access to force re-auth:
	/**
	 * Alternative authentication recovery - recreate the session
	 */
	private boolean attemptAuthenticationRecovery(int attempts) {

		final int MAX_AUTH_RETRIES = 20;
		final long RETRY_DELAY_MS = 2000;

		if (MAX_AUTH_RETRIES <= attempts) {
			throw new TidalSessionRecoverException("FAILED... Attempting authentication recovery by recreating session...{" + attempts + "}");
		}
		try {
			log.info("Attempting authentication recovery by recreating session...{}", attempts);

			// This approach would recreate the session entirely
			// Add small delay before retry
			Thread.sleep(RETRY_DELAY_MS);

			getTidalApi().getSession().fullReset();

			//getTidalApi().getSession().login();

			log.info("Authentication recovery successful - session recreated...{}", attempts);
			return true;

		} catch (RuntimeException e) {
			return attemptAuthenticationRecovery(attempts + 1);
		} catch (InterruptedException e) {
		}

		return false;
	}

	private class TidalSessionRecoverException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public TidalSessionRecoverException(String m) {
			super(m);
		}
	}
}
