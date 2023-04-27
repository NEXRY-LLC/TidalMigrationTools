package com.bluehouseinc.dataconverter.parsers.bmc.model.xml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class AccountParser {
	private static String PREFIX = "accounts-";
	private static String EXT = ".xml";

	final List<NodeKey> cache = new LinkedList<>();

	private String container; // Location of our files to load.

	public AccountParser(String container) {
		this.container = container;
		init();
	}

	private void init() {
		// Keep is simple and just remove the darn last char so we can build a new file name using system char.
		if (this.container.endsWith("\\") || this.container.endsWith("/")) {
			this.container = this.container.substring(0, this.container.length() - 1);
		}

		File dir = new File(this.container);
		if (dir.isDirectory()) {

			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(PREFIX) && name.endsWith(EXT);
				}
			});

			Arrays.stream(files).collect(Collectors.toList()).forEach(f -> initXMLData(f));

		} else {
			throw new TidalException("Directory[" + dir.getAbsolutePath() + "] is not valid, please check the path");
		}

	}

	/**
	 *
	 * @param nodeid - The nodeid listed in the XML export, we need to load each and every one of them or only the ones we have?
	 *
	 * @throws JAXBException
	 * @throws XMLStreamException
	 */
	private void initXMLData(File file) {

		// And we have not already processed this file

		try {
			JAXBContext jc = JAXBContext.newInstance(Accounts.class);

			XMLInputFactory xif = XMLInputFactory.newInstance();// .newFactory();
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);
			XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(file));
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Accounts accts = (Accounts) unmarshaller.unmarshal(xsr);

			String nodeid = file.getName().replace(PREFIX, "").replace(EXT, "").toLowerCase();

			accts.getAccounts().forEach(f -> {
				NodeKey nodekey = new NodeKey(nodeid, f.getAccountName(), f);
				if (!cache.contains(nodekey)) {
					cache.add(nodekey);
				}
			});

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws JAXBException, XMLStreamException {
		String dir = "./cfg/genex/accountfiles/";

		String nodeid = "phlprdedi01";//phlprdedi01
		String accountname = "ZZARCH_FSRMF_SFTP";

		AccountParser p = new AccountParser(dir);

		NodeKey key = new NodeKey(nodeid, accountname);

		p.loadAccount(key);

	}

	public void loadAccount(NodeKey key) {

		NodeKey cachekey = cache.stream().filter(f -> f.equals(key)).findFirst().orElse(null);

		if (cachekey != null) {
			key.setAccount(cachekey.getAccount());
		}

	}
}
