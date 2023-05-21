package com.bluehouseinc.dataconverter.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.reporters.GenericJobTypeReporter;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public abstract class AbstractParser<E extends BaseParserDataModel<?, ?>> implements IParser {

	private E parserDataModel;

	public AbstractParser(E parserDataModel) {
		this.parserDataModel = parserDataModel;
	}

	protected void parseFile() throws Exception {
		throw new RuntimeException("Need to override");
	}

	@Override
	public TidalDataModel processDataModel() throws Exception {

		parseFile(); // Parse our source data.

		// Finally convert from our parser data model into our Tidal Data Model
		TidalDataModel model = getParserDataModel().convertToDomainDataModel();

		List<IReporter> reporters = new LinkedList<>();
		reporters.add(new GenericJobTypeReporter());

		if (getModelReporter() != null) {
			reporters.addAll(getModelReporter().getReporters());
		}
		// Do some printing if we can on our parser data model.
		reporters.forEach(r -> {
			log.trace("#######################################{}#######################################", r.getClass().getSimpleName());

			r.doPrint(getParserDataModel());

			log.trace("#######################################{}#######################################", r.getClass().getSimpleName());

		});

		return model;

	}

	protected void skipNextLines(BufferedReader reader, int linesCount) throws IOException {
		if (linesCount > 0) {
			for (int i = 0; i < linesCount; i++) {
				reader.readLine();
			}
		}
	}

	// public E getParserDataModel() {
	// return this.parserDataModel;
	// }

	protected Map.Entry<Integer, Integer> getValuePosition(Integer columnGroupPos, Integer columnPos, List<List<String>> columns) {
		int startPos = 1;
		for (int i = 0; i <= columnGroupPos; i++) {
			// startPos++;
			int length = i == columnGroupPos ? columnPos : columns.get(i).size();
			for (int j = 0; j < length; j++) {
				startPos++;
				startPos += columns.get(i).get(j).length();
			}
		}

		return new AbstractMap.SimpleEntry<>(startPos, startPos + columns.get(columnGroupPos).get(columnPos).length());
	}

	protected List<List<String>> generateColumns(String headerLine, Function<String, String> sanitizeFunc) {
		List<List<String>> columns = new ArrayList<>();
		List<String> columnGroups = Arrays.asList(headerLine.trim().split("\\|"));
		for (String columnGroup : columnGroups) {
			String sanitized = sanitizeFunc != null ? sanitizeFunc.apply(columnGroup) : columnGroup;
			List<String> list = Arrays.asList(sanitized
					// this will fix empty spaces between columns
					// .replaceAll("\\s{1}(?!-)", "-")
					.split(" "));
			columns.add(list);
		}

		return columns;
	}

	protected String readLine(final BufferedReader reader) throws IOException {
		String line = reader.readLine();
		return line != null ? line.trim() : null;
	}
}
