package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.MailListDataProcessor.MailBoxDetails;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;

@Component
public class EspDataModel extends BaseParserDataModel<EspAbstractJob, EspConfigProvider> implements IParserModel {

	private DependencyGraphMapper depgraph;
	private ScheduleEventDataProcessor schedDataProcessor;
	private MailListDataProcessor mailProcessor;

	
	public EspDataModel(ConfigurationProvider cfgProvider, ScheduleEventDataProcessor schedDataProcessor, MailListDataProcessor mailProcessor) {
		super(new EspConfigProvider(cfgProvider));
		this.depgraph = new DependencyGraphMapper(getConfigeProvider(), getTidal(), this);
		this.schedDataProcessor = schedDataProcessor;
		this.mailProcessor = mailProcessor;
	}

	@Override
	public BaseVariableProcessor<EspAbstractJob> getVariableProcessor(TidalDataModel model) {
		return new EspVariableProcessor(model);
	}

	@Override
	public ITransformer<List<EspAbstractJob>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new EspToTidalTransformer2(model,this);
	}

	@Override
	public void doPostTransformJobObjects(List<EspAbstractJob> jobs) {
		jobs.forEach(jobGroupObject -> buildEmailActions(jobGroupObject));

	}

	@Override
	public void doProcessJobDependency(List<EspAbstractJob> jobs) {
		this.depgraph.setupSleepDependency();
		jobs.forEach(jobGroupObject -> doProcessJobDepsForJob(jobGroupObject));

	}

	// Not currently used, but logic for converting into TIDAL emailActions exists if it needs to be used later.
	public void buildEmailActions(EspAbstractJob job) {

		List<String> emaillist = job.getNotifyList();

		if (!emaillist.isEmpty()) {
			emaillist.forEach(notify -> {
				// RegEx for search: NOTIFY (.*) MAILBOX
				// NOTIFY ABEND FAILURE ALERT(ADW1) MAILBOX(DGRIMMETT1)
				if (notify.contains("MAILBOX")) {

					CsvActionEmail csvActionEmail = new CsvActionEmail();

					String[] notifyElements = notify.split("MAILBOX", 2);
					String mailBoxString = notifyElements[1];

					// emailAddressGroup <=> boxName <= MAILBOX(boxName)
					String emailAddressGroup = mailBoxString.substring(mailBoxString.indexOf("(") + 1, mailBoxString.indexOf(")"));
					
					String csvActionEmailName = emailAddressGroup;
					csvActionEmail.setName(csvActionEmailName);

					if (notify.contains("SUBJECT")) {
						String subject = mailBoxString.substring(mailBoxString.lastIndexOf("(") + 1, mailBoxString.lastIndexOf(")"));
						// csvActionEmailName += subject;
						csvActionEmail.setSubject(subject);
						csvActionEmail.setToEmailAddresses(emailAddressGroup);
					} else {
						String subject = this.getConfigeProvider().getEspMailSubject();
						String message = this.getConfigeProvider().getEspMailBody();

						MailBoxDetails details = this.getMailListDataProcessor().getMailDetails(csvActionEmailName);

						if (details != null && !details.getEmailAdresses().isEmpty()) {
							String emailaddresslist = String.join(",", details.getEmailAdresses());
							csvActionEmail.setSubject(subject);
							csvActionEmail.setToEmailAddresses(emailaddresslist);
							csvActionEmail.setMessage(message);
						}else {
							
						}

					}

					this.getTidal().addOwnerToAction(csvActionEmail, this.getTidal().getDefaultOwner());
					this.getTidal().getEmailActions().add(csvActionEmail);
				}
			});
		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(f -> buildEmailActions((EspAbstractJob) f));
		}

	}

	/**
	 * Helper method to loop over all the jobs that we registered within the system and set up the dependency objects
	 */
	public void doProcessJobDepsForJob(BaseJobOrGroupObject jobGroupObject) {

		this.depgraph.doProcessJobDepsForJob((EspAbstractJob) jobGroupObject);

		if (jobGroupObject instanceof EspJobGroup) { // This is why everything should be from single abstract object
			if (!jobGroupObject.getChildren().isEmpty()) {
				jobGroupObject.getChildren().forEach(f -> doProcessJobDepsForJob((EspAbstractJob) f));
			}
		}
	}

	public ScheduleEventDataProcessor getScheduleEventDataProcessor() {
		return this.schedDataProcessor;
	}

	public MailListDataProcessor getMailListDataProcessor() {
		return this.mailProcessor;
	}

	@Override
	public void doPostJobDependencyJobObject(List<EspAbstractJob> jobs) {
		// TODO Auto-generated method stub
		
	}

}
