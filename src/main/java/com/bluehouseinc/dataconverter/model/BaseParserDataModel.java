package com.bluehouseinc.dataconverter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public abstract class BaseParserDataModel<E extends BaseJobOrGroupObject, P extends AbstractConfigProvider> implements IParserModel {

	@Getter(value = AccessLevel.PUBLIC)
	private P configeProvider;

	private List<E> dataObjects = new ArrayList<>();

	private BaseVariableProcessor<E> variableProcessor;

	@Getter(value = AccessLevel.PUBLIC)
	private ITransformer<List<E>, TidalDataModel> jobTransformer;

	public BaseParserDataModel(P configeProvider) {
		this.configeProvider = configeProvider;
		this.variableProcessor = getVariableProcessor(getTidal());
		this.jobTransformer = getJobTransformer(getTidal());
	}

	@Override
	public TidalDataModel convertToDomainDataModel() {
		// We do not want empty groups to be included.

		this.doProcessEmptyGroupLogic();

		if (this.variableProcessor != null) {

			log.info("convertToDomainDataModel.getVariableProcessor={} Starting."); // debugging
			long startTime = System.currentTimeMillis(); // debugging
			this.getDataObjects().stream().forEach(getVariableProcessor()::processJob);
			long endTime = System.currentTimeMillis(); // debugging
			long finishtime = (endTime - startTime) / 1000; // debugging
			log.info("convertToDomainDataModel.getVariableProcessor={} seconds", finishtime); // debugging

		}

		if (this.jobTransformer != null) {
			try {

				log.info("convertToDomainDataModel.getTransformer Starting."); // debugging
				long startTime = System.currentTimeMillis(); // debugging
				this.getJobTransformer().transform(dataObjects);
				long endTime = System.currentTimeMillis(); // debugging
				long finishtime = (endTime - startTime) / 1000; // debugging
				log.info("convertToDomainDataModel.getTransformer={} seconds", finishtime); // debugging

				log.info("doPostTransformJobObject Starting."); // debugging
				long startTimej = System.currentTimeMillis(); // debugging
				doPostTransformJobObjects(this.dataObjects);
				long endTimej = System.currentTimeMillis(); // debugging
				long doPostTransformJobObjectsTime = (endTimej - startTimej) / 1000; // debugging
				log.info("doPostTransformJobObject Time={} seconds", doPostTransformJobObjectsTime); // debugging

			} catch (TransformationException e) {
				throw new TidalException(e);
			}
		}

		if (getConfigeProvider().skipDependencyProcessing()) {
			log.info("doProcessJobDependency Skipping. skipDependencyProcessing() == true"); // debugging
		} else {

			log.info("doProcessJobDependency Starting."); // debugging
			long startTime = System.currentTimeMillis(); // debugging
			doProcessJobDependency(this.dataObjects);
			long endTime = System.currentTimeMillis(); // debugging
			long jobDependencyProcessingTime = (endTime - startTime) / 1000; // debugging
			log.info("doProcessJobDependency Time={} seconds", jobDependencyProcessingTime); // debugging

		}

		log.info("doPostTransformJobObject Starting."); // debugging
		long startTimedp = System.currentTimeMillis(); // debugging
		doPostJobDependencyJobObject(this.dataObjects);
		long endTimejpb = System.currentTimeMillis(); // debugging
		long doPostTransformJobObjectsTimedp = (startTimedp - endTimejpb) / 1000; // debugging
		log.info("doPostTransformJobObjectsTimedp Time={} seconds", doPostTransformJobObjectsTimedp); // debugging

		TidalDataModelStats statprinter = new TidalDataModelStats(getTidal());
		statprinter.displayFullStatistics();
		// this.datamodel.getDependencyProcessor().getEnhancedModelStatistics().printStatistics();

		return this.getTidal();
	}

	public TidalDataModel getTidal() {
		return TidalDataModel.instance(getConfigeProvider());
	}

	public abstract void doPostTransformJobObjects(List<E> jobs);

	public abstract void doProcessJobDependency(List<E> jobs);

	public abstract BaseVariableProcessor<E> getVariableProcessor(TidalDataModel model);

	public abstract ITransformer<List<E>, TidalDataModel> getJobTransformer(TidalDataModel model);

	public abstract void doPostJobDependencyJobObject(List<E> jobs);

	/**
	 * Add a new top-level object to our datamodel with TIDAL validation.
	 * Enforces the rule: no two top-level objects can have the same name.
	 *
	 * @param obj The object to add
	 * @throws IllegalArgumentException if top-level name conflicts with existing object
	 */
	public void addDataObject(E obj) {
		// TIDAL Validation: Check for name conflicts at top level
		if (hasTopLevelObjectWithName(obj.getName())) {
			String errorMsg = String.format("TIDAL Top-Level Validation Error: Cannot add top-level object '%s' - name already exists.", obj.getName(), getTopLevelObjectByName(obj.getName()).getFullPath(), obj.getName());
			
			log.error(errorMsg);
			
			throw new IllegalArgumentException(errorMsg);
		}

		this.dataObjects.add(obj);
		
		log.debug("Added top-level object '{}' with path: {}", obj.getName(), obj.getFullPath());
	}

	public boolean addDataObjectTest(E obj) {
		try {
			addDataObject(obj);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check if a top-level object with the given name already exists.
	 * Case-insensitive comparison for TIDAL compatibility.
	 */
	private boolean hasTopLevelObjectWithName(String name) {
		if (name == null) {
			return false;
		}

		return dataObjects.stream().anyMatch(obj -> name.equalsIgnoreCase(obj.getName()));
	}

	/**
	 * Get top-level object by name (case-insensitive).
	 * Returns null if not found.
	 */
	private E getTopLevelObjectByName(String name) {
		if (name == null) {
			return null;
		}

		return dataObjects.stream().filter(obj -> name.equalsIgnoreCase(obj.getName())).findFirst().orElse(null);
	}

	public List<E> getBaseObjectsNameBeginsWith(String name) {
		List<E> objs = ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getName().toLowerCase().trim().startsWith(name.toLowerCase().trim())).collect(Collectors.toList());

		return objs;
	}

	private boolean nameMatches(E obj, String name) {
		String jobname = obj.getName();
		// log.info("Check job {} match name {}",jobname, name);

		if (jobname.equals("DHP_EDM_PROD_6100_010.ETS_837_ENC_OUT.FT00")) {
			name.getBytes();
		}

		if (jobname.toLowerCase().trim().equals(name.toLowerCase().trim())) {
			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private E getJobByName(String name, E job) {

		if (nameMatches(job, name)) {
			return job;
		} else {
			for (BaseJobOrGroupObject obj : job.getChildren()) {
				return getJobByName(name, (E) obj);
			}
			// job.getChildren().forEach(f -> getJobByName(name,(E) f));
		}

		return null;
	}

	public E getJobByName(String name) {
		if (name.equals("DHP_EDM_PROD_6100_010.ETS_837_ENC_OUT.FT00")) {
			name.getBytes();
		}

		for (E obj : getDataObjects()) {
			E found = getJobByName(name, obj);

			if (found != null) {
				return found;
			}
		}

		return null;
	}

	public E getBaseObjectByName(String name) {
		List<E> objs = ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getName().toLowerCase().trim().equalsIgnoreCase(name.toLowerCase().trim())).collect(Collectors.toList());

		if (objs.isEmpty()) {
			return null;
		}

		if (objs.size() > 1) {
			// log.info("MULTIPLE[{}] JOBS WITH NAME[{}] returning last in the list", objs.size(), name);
			// objs.forEach(baseJobOrGroupObject -> log.info("{}", baseJobOrGroupObject.getFullPath()));
			long count = objs.size();

			return objs.stream().skip(count - 1).findFirst().get();
		} else {
			return objs.get(0);
		}

	}

	public E getBaseObjectByPath(String path) {
		return ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getFullPath().equals(path)).findAny().orElse(null);
	}

	public E getBaseObjectById(int id) {
		return ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getId() == id).findAny().orElse(null);
	}

	@SuppressWarnings("unchecked")
	public <T extends E> List<T> getObjectsByType(Class<T> type) {
		return (List<T>) ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> type.isInstance(f)).collect(Collectors.toList());
	}

	protected void doProcessEmptyGroupLogic() {
		if (getConfigeProvider().isSkipEmptyGroups()) {
			int cnt = ObjectUtils.toFlatStream(this.getDataObjects()).collect(Collectors.toList()).size();

			log.info("tidal.skip.empty.groupsr=true List Size{}", cnt); // debugging
			List<E> cleaned = new ArrayList<>();

			for (E obj : this.dataObjects) {
				List<E> tmp = doProcessEmptyGroupData(obj);
				cleaned.addAll(tmp);
			}
			this.dataObjects.clear();
			this.dataObjects.addAll(cleaned);

			cnt = ObjectUtils.toFlatStream(this.getDataObjects()).collect(Collectors.toList()).size();
			log.info("tidal.skip.empty.groupsr=true List New Size{}", cnt); // debugging
		}
	}

	@SuppressWarnings("unchecked")
	private List<E> doProcessEmptyGroupData(E obj) {
		List<E> cleaned = new ArrayList<>();

		if (obj.isGroup()) {
			if (obj.getChildren().isEmpty()) {
				// Do nothing , we are skipping this one.
				log.debug("tidal.skip.empty.groupsr=true Skipping group {} , has no children", obj.getFullPath()); // debugging
			} else {
				cleaned.add(obj);
				obj.getChildren().forEach(c -> doProcessEmptyGroupData((E) c));
			}
		} else {
			cleaned.add(obj);
		}

		return cleaned;
	}
}
