package com.bluehouseinc.dataconverter.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	List<E> dataObjects = new ArrayList<>();
	List<String> dupCheck = new ArrayList<>();

	BaseVariableProcessor<E> variableProcessor;

	@Getter(value = AccessLevel.PUBLIC)
	ITransformer<List<E>, TidalDataModel> jobTransformer;

	public BaseParserDataModel(P configeProvider) {
		this.configeProvider = configeProvider;
		this.variableProcessor = getVariableProcessor(getTidal());
		this.jobTransformer = getJobTransformer(getTidal());
	}

	@Override
	public TidalDataModel convertToDomainDataModel() {

		if (this.variableProcessor != null) {

			long startTime = System.currentTimeMillis(); // debugging

			this.getDataObjects().stream().forEach(getVariableProcessor()::processJob);

			long endTime = System.currentTimeMillis(); // debugging
			long finishtime = (endTime - startTime) / 1000; // debugging
			log.info("convertToDomainDataModel.getVariableProcessor={} seconds", finishtime); // debugging

		}

		if (this.jobTransformer != null) {
			try {

				long startTime = System.currentTimeMillis(); // debugging

				this.getJobTransformer().transform(dataObjects);

				long endTime = System.currentTimeMillis(); // debugging
				long finishtime = (endTime - startTime) / 1000; // debugging
				log.info("convertToDomainDataModel.getTransformer={} seconds", finishtime); // debugging

			} catch (TransformationException e) {
				throw new TidalException(e);
			}
		}

		doProcessData(this.dataObjects);

		return this.getTidal();
	}

	public TidalDataModel getTidal() {
		return TidalDataModel.instance(getConfigeProvider());
	}

	public abstract void doProcessData(List<E> dataObjects);

	public abstract BaseVariableProcessor<E> getVariableProcessor(TidalDataModel model);

	public abstract ITransformer<List<E>, TidalDataModel> getJobTransformer(TidalDataModel model);

	/**
	 * Add a new object to our datamodel and check for duplicates at the same level.
	 *
	 * @param <E>
	 * @param obj
	 */
	public void addDataDuplicateLevelCheck(E obj) {
		addDataDuplicateLevelCheck(obj, true);
	}

	public void addDataDuplicateLevelCheck(E obj, boolean fail) {

		String path = obj.getFullPath(); // This is what I should be concerned with.. Jobs with the same name at the
											// same
											// level.
		// dupNameCheck(path);
		// String path = obj.getId().toString();

		if (dupCheck.contains(path)) {
			if (fail) {
				throw new RuntimeException("Duplicate Job/Group[" + path + "] detected");
			} else {
				log.warn("Duplicate Job/Group[" + path + "] detected");
			}
		} else {
			this.dataObjects.add(obj);
			this.dupCheck.add(path);
		}
	}

	// synchronized private void dupNameCheck(String name) {
	// this.dataObjects.stream().forEach(f -> dupNameCheck(name.toLowerCase(), f));
	// }
	//
	// synchronized private void dupNameCheck(String name, BaseJobOrGroupObject object) {
	//
	// if (object.getName().toLowerCase().equals(name)) {
	// throw new RuntimeException("Duplicate Job/Group[" + object.getFullPath() + "] detected");
	// }
	//
	// object.getChildren().forEach(c -> dupNameCheck(name, c));
	//
	// }

	private Map<String, E> cacheJobByName = null;
	
	private Map<String, E> getLoadedJobCache() {
		if (cacheJobByName == null) {
			this.cacheJobByName = new HashMap<>();
			ObjectUtils.toFlatStream(this.getDataObjects()).forEach(jobOrGroup -> {
				if (this.cacheJobByName.containsKey(jobOrGroup.getFullPath())) {
					log.debug("already contains key: " + jobOrGroup.getFullPath());
				}
				this.cacheJobByName.put(jobOrGroup.getName(), jobOrGroup);
			});
		}
		
		return cacheJobByName;
	}
	
	
	public List<E> getBaseObjectsNameBeginsWith(String name) {
		List<E> data = new ArrayList<>();
		
		getLoadedJobCache().keySet().forEach( f -> {
			
			if(f.startsWith(name)) {
				data.add(getLoadedJobCache().get(f));
			}
		});
		
		return data;
	}
	
	public E getBaseObjectByName(String name) {
		
		if (getLoadedJobCache().containsKey(name)) {
			return getLoadedJobCache().get(name);
		}


		List<E> objs = ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getName().trim().equalsIgnoreCase(name.trim())).collect(Collectors.toList());

		if (objs.isEmpty()) {
			return null;
		}

		if (objs.size() > 1) {
			log.info("MULTIPLE[{}] JOBS WITH NAME[{}] returning last in the list", objs.size(), name);
			objs.forEach(baseJobOrGroupObject -> log.info("{}", baseJobOrGroupObject.getFullPath()));
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

	public List<E> getObjectsByType(Class<E> type) {
		return ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> type.isInstance(f)).collect(Collectors.toList());
	}

}
