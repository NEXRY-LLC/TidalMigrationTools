package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Component
@Log4j2
public class MailListDataProcessor {
	private final static String MAILBOX_PATTERN = "MAILBOX\\s+(\\S+)";
	private final static String MAILBOX_EMAIL_PATTERN = "EMAIL\\s+(\\S+)";

	List<MailBoxDetails> elements;

	public MailListDataProcessor() {

	}

	public void doProcessMailListData(String datafile) {

		this.elements = new LinkedList<>();

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing esp.mail.datafile entry");
			}

			MailBoxDetails maildetail = null;
			
			reader = new BufferedReader(new FileReader(datafile));

			String line;
			
			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}

				// Fix our newline
				line = EspFileReaderUtils.readLineMerged(reader, line, '-');

				if (isMailDataPattern(line)) {
					String mailboxname = RegexHelper.extractFirstMatch(line, MAILBOX_PATTERN);
					maildetail = new MailBoxDetails(mailboxname.toUpperCase());
					elements.add(maildetail);
				}else if(line.startsWith("EMAIL")){
					// Email Line to our maildetail
					String address = RegexHelper.extractFirstMatch(line, MAILBOX_EMAIL_PATTERN);
					maildetail.addEmailAddress(address);
				}
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isMailDataPattern(String line) {
		return RegexHelper.matchesRegexPattern(line, MAILBOX_PATTERN);
	}


	public MailBoxDetails getMailDetails(String boxname) {
		return this.getElements().stream().filter(f -> f.getName().equalsIgnoreCase(boxname)).findAny().orElse(null);
	}
	
	@Data
	public class MailBoxDetails{
		String name;
		List<String> emailAdresses;
		
		public MailBoxDetails(String name) {
			this.name = name;
			this.emailAdresses = new ArrayList<>();
		}
		
		public void addEmailAddress(String addr) {
			if(this.getEmailAdresses().contains(addr)) {
				// skip
				return;
			}
			
			this.getEmailAdresses().add(addr);
		}
	}
}
