package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;

public class CcCheckFileWriter {

	private static File ccode; // new File(this.espdatamodel.getConfigeProvider().getCcodeDataFile());
	private static FileWriter ccodeWriter;

	public CcCheckFileWriter(String file) {
		init(file);
	}

	private static void init(String file) {
		try {
			if (ccode == null) {
				ccode = new File(file);

			}

			if (ccodeWriter == null) {
				ccodeWriter = new FileWriter(ccode);
			}
		} catch (IOException e) {
			// eat
		}
	}

	public void processCcChecksRanges(List<CcCheck> checks, EspZosJob job) {

		final List<Integer> fullrangecheck = IntStream.range(0, 4095).boxed().collect(Collectors.toList());
		final int rangesize = fullrangecheck.size();

		checks.forEach(ccchk -> {

			List<Integer> jobrange = IntStream.range(ccchk.getRangeStartCode(), ccchk.getRangeEndCode() + 1).boxed().collect(Collectors.toList());
			// This is our range of data
			ccchk.isOkContinue(); // If FAIL that means anything outside of this range

			jobrange.forEach(s -> {
				fullrangecheck.remove(Integer.valueOf(s));
			});

			fullrangecheck.remove(Integer.valueOf(0)); // Just remove this, not sure why we are picking up zero
		});

		if (fullrangecheck.size() != rangesize) {
			// Was modified so we need to process this list.
			for (int value : fullrangecheck) {
				String linetowrite = job.getName() + ".*=" + value;
				try {
					ccodeWriter.append(linetowrite);
					ccodeWriter.append(System.lineSeparator());
					ccodeWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public void processCcCheckStepProcess(List<CcCheck> checks, EspZosJob job) {
		// Was modified so we need to process this list.
		checks.forEach(ccchk -> {
			String linetowrite = job.getName() + "." + ccchk.getProccessName() + "." + ccchk.getProcessStepName() + "=" + ccchk.getProcessReturnCode();
			try {
				ccodeWriter.append(linetowrite);
				ccodeWriter.append(System.lineSeparator());
				ccodeWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		});

	}

	public void processCcCheckSingleCheck(CcCheck check, EspZosJob job) {
		// Was modified so we need to process this list.

		String linetowrite = job.getName() + ".*=" + check.getSingleReturnCode();
		
		try {
			ccodeWriter.append(linetowrite);
			ccodeWriter.append(System.lineSeparator());
			ccodeWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
