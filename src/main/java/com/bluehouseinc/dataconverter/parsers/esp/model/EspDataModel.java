package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;



@Component
public class EspDataModel extends BaseParserDataModel<EspAbstractJob,EspConfigProvider> implements IParserModel {


	private DependencyGraphMapper depgraph;

	public EspDataModel(ConfigurationProvider cfgProvider) {
		super(new EspConfigProvider(cfgProvider));
		this.depgraph = new DependencyGraphMapper(getConfigeProvider(), getTidal());
	}

	@Override
	public BaseVariableProcessor<EspAbstractJob> getVariableProcessor(TidalDataModel model) {
		return new EspVariableProcessor(model);
	}

	@Override
	public ITransformer<List<EspAbstractJob>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return  new EspToTidalTransformer2(model);
	}

	@Override
	public void doProcessData(List<EspAbstractJob> dataObjects) {

		dataObjects.forEach(jobGroupObject -> doProcessJobDeps(jobGroupObject));

	}





	// Not currently used, but logic for converting into TIDAL emailActions exists if it needs to be used later.
	public void buildEmailActions(BaseJobOrGroupObject baseJobOrGroupObject) {
		if (baseJobOrGroupObject instanceof EspJobGroup) {
			EspJobGroup currentJobGroup = (EspJobGroup) baseJobOrGroupObject;
			List<String> jobGroupNotifications = currentJobGroup.getEspJobGroupNotifyList();
			if (jobGroupNotifications != null && !jobGroupNotifications.isEmpty()) {
				currentJobGroup.getEspJobGroupNotifyList().forEach(notify -> {
					// RegEx for search: NOTIFY (.*) MAILBOX
					// NOTIFY ABEND FAILURE ALERT(ADW1) MAILBOX(DGRIMMETT1)
					if (notify.contains("MAILBOX")) {
						CsvActionEmail csvActionEmail = new CsvActionEmail();

						String[] notifyElements = notify.split("MAILBOX", 2);
						String mailBoxString = notifyElements[1];
						// emailAddressGroup <=> boxName <= MAILBOX(boxName)
						String emailAddressGroup = mailBoxString.substring(mailBoxString.indexOf("(") + 1, mailBoxString.indexOf(")"));

						csvActionEmail.setToEmailAddresses(emailAddressGroup);
						String csvActionEmailName = currentJobGroup.getName() + emailAddressGroup;

						if (notify.contains("SUBJECT")) {
							String subject = mailBoxString.substring(mailBoxString.lastIndexOf("(") + 1, mailBoxString.lastIndexOf(")"));
							csvActionEmailName += subject;
						}
						csvActionEmail.setName(csvActionEmailName);

						this.getTidal().addOwnerToAction(csvActionEmail, this.getTidal().getDefaultOwner());
						this.getTidal().getEmailActions().add(csvActionEmail);
					}
				});

				if ((currentJobGroup).isGroup()) {
					(currentJobGroup).getChildren().forEach(this::buildEmailActions);
				}
			}
		} else {
			EspAbstractJob currentJob = (EspAbstractJob) baseJobOrGroupObject;
			List<String> jobNotifications = currentJob.getNotifyList();
			if (jobNotifications != null && !jobNotifications.isEmpty()) {
				currentJob.getNotifyList().forEach(notify -> {
					// RegEx for search: NOTIFY (.*) MAILBOX
					// NOTIFY ABEND FAILURE ALERT(ADW1) MAILBOX(DGRIMMETT1)
					if (notify.contains("MAILBOX")) {
						CsvActionEmail csvActionEmail = new CsvActionEmail();

						String[] notifyElements = notify.split("MAILBOX", 2);
						String mailBoxString = notifyElements[1];

						// emailAddressGroup <=> boxName <= MAILBOX(boxName)
						String emailAddressGroup = mailBoxString.substring(mailBoxString.indexOf("(") + 1, mailBoxString.indexOf(")"));
						csvActionEmail.setToEmailAddresses(emailAddressGroup);
						String csvActionEmailName = currentJob.getName() + emailAddressGroup;

						if (notify.contains("SUBJECT")) {
							String subject = mailBoxString.substring(mailBoxString.lastIndexOf("(") + 1, mailBoxString.lastIndexOf(")"));
							csvActionEmailName += subject;
						}
						csvActionEmail.setName(csvActionEmailName);

						this.getTidal().addOwnerToAction(csvActionEmail, this.getTidal().getDefaultOwner());
						this.getTidal().getEmailActions().add(csvActionEmail);
					}
				});
			}
		}
	}

	/**
	 * Helper method to loop over all the jobs that we registered within the system and set up the dependency objects
	 */
	public void doProcessJobDeps(BaseJobOrGroupObject jobGroupObject) {
		if (jobGroupObject instanceof EspJobGroup) { // This is why everything should be from single abstract object
			if (!jobGroupObject.getChildren().isEmpty()) {
				jobGroupObject.getChildren().forEach(f -> this.depgraph.doProcessJobDeps((EspAbstractJob) f));
			}
		}
	}

}
